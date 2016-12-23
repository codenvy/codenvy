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

import com.codenvy.resource.api.free.DefaultResourcesProvider;
import com.codenvy.resource.api.free.DefaultUserResourcesProvider;
import com.codenvy.resource.api.free.FreeResourcesLimitService;
import com.codenvy.resource.api.free.FreeResourcesLimitServicePermissionsFilter;
import com.codenvy.resource.api.free.FreeResourcesProvider;
import com.codenvy.resource.api.license.AccountLicenseService;
import com.codenvy.resource.api.license.LicenseServicePermissionsFilter;
import com.codenvy.resource.api.license.ResourcesProvider;
import com.codenvy.resource.api.usage.ResourceUsageService;
import com.codenvy.resource.api.usage.ResourceUsageServicePermissionsFilter;
import com.codenvy.resource.api.usage.ResourcesPermissionsChecker;
import com.codenvy.resource.api.usage.UserResourcesPermissionsChecker;
import com.codenvy.resource.api.usage.tracker.RamResourceUsageTracker;
import com.codenvy.resource.api.usage.tracker.RuntimeResourceUsageTracker;
import com.codenvy.resource.api.usage.tracker.WorkspaceResourceUsageTracker;
import com.codenvy.resource.model.ResourceType;
import com.codenvy.resource.spi.FreeResourcesLimitDao;
import com.codenvy.resource.spi.jpa.JpaFreeResourcesLimitDao;
import com.google.inject.AbstractModule;
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

        Multibinder.newSetBinder(binder(), DefaultResourcesProvider.class)
                   .addBinding().to(DefaultUserResourcesProvider.class);

        Multibinder.newSetBinder(binder(), ResourcesProvider.class)
                   .addBinding().to(FreeResourcesProvider.class);

        Multibinder<ResourceType> resourcesTypesBinder = Multibinder.newSetBinder(binder(), ResourceType.class);
        resourcesTypesBinder.addBinding().to(RamResourceType.class);
        resourcesTypesBinder.addBinding().to(WorkspaceResourceType.class);
        resourcesTypesBinder.addBinding().to(RuntimeResourceType.class);

        Multibinder.newSetBinder(binder(), ResourcesPermissionsChecker.class)
                   .addBinding().to(UserResourcesPermissionsChecker.class);

        Multibinder<ResourceUsageTracker> usageTrackersBinder = Multibinder.newSetBinder(binder(), ResourceUsageTracker.class);
        usageTrackersBinder.addBinding().to(RamResourceUsageTracker.class);
        usageTrackersBinder.addBinding().to(WorkspaceResourceUsageTracker.class);
        usageTrackersBinder.addBinding().to(RuntimeResourceUsageTracker.class);
    }
}
