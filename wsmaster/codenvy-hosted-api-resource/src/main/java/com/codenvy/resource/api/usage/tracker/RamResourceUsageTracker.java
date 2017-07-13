/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.resource.api.usage.tracker;

import com.codenvy.resource.api.ResourceUsageTracker;
import com.codenvy.resource.api.type.RamResourceType;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;

/**
 * Tracks usage of {@link RamResourceType} resource.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RamResourceUsageTracker implements ResourceUsageTracker {
    private final Provider<WorkspaceManager> workspaceManagerProvider;
    private final AccountManager             accountManager;
    private final EnvironmentRamCalculator   environmentRamCalculator;

    @Inject
    public RamResourceUsageTracker(Provider<WorkspaceManager> workspaceManagerProvider,
                                   AccountManager accountManager,
                                   EnvironmentRamCalculator environmentRamCalculator) {
        this.workspaceManagerProvider = workspaceManagerProvider;
        this.accountManager = accountManager;
        this.environmentRamCalculator = environmentRamCalculator;
    }

    @Override
    public Optional<Resource> getUsedResource(String accountId) throws NotFoundException, ServerException {
        final Account account = accountManager.getById(accountId);
        List<WorkspaceImpl> activeWorkspaces = workspaceManagerProvider.get()
                                                                       .getByNamespace(account.getName(), true)
                                                                       .stream()
                                                                       .filter(ws -> STOPPED != ws.getStatus())
                                                                       .collect(Collectors.toList());
        long currentlyUsedRamMB = 0;
        for (WorkspaceImpl activeWorkspace : activeWorkspaces) {
            if (WorkspaceStatus.STARTING.equals(activeWorkspace.getStatus())) {
                //starting workspace may not have all machine in runtime
                //it is need to calculate ram from environment config
                EnvironmentImpl activeEnvironmentConfig = activeWorkspace.getConfig()
                                                                         .getEnvironments()
                                                                         .get(activeWorkspace.getRuntime()
                                                                                             .getActiveEnv());

                currentlyUsedRamMB += environmentRamCalculator.calculate(activeEnvironmentConfig);
            } else {
                currentlyUsedRamMB += activeWorkspace.getRuntime()
                                                     .getMachines()
                                                     .stream()
                                                     .mapToInt(machine -> machine.getConfig()
                                                                                 .getLimits()
                                                                                 .getRam())
                                                     .sum();
            }
        }

        if (currentlyUsedRamMB > 0) {
            return Optional.of(new ResourceImpl(RamResourceType.ID, currentlyUsedRamMB, RamResourceType.UNIT));
        } else {
            return Optional.empty();
        }
    }
}
