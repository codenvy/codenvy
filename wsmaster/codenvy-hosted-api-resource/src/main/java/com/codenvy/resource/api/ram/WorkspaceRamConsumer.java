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

import com.codenvy.resource.api.ResourceManager;
import com.codenvy.resource.spi.impl.ResourceImpl;
import com.google.common.annotations.VisibleForTesting;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.environment.server.EnvironmentParser;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.lang.Size;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Collections.singletonList;

/**
 * Intercepts {@link WorkspaceManager#startWorkspace(String, String, Boolean)}
 * and {@link WorkspaceManager#startWorkspace(WorkspaceConfig, String, boolean)}
 * and reserves RAM resource while workspace is starting.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class WorkspaceRamConsumer implements MethodInterceptor {
    private static final long BYTES_TO_MEGABYTES_DIVIDER = 1024L * 1024L;

    @Inject
    private ResourceManager  resourceManager;
    @Inject
    private WorkspaceManager workspaceManager;
    @Inject
    private AccountManager   accountManager;

    @VisibleForTesting
    @Inject
    EnvironmentParser environmentParser;

    @Inject
    @Named("che.workspace.default_memory_mb")
    @VisibleForTesting
    int defaultMachineMemorySizeMB;

    private Long defaultMachineMemorySizeBytes;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Object[] arguments = invocation.getArguments();

        WorkspaceConfigImpl config;
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
            config = (WorkspaceConfigImpl)arguments[0];
            namespace = (String)arguments[1];
        }

        final Environment environment = config.getEnvironments().get(firstNonNull(envName, config.getDefaultEnv()));
        final ResourceImpl ramToUse = new ResourceImpl(RamResourceType.ID, sumRam(environment), RamResourceType.UNIT);
        final Account account = accountManager.getByName(namespace);
        return resourceManager.reserveResources(account.getId(), singletonList(ramToUse), invocation::proceed);
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
                                          return getDefaultMachineMemorySizeBytes();
                                      } else {
                                          return value.getMemLimit();
                                      }
                                  })
                                  .sum();
        return sumBytes / BYTES_TO_MEGABYTES_DIVIDER;
    }

    public long getDefaultMachineMemorySizeBytes() {
        if (defaultMachineMemorySizeBytes != null) {
            return defaultMachineMemorySizeBytes;
        }
        return defaultMachineMemorySizeBytes = Size.parseSize(defaultMachineMemorySizeMB + "MB");
    }
}
