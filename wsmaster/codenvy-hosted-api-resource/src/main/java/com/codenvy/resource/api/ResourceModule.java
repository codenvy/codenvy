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
package com.codenvy.resource.api;

import com.codenvy.resource.api.provider.FreeResourcesProvider;
import com.codenvy.resource.api.provider.ResourcesProvider;
import com.codenvy.resource.api.ram.RamResourceType;
import com.codenvy.resource.api.ram.RamResourceUsageTracker;
import com.codenvy.resource.api.ram.WorkspaceRamConsumer;
import com.codenvy.resource.model.ResourceType;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.workspace.server.WorkspaceManager;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;

/**
 * @author Sergii Leschenko
 */
public class ResourceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ResourceService.class);

        Multibinder<ResourcesProvider> resourceProviderBinder = Multibinder.newSetBinder(binder(), ResourcesProvider.class);
        resourceProviderBinder.addBinding().to(FreeResourcesProvider.class);

        Multibinder<ResourceType> resourcesTypesBinder = Multibinder.newSetBinder(binder(), ResourceType.class);
        resourcesTypesBinder.addBinding().to(RamResourceType.class);

        final WorkspaceRamConsumer workspaceRamConsumer = new WorkspaceRamConsumer();
        requestInjection(workspaceRamConsumer);
        bindInterceptor(subclassesOf(WorkspaceManager.class), names("startWorkspace"), workspaceRamConsumer);

        Multibinder<ResourceUsageTracker> usageTrackersBinder = Multibinder.newSetBinder(binder(), ResourceUsageTracker.class);
        usageTrackersBinder.addBinding().to(RamResourceUsageTracker.class);
    }
}
