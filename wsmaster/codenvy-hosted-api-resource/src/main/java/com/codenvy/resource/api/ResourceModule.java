/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.resource.api;

import com.codenvy.resource.api.free.FreeResourcesLimitService;
import com.codenvy.resource.api.free.FreeResourcesLimitServicePermissionsFilter;
import com.codenvy.resource.api.free.FreeResourcesProvider;
import com.codenvy.resource.api.license.AccountLicenseService;
import com.codenvy.resource.api.license.LicenseServicePermissionsFilter;
import com.codenvy.resource.api.license.ResourcesProvider;
import com.codenvy.resource.api.type.RamResourceType;
import com.codenvy.resource.api.type.ResourceType;
import com.codenvy.resource.api.type.RuntimeResourceType;
import com.codenvy.resource.api.type.TimeoutResourceType;
import com.codenvy.resource.api.type.WorkspaceResourceType;
import com.codenvy.resource.api.usage.ResourceUsageService;
import com.codenvy.resource.api.usage.ResourceUsageServicePermissionsFilter;
import com.codenvy.resource.api.usage.tracker.RamResourceUsageTracker;
import com.codenvy.resource.api.usage.tracker.RuntimeResourceUsageTracker;
import com.codenvy.resource.api.usage.tracker.WorkspaceResourceUsageTracker;
import com.codenvy.resource.spi.FreeResourcesLimitDao;
import com.codenvy.resource.spi.jpa.JpaFreeResourcesLimitDao;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Sergii Leschenko
 */
public class ResourceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ResourceUsageService.class);
        bind(ResourceUsageServicePermissionsFilter.class);

        bind(AccountLicenseService.class);
        bind(LicenseServicePermissionsFilter.class);

        bind(FreeResourcesLimitService.class);
        bind(FreeResourcesLimitDao.class).to(JpaFreeResourcesLimitDao.class);
        bind(JpaFreeResourcesLimitDao.RemoveFreeResourcesLimitSubscriber.class).asEagerSingleton();
        bind(FreeResourcesLimitServicePermissionsFilter.class);

        Multibinder.newSetBinder(binder(), ResourcesProvider.class)
                   .addBinding().to(FreeResourcesProvider.class);

        MapBinder.newMapBinder(binder(), String.class, AvailableResourcesProvider.class);

        Multibinder<ResourceType> resourcesTypesBinder = Multibinder.newSetBinder(binder(), ResourceType.class);
        resourcesTypesBinder.addBinding().to(RamResourceType.class);
        resourcesTypesBinder.addBinding().to(WorkspaceResourceType.class);
        resourcesTypesBinder.addBinding().to(RuntimeResourceType.class);
        resourcesTypesBinder.addBinding().to(TimeoutResourceType.class);

        Multibinder<ResourceUsageTracker> usageTrackersBinder = Multibinder.newSetBinder(binder(), ResourceUsageTracker.class);
        usageTrackersBinder.addBinding().to(RamResourceUsageTracker.class);
        usageTrackersBinder.addBinding().to(WorkspaceResourceUsageTracker.class);
        usageTrackersBinder.addBinding().to(RuntimeResourceUsageTracker.class);
    }
}
