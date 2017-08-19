/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.organization.api;

import com.codenvy.api.permission.server.SuperPrivilegesChecker;
import com.codenvy.api.permission.server.account.AccountPermissionsChecker;
import com.codenvy.api.permission.shared.model.PermissionsDomain;
import com.codenvy.organization.api.listener.MemberEventsPublisher;
import com.codenvy.organization.api.listener.OrganizationEventsWebsocketBroadcaster;
import com.codenvy.organization.api.listener.OrganizationNotificationEmailSender;
import com.codenvy.organization.api.listener.RemoveOrganizationOnLastUserRemovedEventSubscriber;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.organization.api.permissions.OrganizationPermissionsFilter;
import com.codenvy.organization.api.permissions.OrganizationResourceDistributionServicePermissionsFilter;
import com.codenvy.organization.api.permissions.OrganizationalAccountPermissionsChecker;
import com.codenvy.organization.api.resource.DefaultOrganizationResourcesProvider;
import com.codenvy.organization.api.resource.OrganizationResourceLockKeyProvider;
import com.codenvy.organization.api.resource.OrganizationResourcesDistributionService;
import com.codenvy.organization.api.resource.OrganizationalAccountAvailableResourcesProvider;
import com.codenvy.organization.api.resource.SuborganizationResourcesProvider;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.api.AvailableResourcesProvider;
import com.codenvy.resource.api.ResourceLockKeyProvider;
import com.codenvy.resource.api.free.DefaultResourcesProvider;
import com.codenvy.resource.api.license.ResourcesProvider;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/** @author Sergii Leschenko */
public class OrganizationApiModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(OrganizationService.class);
    bind(OrganizationPermissionsFilter.class);
    bind(RemoveOrganizationOnLastUserRemovedEventSubscriber.class).asEagerSingleton();

    Multibinder.newSetBinder(binder(), DefaultResourcesProvider.class)
        .addBinding()
        .to(DefaultOrganizationResourcesProvider.class);

    Multibinder.newSetBinder(binder(), ResourcesProvider.class)
        .addBinding()
        .to(SuborganizationResourcesProvider.class);

    MapBinder.newMapBinder(binder(), String.class, AvailableResourcesProvider.class)
        .addBinding(OrganizationImpl.ORGANIZATIONAL_ACCOUNT)
        .to(OrganizationalAccountAvailableResourcesProvider.class);

    Multibinder.newSetBinder(binder(), ResourceLockKeyProvider.class)
        .addBinding()
        .to(OrganizationResourceLockKeyProvider.class);

    Multibinder.newSetBinder(binder(), AccountPermissionsChecker.class)
        .addBinding()
        .to(OrganizationalAccountPermissionsChecker.class);

    bind(OrganizationResourcesDistributionService.class);
    bind(OrganizationResourceDistributionServicePermissionsFilter.class);

    bind(OrganizationEventsWebsocketBroadcaster.class).asEagerSingleton();
    bind(OrganizationNotificationEmailSender.class).asEagerSingleton();
    bind(MemberEventsPublisher.class).asEagerSingleton();

    Multibinder.newSetBinder(
            binder(),
            PermissionsDomain.class,
            Names.named(SuperPrivilegesChecker.SUPER_PRIVILEGED_DOMAINS))
        .addBinding()
        .to(OrganizationDomain.class);
  }
}
