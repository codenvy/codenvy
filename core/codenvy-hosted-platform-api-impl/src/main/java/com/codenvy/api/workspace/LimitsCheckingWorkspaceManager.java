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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Striped;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Size;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;

/**
 * Manager that checks limits and delegates all its operations to the {@link WorkspaceManager}.
 * Doesn't contain any logic related to start/stop or any kind of operations different from limits checks.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class LimitsCheckingWorkspaceManager extends WorkspaceManager {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.#");
    private static final Striped<Lock> CREATE_LOCKS   = Striped.lazyWeakLock(100);
    private static final Striped<Lock> START_LOCKS    = Striped.lazyWeakLock(100);

    private final int  workspacesPerUser;
    private final long maxRamPerEnv;
    private final long ramPerUser;

    @Inject
    public LimitsCheckingWorkspaceManager(@Named("limits.user.workspaces.count") int workspacesPerUser,
                                          @Named("limits.user.workspaces.ram") String ramPerUser,
                                          @Named("limits.workspace.env.ram") String maxRamPerEnv,
                                          WorkspaceDao workspaceDao,
                                          WorkspaceRuntimes runtimes,
                                          EventService eventService,
                                          MachineManager machineManager,
                                          UserManager userManager) {
        super(workspaceDao, runtimes, eventService, machineManager, userManager);
        this.workspacesPerUser = workspacesPerUser;
        this.maxRamPerEnv = "-1".equals(maxRamPerEnv) ? -1 : Size.parseSizeToMegabytes(maxRamPerEnv);
        this.ramPerUser = "-1".equals(ramPerUser) ? -1 : Size.parseSizeToMegabytes(ramPerUser);
    }

    @Override
    public WorkspaceImpl createWorkspace(WorkspaceConfig config,
                                         String namespace,
                                         @Nullable String accountId) throws ServerException,
                                                                            ConflictException,
                                                                            NotFoundException {
        checkMaxEnvironmentRam(config);
        return checkCountAndPropagateCreation(namespace, () -> super.createWorkspace(config, namespace, accountId));
    }

    @Override
    public WorkspaceImpl createWorkspace(WorkspaceConfig config,
                                         String namespace,
                                         Map<String, String> attributes,
                                         @Nullable String accountId) throws ServerException,
                                                                            NotFoundException,
                                                                            ConflictException {
        checkMaxEnvironmentRam(config);
        return checkCountAndPropagateCreation(namespace, () -> super.createWorkspace(config, namespace, accountId));
    }

    @Override
    public WorkspaceImpl startWorkspace(String workspaceId,
                                        @Nullable String envName,
                                        @Nullable String accountId) throws NotFoundException,
                                                                           ServerException,
                                                                           ConflictException {
        final WorkspaceImpl workspace = getWorkspace(workspaceId);
        return checkRamAndPropagateStart(workspace.getConfig(),
                                         envName,
                                         workspace.getNamespace(),
                                         () -> super.startWorkspace(workspaceId, envName, accountId));
    }

    @Override
    public WorkspaceImpl startWorkspace(WorkspaceConfig config,
                                        String namespace,
                                        boolean isTemporary,
                                        @Nullable String accountId) throws ServerException,
                                                                           NotFoundException,
                                                                           ConflictException {
        checkMaxEnvironmentRam(config);
        return checkRamAndPropagateStart(config,
                                         config.getDefaultEnv(),
                                         getCurrentUserId(),
                                         () -> super.startWorkspace(config, namespace, isTemporary, accountId));
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
     * Checks that starting workspace won't exceed user's RAM limit.
     * Throws {@link BadRequestException} in the case of RAM constraint violation, otherwise
     * performs {@code callback.call()} and returns its result.
     */
    @VisibleForTesting
    <T extends WorkspaceImpl> T checkRamAndPropagateStart(WorkspaceConfig config,
                                                          String envName,
                                                          String user,
                                                          WorkspaceCallback<T> callback) throws ServerException,
                                                                                                NotFoundException,
                                                                                                ConflictException {
        if (ramPerUser < 0) {
            return callback.call();
        }
        Optional<? extends Environment> envOptional = findEnv(config.getEnvironments(), envName);
        if (!envOptional.isPresent()) {
            envOptional = findEnv(config.getEnvironments(), config.getDefaultEnv());
        }
        // It is important to lock in this place because:
        // if ram per user limit is 2GB and user currently using 1GB, then if he sends 2 separate requests to start a new
        // 1 GB workspace , it may start both of them, because currently allocated ram check is not atomic one
        final Lock lock = START_LOCKS.get(user);
        lock.lock();
        try {
            final List<WorkspaceImpl> workspacesPerUser = getWorkspaces(user);
            final long runningWorkspaces = workspacesPerUser.stream().filter(ws -> STOPPED != ws.getStatus()).count();
            final long currentlyUsedRamMB = workspacesPerUser.stream().filter(ws -> STOPPED != ws.getStatus())
                                                             .map(ws -> ws.getConfig()
                                                                          .getEnvironment(ws.getRuntime().getActiveEnv())
                                                                          .get()
                                                                          .getMachineConfigs())
                                                             .mapToLong(this::sumRam)
                                                             .sum();
            final long currentlyFreeRamMB = ramPerUser - currentlyUsedRamMB;
            final long allocating = sumRam(envOptional.get().getMachineConfigs());
            if (allocating > currentlyFreeRamMB) {
                final String usedRamGb = DECIMAL_FORMAT.format(currentlyUsedRamMB / 1024D);
                final String limitRamGb = DECIMAL_FORMAT.format(ramPerUser / 1024D);
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
    <T extends WorkspaceImpl> T checkCountAndPropagateCreation(String user,
                                                               WorkspaceCallback<T> callback) throws ServerException,
                                                                                                     NotFoundException,
                                                                                                     ConflictException {
        if (workspacesPerUser < 0) {
            return callback.call();
        }
        // It is important to lock in this place because:
        // if workspace per user limit is 10 and user has 9, then if he sends 2 separate requests to create
        // a new workspace, it may create both of them, because workspace count check is not atomic one
        final Lock lock = CREATE_LOCKS.get(user);
        lock.lock();
        try {
            final List<WorkspaceImpl> workspaces = getWorkspaces(user);
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
    void checkMaxEnvironmentRam(WorkspaceConfig config) throws LimitExceededException {
        if (maxRamPerEnv < 0) {
            return;
        }
        for (Environment environment : config.getEnvironments()) {
            final long workspaceRam = environment.getMachineConfigs()
                                                 .stream()
                                                 .filter(machineCfg -> machineCfg.getLimits() != null)
                                                 .mapToInt(machineCfg -> machineCfg.getLimits().getRam())
                                                 .sum();
            if (workspaceRam > maxRamPerEnv) {
                throw new LimitExceededException(format("The maximum RAM per workspace is set to '%dmb' and you requested '%dmb'. " +
                                                        "This value is set by your admin with the 'limits.workspace.env.ram' property",
                                                        maxRamPerEnv,
                                                        workspaceRam),
                                                 ImmutableMap.of("environment_max_ram", Long.toString(maxRamPerEnv),
                                                                 "environment_max_ram_unit", "mb",
                                                                 "environment_ram", Long.toString(workspaceRam),
                                                                 "environment_ram_unit", "mb"));
            }
        }
    }

    private long sumRam(List<? extends MachineConfig> machineConfigs) {
        return machineConfigs.stream()
                             .mapToInt(m -> m.getLimits().getRam())
                             .sum();
    }

    private Optional<? extends Environment> findEnv(List<? extends Environment> environments, String envName) {
        return environments.stream()
                           .filter(env -> env.getName().equals(envName))
                           .findFirst();
    }

    private String getCurrentUserId() {
        return EnvironmentContext.getCurrent().getSubject().getUserId();
    }
}
