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
import org.eclipse.che.api.environment.server.EnvironmentParser;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Size;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

import static com.google.common.base.MoreObjects.firstNonNull;
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

    private static final DecimalFormat DECIMAL_FORMAT             = new DecimalFormat("#0.#");
    private static final Striped<Lock> CREATE_LOCKS               = Striped.lazyWeakLock(100);
    private static final Striped<Lock> START_LOCKS                = Striped.lazyWeakLock(100);
    private static final long          BYTES_TO_MEGABYTES_DIVIDER = 1024L * 1024L;

    private final EnvironmentParser environmentParser;
    private final AccountManager accountManager;
    private final SystemRamInfoProvider systemRamInfoProvider;

    private final int  workspacesPerUser;
    private final long maxRamPerEnvMB;
    private final long ramPerUserMB;
    private final long defaultMachineMemorySizeBytes;

    @VisibleForTesting
    Semaphore startSemaphore;

    @Inject
    public LimitsCheckingWorkspaceManager(@Named("limits.user.workspaces.count") int workspacesPerUser,
                                          @Named("limits.user.workspaces.ram") String ramPerUser,
                                          @Named("limits.workspace.env.ram") String maxRamPerEnv,
                                          @Named("limits.workspace.start.throughput") int maxSameTimeStartWSRequests,
                                          SystemRamInfoProvider systemRamInfoProvider,
                                          WorkspaceDao workspaceDao,
                                          WorkspaceRuntimes runtimes,
                                          EventService eventService,
                                          SnapshotDao snapshotDao,
                                          AccountManager accountManager,
                                          EnvironmentParser environmentParser,
                                          @Named("workspace.runtime.auto_snapshot") boolean defaultAutoSnapshot,
                                          @Named("workspace.runtime.auto_restore") boolean defaultAutoRestore,
                                          @Named("machine.default_mem_size_mb") int defaultMachineMemorySizeMB) {
        super(workspaceDao, runtimes, eventService, accountManager, defaultAutoSnapshot, defaultAutoRestore, snapshotDao);
        this.accountManager = accountManager;
        this.systemRamInfoProvider = systemRamInfoProvider;
        this.workspacesPerUser = workspacesPerUser;
        this.maxRamPerEnvMB = "-1".equals(maxRamPerEnv) ? -1 : Size.parseSizeToMegabytes(maxRamPerEnv);
        this.ramPerUserMB = "-1".equals(ramPerUser) ? -1 : Size.parseSizeToMegabytes(ramPerUser);
        this.environmentParser = environmentParser;
        this.defaultMachineMemorySizeBytes = Size.parseSize(defaultMachineMemorySizeMB + "MB");
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
        checkNamespaceValidity(namespace, "Unable to create workspace because its namespace owner is " +
                                          "unavailable and it is impossible to check resources limit.");
        return checkCountAndPropagateCreation(namespace, () -> super.createWorkspace(config, namespace));
    }

    @Override
    public WorkspaceImpl createWorkspace(WorkspaceConfig config,
                                         String namespace,
                                         Map<String, String> attributes) throws ServerException,
                                                                                NotFoundException,
                                                                                ConflictException {
        checkMaxEnvironmentRam(config);
        checkNamespaceValidity(namespace, "Unable to create workspace because its namespace owner is " +
                                          "unavailable and it is impossible to check resources limit.");
        return checkCountAndPropagateCreation(namespace, () -> super.createWorkspace(config, namespace, attributes));
    }

    @Override
    public WorkspaceImpl startWorkspace(String workspaceId,
                                        @Nullable String envName,
                                        @Nullable Boolean restore) throws NotFoundException,
                                                                          ServerException,
                                                                          ConflictException {
        final WorkspaceImpl workspace = getWorkspace(workspaceId);
        checkNamespaceValidity(workspace.getNamespace(), String.format(
                "Unable to start workspace %s, because its namespace owner is " +
                "unavailable and it is impossible to check resources consumption.",
                workspaceId));
        return checkRamAndPropagateLimitedThroughputStart(workspace.getConfig(),
                                                          envName,
                                                          workspace.getNamespace(),
                                                          () -> super.startWorkspace(workspaceId, envName, restore));
    }

    @Override
    public WorkspaceImpl startWorkspace(WorkspaceConfig config,
                                        String namespace,
                                        boolean isTemporary) throws ServerException,
                                                                    NotFoundException,
                                                                    ConflictException {
        checkMaxEnvironmentRam(config);
        return checkRamAndPropagateLimitedThroughputStart(config,
                                                          config.getDefaultEnv(),
                                                          namespace,
                                                          () -> super.startWorkspace(config, namespace, isTemporary));
    }

    @Override
    public WorkspaceImpl updateWorkspace(String id, Workspace update) throws ConflictException,
                                                                             ServerException,
                                                                             NotFoundException {
        checkMaxEnvironmentRam(update.getConfig());
        return super.updateWorkspace(id, update);
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
     * One of the checks in {@link #checkRamAndPropagateStart(WorkspaceConfig, String, String, WorkspaceCallback)}
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
    <T extends WorkspaceImpl> T checkRamAndPropagateLimitedThroughputStart(WorkspaceConfig config,
                                                                           String envName,
                                                                           String namespace,
                                                                           WorkspaceCallback<T> callback) throws ServerException,
                                                                                                                 NotFoundException,
                                                                                                                 ConflictException {
        if (startSemaphore == null) {
            return checkRamAndPropagateStart(config, envName, namespace, callback);
        } else {
            try {
                startSemaphore.acquire();
                return checkRamAndPropagateStart(config, envName, namespace, callback);
            } catch (InterruptedException e) {
                currentThread().interrupt();
                throw new ServerException(e.getMessage(), e);
            } finally {
                startSemaphore.release();
            }
        }
    }

    /**
     * Checks that starting workspace won't exceed user's RAM limit.
     * Throws {@link BadRequestException} in the case of RAM constraint violation, otherwise
     * performs {@code callback.call()} and returns its result.
     */
    @VisibleForTesting
    <T extends WorkspaceImpl> T checkRamAndPropagateStart(WorkspaceConfig config,
                                                          String envName,
                                                          String namespace,
                                                          WorkspaceCallback<T> callback) throws ServerException,
                                                                                                NotFoundException,
                                                                                                ConflictException {
        if (systemRamInfoProvider.getSystemRamInfo().isSystemRamLimitExceeded()) {
            throw new LimitExceededException("Low RAM. Your workspace cannot be started until the system has more RAM available.");
        }
        if (ramPerUserMB < 0) {
            return callback.call();
        }
        final Environment env = config.getEnvironments().get(firstNonNull(envName, config.getDefaultEnv()));
        // It is important to lock in this place because:
        // if ram per user limit is 2GB and user currently using 1GB, then if he sends 2 separate requests to start a new
        // 1 GB workspace , it may start both of them, because currently allocated ram check is not atomic one
        final Lock lock = START_LOCKS.get(namespace);
        lock.lock();
        try {
            final List<WorkspaceImpl> workspacesPerUser = getByNamespace(namespace);
            final long runningWorkspaces = workspacesPerUser.stream()
                                                            .filter(ws -> STOPPED != ws.getStatus())
                                                            .count();
            final long currentlyUsedRamMB = workspacesPerUser.stream()
                                                             .filter(ws -> STOPPED != ws.getStatus())
                                                             .map(ws -> ws.getRuntime().getMachines())
                                                             .flatMap(List::stream)
                                                             .mapToInt(machine -> machine.getConfig()
                                                                                         .getLimits()
                                                                                         .getRam())
                                                             .sum();
            final long currentlyFreeRamMB = ramPerUserMB - currentlyUsedRamMB;
            final long allocating = sumRam(env);
            if (allocating > currentlyFreeRamMB) {
                final String usedRamGb = DECIMAL_FORMAT.format(currentlyUsedRamMB / 1024D);
                final String limitRamGb = DECIMAL_FORMAT.format(ramPerUserMB / 1024D);
                final String requiredRamGb = DECIMAL_FORMAT.format(allocating / 1024D);
                throw new LimitExceededException(format("There are %d running workspaces consuming" +
                                                        " %sGB RAM. Your current RAM limit is %sGB." +
                                                        " This workspaces requires an additional %sGB." +
                                                        " You can stop other workspaces to free resources.",
                                                        runningWorkspaces,
                                                        usedRamGb,
                                                        limitRamGb,
                                                        requiredRamGb),
                                                 ImmutableMap.of("workspaces_count", Long.toString(runningWorkspaces),
                                                                 "used_ram", usedRamGb,
                                                                 "limit_ram", limitRamGb,
                                                                 "required_ram", requiredRamGb,
                                                                 "ram_unit", "GB"));
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
    <T extends WorkspaceImpl> T checkCountAndPropagateCreation(String namespace,
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
                throw new LimitExceededException(format("The maximum workspaces allowed per user is set to '%d' and " +
                                                        "you are currently at that limit. This value is set by your admin with the " +
                                                        "'limits.user.workspaces.count' property",
                                                        workspacesPerUser),
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
            final long workspaceRam = sumRam(env);
            if (workspaceRam > maxRamPerEnvMB) {
                throw new LimitExceededException(format("The maximum RAM per workspace is set to '%dmb' and you requested '%dmb'. " +
                                                        "This value is set by your admin with the 'limits.workspace.env.ram' property",
                                                        maxRamPerEnvMB,
                                                        workspaceRam),
                                                 ImmutableMap.of("environment_max_ram", Long.toString(maxRamPerEnvMB),
                                                                 "environment_max_ram_unit", "mb",
                                                                 "environment_ram", Long.toString(workspaceRam),
                                                                 "environment_ram_unit", "mb"));
            }
        }
    }

    /**
     * Parses (and fetches if needed) recipe of environment and sums RAM size of all machines in environment in megabytes.
     */
    private long sumRam(Environment environment) throws ServerException {
        CheServicesEnvironmentImpl composeEnv = environmentParser.parse(environment);

        long sumBytes = composeEnv.getServices()
                                  .values()
                                  .stream()
                                  .mapToLong(value -> {
                                      if (value.getMemLimit() == null || value.getMemLimit() == 0) {
                                          return defaultMachineMemorySizeBytes;
                                      } else {
                                          return value.getMemLimit();
                                      }
                                  })
                                  .sum();
        return sumBytes / BYTES_TO_MEGABYTES_DIVIDER;
    }

    private void checkNamespaceValidity(String namespace, String errorMsg) throws ServerException {
        try {
            accountManager.getByName(namespace);
        } catch (NotFoundException e) {
            throw new ServerException(errorMsg);
        }
    }
}
