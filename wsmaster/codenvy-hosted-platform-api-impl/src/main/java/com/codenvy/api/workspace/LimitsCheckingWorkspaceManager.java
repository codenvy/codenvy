/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.api.workspace;

import com.codenvy.service.systemram.SystemRamInfoProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Striped;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.WorkspaceSharedPool;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Size;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;

/**
 * Manager that checks limits and delegates all its operations to the {@link WorkspaceManager}.
 * Doesn't contain any logic related to start/stop or any kind of operations different from limits checks.
 *
 * @author Yevhenii Voevodin
 * @author Igor Vinokur
 */
@Singleton
public class LimitsCheckingWorkspaceManager extends WorkspaceManager {

    private static final Striped<Lock> CREATE_LOCKS = Striped.lazyWeakLock(100);
    private static final Striped<Lock> START_LOCKS  = Striped.lazyWeakLock(100);

    private final SystemRamInfoProvider    systemRamInfoProvider;
    private final EnvironmentRamCalculator environmentRamCalculator;

    private final int workspacesPerUser;
    private final int startedWorkspacesLimit;

    private final long maxRamPerEnvMB;


    @VisibleForTesting
    Semaphore startSemaphore;

    @Inject
    public LimitsCheckingWorkspaceManager(@Named("limits.user.workspaces.count") int workspacesPerUser,
                                          @Named("limits.user.workspaces.run.count") int startedWorkspacesLimit,
                                          @Named("limits.workspace.env.ram") String maxRamPerEnv,
                                          @Named("limits.workspace.start.throughput") int maxSameTimeStartWSRequests,
                                          SystemRamInfoProvider systemRamInfoProvider,
                                          WorkspaceDao workspaceDao,
                                          WorkspaceRuntimes runtimes,
                                          EventService eventService,
                                          SnapshotDao snapshotDao,
                                          AccountManager accountManager,
                                          @Named("che.workspace.auto_snapshot") boolean defaultAutoSnapshot,
                                          @Named("che.workspace.auto_restore") boolean defaultAutoRestore,
                                          EnvironmentRamCalculator environmentRamCalculator,
                                          WorkspaceSharedPool sharedPool) {
        super(workspaceDao, runtimes, eventService, accountManager, defaultAutoSnapshot, defaultAutoRestore, snapshotDao, sharedPool);
        this.startedWorkspacesLimit = startedWorkspacesLimit;
        this.systemRamInfoProvider = systemRamInfoProvider;
        this.workspacesPerUser = workspacesPerUser;
        this.environmentRamCalculator = environmentRamCalculator;
        this.maxRamPerEnvMB = "-1".equals(maxRamPerEnv) ? -1 : Size.parseSizeToMegabytes(maxRamPerEnv);

        if (maxSameTimeStartWSRequests > 0) {
            this.startSemaphore = new Semaphore(maxSameTimeStartWSRequests);
        }
    }

    @Override
    public WorkspaceImpl createWorkspace(WorkspaceConfig config,
                                         String namespace) throws ServerException,
                                                                  ConflictException,
                                                                  NotFoundException {
        checkMaxEnvironmentRam(config);
        return checkNumberOfWorkspacesAndPropagateCreation(namespace, () -> super.createWorkspace(config, namespace));
    }

    @Override
    public WorkspaceImpl createWorkspace(WorkspaceConfig config,
                                         String namespace,
                                         Map<String, String> attributes) throws ServerException,
                                                                                NotFoundException,
                                                                                ConflictException {
        checkMaxEnvironmentRam(config);
        return checkNumberOfWorkspacesAndPropagateCreation(namespace, () -> super.createWorkspace(config, namespace, attributes));
    }

    @Override
    public WorkspaceImpl startWorkspace(String workspaceId,
                                        @Nullable String envName,
                                        @Nullable Boolean restore) throws NotFoundException,
                                                                          ServerException,
                                                                          ConflictException {
        return checkLimitsAndPropagateLimitedThroughputStart(getWorkspace(workspaceId).getNamespace(),
                                                             () -> super.startWorkspace(workspaceId, envName, restore));
    }

    @Override
    public WorkspaceImpl startWorkspace(WorkspaceConfig config,
                                        String namespace,
                                        boolean isTemporary) throws ServerException,
                                                                    NotFoundException,
                                                                    ConflictException {
        checkMaxEnvironmentRam(config);
        return checkNumberOfWorkspacesAndPropagateCreation(namespace, () -> checkLimitsAndPropagateLimitedThroughputStart(
                namespace, () -> LimitsCheckingWorkspaceManager.super.startWorkspace(config, namespace, isTemporary)));
    }

