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
package com.codenvy.resource.api.ram;

import com.codenvy.api.workspace.EnvironmentRamCalculator;
import com.codenvy.resource.api.exception.NoEnoughResourcesException;
import com.codenvy.resource.api.usage.ResourceUsageManager;
import com.codenvy.resource.api.usage.ResourcesLocks;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.lang.concurrent.CloseableLock;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

/**
 * Intercepts {@link WorkspaceManager#startWorkspace(String, String, Boolean)}
 * and {@link WorkspaceManager#startWorkspace(WorkspaceConfig, String, boolean)}
 * lock account's resources while workspace is starting and check RAM availability.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class StartWorkspaceResourcesLocker implements MethodInterceptor {
    @Inject
    private ResourceUsageManager resourceUsageManager;

    @Inject
    private WorkspaceManager workspaceManager;

    @Inject
    private AccountManager accountManager;

    @Inject
    private ResourcesLocks resourcesLocks;

    @Inject
    private EnvironmentRamCalculator ramCalculator;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Object[] arguments = invocation.getArguments();

        WorkspaceConfig config;
        String envName = null;
        String namespace;

        if (arguments[0] instanceof String) {
            //start existed workspace
            String workspaceId = (String)arguments[0];
            WorkspaceImpl workspace = workspaceManager.getWorkspace(workspaceId);
            namespace = workspace.getNamespace();
            config = workspace.getConfig();
            envName = (String)arguments[1];
        } else {
            //start from config with default env
            config = (WorkspaceConfig)arguments[0];
            namespace = (String)arguments[1];
        }

        final Account account = accountManager.getByName(namespace);
        final String accountId = account.getId();

        final Environment environment = config.getEnvironments().get(firstNonNull(envName, config.getDefaultEnv()));
        final ResourceImpl ramToUse = new ResourceImpl(RamResourceType.ID, ramCalculator.calculate(environment), RamResourceType.UNIT);

        try (CloseableLock lock = resourcesLocks.acquiresLock(accountId)) {
            resourceUsageManager.checkResourcesAvailability(accountId, singletonList(ramToUse));
            return invocation.proceed();
        } catch (NoEnoughResourcesException e) {
            // starting of workspace requires only RAM resource
            final Resource requiredRam = e.getRequiredResources().get(0);
            final Resource availableRam = findRamResource(e.getAvailableResources());
            final Resource usedRam = findRamResource(resourceUsageManager.getUsedResources(accountId));

            throw new ConflictException(format("Workspace %s/%s needs %s to start. Your account has %s and %s in use. " +
                                               "The workspace can't be start. Stop other workspaces or grant more resources.",
                                               namespace,
                                               config.getName(),
                                               printResource(requiredRam),
                                               printResource(availableRam),
                                               printResource(usedRam)));
        }
    }

    private Resource findRamResource(List<? extends Resource> resources) {
        Optional<? extends Resource> ramOpt = resources.stream()
                                                       .filter(r -> r.getType().equals(RamResourceType.ID))
                                                       .findAny();
        if (ramOpt.isPresent()) {
            return ramOpt.get();
        } else {
            return new ResourceImpl(RamResourceType.ID, 0, RamResourceType.UNIT);
        }
    }

    private String printResource(Resource resource) {
        return resource.getAmount() + resource.getUnit().toUpperCase();
    }
}
