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

        final Multibinder<String> binder = Multibinder.newSetBinder(binder(),
                                                                    String.class,
                                                                    Names.named(SystemDomain.SYSTEM_DOMAIN_ACTIONS));
        binder.addBinding().toInstance(OrganizationPermissionsFilter.MANAGE_ORGANIZATIONS_ACTION);
    }
}