    @Override
    public WorkspaceImpl updateWorkspace(String id, Workspace update) throws ConflictException,
                                                                             ServerException,
                                                                             NotFoundException {
        checkMaxEnvironmentRam(update.getConfig());
        // Workspace must not be updated while the manager checks it's resources to allow start
        final Lock lock = START_LOCKS.get(getWorkspace(id).getNamespace());
        lock.lock();
        try {
            return super.updateWorkspace(id, update);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Defines callback which should be called when all necessary checks are performed.
     * Helps to propagate actions to the super class.
     */
    @FunctionalInterface
    @VisibleForTesting
    interface WorkspaceCallback<T extends WorkspaceImpl> {
        T call() throws ConflictException, NotFoundException, ServerException;
    }

    /**
     * One of the checks in {@link #checkLimitsAndPropagateStart(String, WorkspaceCallback)}
     * is needed to deny starting workspace, if system RAM limit exceeded.
     * This check may be slow because it is based on request to swarm for memory amount allocated on all nodes, but it
     * can't be performed more than specified times at the same time, and the semaphore is used to control that.
     * The semaphore is a trade off between speed and risk to exceed system RAM limit.
     * In the worst case specified number of permits to start workspace can happen at the same time after the actually
     * system limit allows to start only one workspace, all permits will be allowed to start workspace.
     * If more than specified number of permits to start workspace happens, they will wait in a queue.
     * limits.workspace.start.throughput property configures how many permits can be handled at the same time.
     */
    @VisibleForTesting
    <T extends WorkspaceImpl> T checkLimitsAndPropagateLimitedThroughputStart(String namespace, WorkspaceCallback<T> callback)
            throws ServerException, NotFoundException, ConflictException {
        if (startSemaphore == null) {
            return checkLimitsAndPropagateStart(namespace, callback);
        } else {
            try {
                startSemaphore.acquire();
                return checkLimitsAndPropagateStart(namespace, callback);
            } catch (InterruptedException e) {
                currentThread().interrupt();
                throw new ServerException(e.getMessage(), e);
            } finally {
                startSemaphore.release();
            }
        }
    }

    /**
     * Checks that starting workspace won't exceed system RAM limit.
     * Then, if previous check is passed, checks that starting workspace won't exceed user's started workspaces number limit.
     * Throws {@link LimitExceededException} in the case of constraints violation, otherwise
     * performs {@code callback.call()} and returns its result.
     */
    @VisibleForTesting
    <T extends WorkspaceImpl> T checkLimitsAndPropagateStart(String namespace, WorkspaceCallback<T> callback)
            throws ServerException, NotFoundException, ConflictException {
        if (systemRamInfoProvider.getSystemRamInfo().isSystemRamLimitExceeded()) {
            throw new LimitExceededException("Low RAM. Your workspace cannot be started until the system has more RAM available.");
        }

        if (startedWorkspacesLimit < 0) {
            return callback.call();
        }

        // It is important to lock in this place because:
        // if started workspaces number limit is 10 and user has 9 started workspaces, then if he sends 2 separate requests
        // to start a workspace, it may start both of them, because started workspaces number check is not atomic one.
        final Lock lock = START_LOCKS.get(namespace);
        lock.lock();
        try {
            long startedWorkspaces = getByNamespace(namespace).stream().filter(ws -> STOPPED != ws.getStatus()).count();
            if (startedWorkspaces >= startedWorkspacesLimit) {
                throw new LimitExceededException(format("You are only allowed to start %d workspace%s.", startedWorkspacesLimit,
                                                        startedWorkspacesLimit == 1 ? "" : "s"));
            }
            return callback.call();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks that created workspace won't exceed user's workspaces limit.
     * Throws {@link BadRequestException} in the case of workspace limit constraint violation, otherwise
     * performs {@code callback.call()} and returns its result.
     */
    @VisibleForTesting
    <T extends WorkspaceImpl> T checkNumberOfWorkspacesAndPropagateCreation(String namespace,
                                                                            WorkspaceCallback<T> callback) throws ServerException,
                                                                                                                  NotFoundException,
                                                                                                                  ConflictException {
        if (workspacesPerUser < 0) {
            return callback.call();
        }
        // It is important to lock in this place because:
        // if workspace per user limit is 10 and user has 9, then if he sends 2 separate requests to create
        // a new workspace, it may create both of them, because workspace count check is not atomic one
        final Lock lock = CREATE_LOCKS.get(namespace);
        lock.lock();
        try {
            final List<WorkspaceImpl> workspaces = getByNamespace(namespace);
            if (workspaces.size() >= workspacesPerUser) {
                throw new LimitExceededException(format("You are only allowed to create %d workspace%s.", workspacesPerUser,
                                                        workspacesPerUser == 1 ? "" : "s"),
                                                 ImmutableMap.of("workspace_max_count", Integer.toString(workspacesPerUser)));
            }
            return callback.call();
        } finally {
            lock.unlock();
        }
    }

    @VisibleForTesting
    void checkMaxEnvironmentRam(WorkspaceConfig config) throws ServerException {
        if (maxRamPerEnvMB < 0) {
            return;
        }
        for (Map.Entry<String, ? extends Environment> envEntry : config.getEnvironments().entrySet()) {
            Environment env = envEntry.getValue();
            final long workspaceRam = environmentRamCalculator.calculate(env);
            if (workspaceRam > maxRamPerEnvMB) {
                throw new LimitExceededException(format("You are only allowed to use %d mb. RAM per workspace.", maxRamPerEnvMB),
                                                 ImmutableMap.of("environment_max_ram", Long.toString(maxRamPerEnvMB),
                                                                 "environment_max_ram_unit", "mb",
                                                                 "environment_ram", Long.toString(workspaceRam),
                                                                 "environment_ram_unit", "mb"));
            }
        }
    }


}
