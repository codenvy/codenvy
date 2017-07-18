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
package com.codenvy.ldap;

import com.codenvy.api.dao.authentication.AuthenticationHandler;
import com.codenvy.ldap.auth.AuthenticatorProvider;
import com.codenvy.ldap.auth.EntryResolverProvider;
import com.codenvy.ldap.auth.LdapAuthenticationHandler;
import com.codenvy.ldap.sync.LdapEntrySelector;
import com.codenvy.ldap.sync.LdapEntrySelectorProvider;
import com.codenvy.ldap.sync.LdapSynchronizer;
import com.codenvy.ldap.sync.LdapSynchronizerPermissionsFilter;
import com.codenvy.ldap.sync.LdapSynchronizerService;
import com.codenvy.ldap.sync.DBUserLinker;
import com.codenvy.ldap.sync.DBUserLinkerProvider;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.pool.PooledConnectionFactory;

/**
 * Binder for Ldap modules.
 *
 * @author Sergii Kabashniuk
 */
public class LdapModule extends AbstractModule {

    @Override
    protected void configure() {

        Multibinder<AuthenticationHandler> handlerBinder =
                Multibinder.newSetBinder(binder(), com.codenvy.api.dao.authentication.AuthenticationHandler.class);
        handlerBinder.addBinding().to(LdapAuthenticationHandler.class);

        bind(Authenticator.class).toProvider(AuthenticatorProvider.class);
        bind(ConnectionFactory.class).toProvider(LdapConnectionFactoryProvider.class);
        bind(PooledConnectionFactory.class).toProvider(LdapConnectionFactoryProvider.class);

        bind(EntryResolver.class).toProvider(EntryResolverProvider.class);

        bind(DBUserLinker.class).toProvider(DBUserLinkerProvider.class);
        bind(LdapEntrySelector.class).toProvider(LdapEntrySelectorProvider.class);
        bind(LdapSynchronizer.class).asEagerSingleton();
        bind(LdapSynchronizerService.class);
        bind(LdapSynchronizerPermissionsFilter.class);
        bind(DisablePasswordOperationsFilter.class);
    }
}
