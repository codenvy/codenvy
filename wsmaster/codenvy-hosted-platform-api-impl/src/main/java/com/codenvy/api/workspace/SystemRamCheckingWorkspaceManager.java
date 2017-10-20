/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.api.workspace;

import static java.lang.Thread.currentThread;

import com.codenvy.service.system.SystemRamInfoProvider;
import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.Semaphore;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.WorkspaceSharedPool;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.multiuser.resource.api.usage.ResourceUsageManager;
import org.eclipse.che.multiuser.resource.api.usage.ResourcesLocks;
import org.eclipse.che.multiuser.resource.api.usage.tracker.EnvironmentRamCalculator;
import org.eclipse.che.multiuser.resource.api.workspace.LimitExceededException;
import org.eclipse.che.multiuser.resource.api.workspace.LimitsCheckingWorkspaceManager;

/**
 * Manager that checks limits and delegates all its operations to the {@link WorkspaceManager}.
 * Doesn't contain any logic related to start/stop or any kind of operations different from limits
 * checks.
 *
 * @author Yevhenii Voevodin
 * @author Igor Vinokur
 * @author Sergii Leschenko
 */
@Singleton
public class SystemRamCheckingWorkspaceManager extends LimitsCheckingWorkspaceManager {

  private final SystemRamInfoProvider systemRamInfoProvider;

  @VisibleForTesting Semaphore startSemaphore;

  @Inject
  public SystemRamCheckingWorkspaceManager(
      WorkspaceDao workspaceDao,
      WorkspaceRuntimes runtimes,
      EventService eventService,
      AccountManager accountManager,
      @Named("che.workspace.auto_snapshot") boolean defaultAutoSnapshot,
      @Named("che.workspace.auto_restore") boolean defaultAutoRestore,
      SnapshotDao snapshotDao,
      WorkspaceSharedPool sharedPool,
      // own injects
      @Named("che.limits.workspace.env.ram") String maxRamPerEnv,
      @Named("limits.workspace.start.throughput") int maxSameTimeStartWSRequests,
      SystemRamInfoProvider systemRamInfoProvider,
      EnvironmentRamCalculator environmentRamCalculator,
      ResourceUsageManager resourceUsageManager,
      ResourcesLocks resourcesLocks) {
    super(
        workspaceDao,
        runtimes,
        eventService,
        accountManager,
        defaultAutoSnapshot,
        defaultAutoRestore,
        snapshotDao,
        sharedPool,
        maxRamPerEnv,
        environmentRamCalculator,
        resourceUsageManager,
        resourcesLocks);
    this.systemRamInfoProvider = systemRamInfoProvider;

    if (maxSameTimeStartWSRequests > 0) {
      this.startSemaphore = new Semaphore(maxSameTimeStartWSRequests);
    }
  }

  @Override
  public WorkspaceImpl startWorkspace(
      String workspaceId, @Nullable String envName, @Nullable Boolean restore)
      throws NotFoundException, ServerException, ConflictException {
    return checkSystemRamLimitAndPropagateLimitedThroughputStart(
        () -> super.startWorkspace(workspaceId, envName, restore));
  }

  @Override
  public WorkspaceImpl startWorkspace(WorkspaceConfig config, String namespace, boolean isTemporary)
      throws ServerException, NotFoundException, ConflictException {
    return checkSystemRamLimitAndPropagateLimitedThroughputStart(
        () -> super.startWorkspace(config, namespace, isTemporary));
  }

  /**
   * Defines callback which should be called when all necessary checks are performed. Helps to
   * propagate actions to the super class.
   */
  @FunctionalInterface
  @VisibleForTesting
  interface WorkspaceCallback<T extends WorkspaceImpl> {

    T call() throws ConflictException, NotFoundException, ServerException;
  }

  /**
   * One of the checks in {@link #checkSystemRamLimitAndPropagateStart(WorkspaceCallback)} is needed
   * to deny starting workspace, if system RAM limit exceeded. This check may be slow because it is
   * based on request to swarm for memory amount allocated on all nodes, but it can't be performed
   * more than specified times at the same time, and the semaphore is used to control that. The
   * semaphore is a trade off between speed and risk to exceed system RAM limit. In the worst case
   * specified number of permits to start workspace can happen at the same time after the actually
   * system limit allows to start only one workspace, all permits will be allowed to start
   * workspace. If more than specified number of permits to start workspace happens, they will wait
   * in a queue. limits.workspace.start.throughput property configures how many permits can be
   * handled at the same time.
   */
  @VisibleForTesting
  <T extends WorkspaceImpl> T checkSystemRamLimitAndPropagateLimitedThroughputStart(
      WorkspaceCallback<T> callback) throws ServerException, NotFoundException, ConflictException {
    if (startSemaphore == null) {
      return checkSystemRamLimitAndPropagateStart(callback);
    } else {
      try {
        startSemaphore.acquire();
        return checkSystemRamLimitAndPropagateStart(callback);
      } catch (InterruptedException e) {
        currentThread().interrupt();
        throw new ServerException(e.getMessage(), e);
      } finally {
        startSemaphore.release();
      }
    }
  }

  /**
   * Checks that starting workspace won't exceed system RAM limit. Then, if previous check is
   * passed, checks that starting workspace won't exceed user's started workspaces number limit.
   * Throws {@link LimitExceededException} in the case of constraints violation, otherwise performs
   * {@code callback.call()} and returns its result.
   */
  @VisibleForTesting
  <T extends WorkspaceImpl> T checkSystemRamLimitAndPropagateStart(WorkspaceCallback<T> callback)
      throws ServerException, NotFoundException, ConflictException {
    if (systemRamInfoProvider.getSystemRamInfo().isSystemRamLimitExceeded()) {
      throw new LimitExceededException(
          "Low RAM. Your workspace cannot be started until the system has more RAM available.");
    }

    return callback.call();
  }
}
