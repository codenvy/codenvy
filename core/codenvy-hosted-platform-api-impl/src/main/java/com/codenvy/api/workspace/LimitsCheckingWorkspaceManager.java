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
import com.google.common.util.concurrent.Striped;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.RuntimeWorkspaceRegistry;
import org.eclipse.che.api.workspace.server.WorkspaceConfigValidator;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeWorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.UsersWorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Size;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import static java.lang.String.format;

/**
 * Manager that checks limits and delegates all its operations to the {@link WorkspaceManager}.
 * Doesn't contain any logic related to start/stop or any kind of operations different from limits checks.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class LimitsCheckingWorkspaceManager extends WorkspaceManager {

    private static final Striped<Lock> CREATE_LOCKS = Striped.lazyWeakLock(100);
    private static final Striped<Lock> START_LOCKS  = Striped.lazyWeakLock(100);

    private final int  workspacesPerUser;
    private final long ramPerUser;

    @Inject
    public LimitsCheckingWorkspaceManager(@Named("limits.user.workspaces.count") int workspacesPerUser,
                                          @Named("limits.user.workspaces.ram") String ramPerUser,
                                          WorkspaceDao workspaceDao,
                                          RuntimeWorkspaceRegistry workspaceRegistry,
                                          WorkspaceConfigValidator workspaceConfigValidator,
                                          EventService eventService,
                                          MachineManager machineManager,
                                          UserManager userManager) {
        super(workspaceDao, workspaceRegistry, workspaceConfigValidator, eventService, machineManager, userManager);
        this.workspacesPerUser = workspacesPerUser;
        this.ramPerUser = Size.parseSizeToMegabytes(ramPerUser);
    }

    @Override
    public UsersWorkspaceImpl createWorkspace(WorkspaceConfig config,
                                              String owner,
                                              @Nullable String accountId) throws ForbiddenException,
                                                                                 ServerException,
                                                                                 BadRequestException,
                                                                                 ConflictException,
                                                                                 NotFoundException {
        return checkCountAndPropagateCreation(owner, () -> super.createWorkspace(config, owner, accountId));
    }

    @Override
    public UsersWorkspaceImpl startWorkspaceById(String workspaceId,
                                                 @Nullable String envName,
                                                 @Nullable String accountId) throws NotFoundException,
                                                                                    ServerException,
                                                                                    BadRequestException,
                                                                                    ForbiddenException,
                                                                                    ConflictException {
        final UsersWorkspaceImpl workspace = getWorkspace(workspaceId);
        return checkRamAndPropagateStart(workspace.getConfig(),
                                         envName,
                                         workspace.getOwner(),
                                         () -> super.startWorkspaceById(workspaceId, envName, accountId));
    }

    @Override
    public UsersWorkspaceImpl startWorkspaceByName(String workspaceName,
                                                   String owner,
                                                   @Nullable String envName,
                                                   @Nullable String accountId) throws NotFoundException,
                                                                                      ServerException,
                                                                                      BadRequestException,
                                                                                      ForbiddenException,
                                                                                      ConflictException {
        final UsersWorkspaceImpl workspace = getWorkspace(workspaceName, owner);
        return checkRamAndPropagateStart(workspace.getConfig(),
                                         envName,
                                         owner,
                                         () -> super.startWorkspaceByName(workspaceName, owner, envName, accountId));
    }

    @Override
    public RuntimeWorkspaceImpl startTemporaryWorkspace(WorkspaceConfig workspaceConfig,
                                                        @Nullable String accountId) throws ServerException,
                                                                                           BadRequestException,
                                                                                           ForbiddenException,
                                                                                           NotFoundException,
                                                                                           ConflictException {
        return checkRamAndPropagateStart(workspaceConfig,
                                         workspaceConfig.getDefaultEnv(),
                                         getCurrentUserId(),
                                         () -> super.startTemporaryWorkspace(workspaceConfig, accountId));
    }

    /**
     * Defines callback which should be called when all necessary checks are performed.
     * Helps to propagate actions to the super class.
     */
    @FunctionalInterface
    @VisibleForTesting
    interface WorkspaceCallback<T extends UsersWorkspaceImpl> {
        T call() throws ConflictException, BadRequestException, ForbiddenException, NotFoundException, ServerException;
    }

    /**
     * Checks that starting workspace won't exceed user's RAM limit.
     * Throws {@link BadRequestException} in the case of RAM constraint violation, otherwise
     * performs {@code callback.call()} and returns its result.
     */
    @VisibleForTesting
    <T extends UsersWorkspaceImpl> T checkRamAndPropagateStart(WorkspaceConfig config,
                                                               String envName,
                                                               String user,
                                                               WorkspaceCallback<T> callback) throws BadRequestException,
                                                                                                     ForbiddenException,
                                                                                                     ServerException,
                                                                                                     NotFoundException,
                                                                                                     ConflictException {
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
            final long currentlyUsedRamMB = getRuntimeWorkspaces(user).stream()
                                                                      .map(ws -> ws.getActiveEnvironment().getMachineConfigs())
                                                                      .mapToLong(this::sumRam)
                                                                      .sum();
            final long allocating = sumRam(envOptional.get().getMachineConfigs());
            if (currentlyUsedRamMB + allocating > ramPerUser) {
                throw new BadRequestException(format("This workspace cannot be started as it would exceed the maximum available RAM " +
                                                     "allocated to you. Users are each currently allocated '%dmb' RAM across their " +
                                                     "active workspaces. This value is set by your admin with the " +
                                                     "'limits.user.workspaces.ram' property",
                                                     ramPerUser));
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
    <T extends UsersWorkspaceImpl> T checkCountAndPropagateCreation(String user,
                                                                    WorkspaceCallback<T> callback) throws BadRequestException,
                                                                                                          ForbiddenException,
                                                                                                          ServerException,
                                                                                                          NotFoundException,
                                                                                                          ConflictException {
        // It is important to lock in this place because:
        // if workspace per user limit is 10 and user has 9, then if he sends 2 separate requests to create
        // a new workspace, it may create both of them, because workspace count check is not atomic one
        final Lock lock = CREATE_LOCKS.get(user);
        lock.lock();
        try {
            final List<UsersWorkspaceImpl> workspaces = getWorkspaces(user);
            if (workspaces.size() >= workspacesPerUser) {
                throw new BadRequestException(format("The maximum workspaces allowed per user is set to '%d' and " +
                                                     "you are currently at that limit. This value is set by your admin with the " +
                                                     "'limits.user.workspaces.count' property",
                                                     workspacesPerUser));
            }
            return callback.call();
        } finally {
            lock.unlock();
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
        return EnvironmentContext.getCurrent().getUser().getId();
    }
}
