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
package com.codenvy.organization.api;

import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.organization.api.permissions.OrganizationCreatorPermissionsProvider;
import com.codenvy.organization.api.permissions.OrganizationPermissionsFilter;
import com.codenvy.organization.api.permissions.OrganizationResourceDistributionServicePermissionsFilter;
import com.codenvy.organization.api.resource.DefaultOrganizationResourcesProvider;
import com.codenvy.organization.api.resource.OrganizationResourceLockKeyProvider;
import com.codenvy.organization.api.resource.OrganizationResourcesDistributionService;
import com.codenvy.organization.api.resource.OrganizationResourcesPermissionsChecker;
import com.codenvy.organization.api.resource.OrganizationResourcesReserveTracker;
import com.codenvy.organization.api.resource.SuborganizationResourcesProvider;
import com.codenvy.resource.api.ResourceLockKeyProvider;
import com.codenvy.resource.api.ResourcesReserveTracker;
import com.codenvy.resource.api.free.DefaultResourcesProvider;
import com.codenvy.resource.api.license.ResourcesProvider;
import com.codenvy.resource.api.usage.ResourcesPermissionsChecker;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * @author Sergii Leschenko
 */
public class OrganizationApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(OrganizationService.class);
        bind(OrganizationPermissionsFilter.class);

        bind(OrganizationCreatorPermissionsProvider.class).asEagerSingleton();

        Multibinder.newSetBinder(binder(),
                                 String.class,
                                 Names.named(SystemDomain.SYSTEM_DOMAIN_ACTIONS))
                   .addBinding().toInstance(OrganizationPermissionsFilter.MANAGE_ORGANIZATIONS_ACTION);

        Multibinder.newSetBinder(binder(), DefaultResourcesProvider.class)
                   .addBinding().to(DefaultOrganizationResourcesProvider.class);

        Multibinder.newSetBinder(binder(), ResourcesProvider.class)
                   .addBinding().to(SuborganizationResourcesProvider.class);

        Multibinder.newSetBinder(binder(), ResourcesReserveTracker.class)
                   .addBinding().to(OrganizationResourcesReserveTracker.class);

        Multibinder.newSetBinder(binder(), ResourceLockKeyProvider.class)
                   .addBinding().to(OrganizationResourceLockKeyProvider.class);

        Multibinder.newSetBinder(binder(), ResourcesPermissionsChecker.class)
                   .addBinding().to(OrganizationResourcesPermissionsChecker.class);

        bind(OrganizationResourcesDistributionService.class);
        bind(OrganizationResourceDistributionServicePermissionsFilter.class);
    }
}
