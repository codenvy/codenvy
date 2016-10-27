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
package com.codenvy.ldap;

import com.codenvy.api.dao.authentication.AuthenticationHandler;
import com.codenvy.ldap.auth.AuthenticatorProvider;
import com.codenvy.ldap.auth.EntryResolverProvider;
import com.codenvy.ldap.auth.LdapAuthenticationHandler;
import com.codenvy.ldap.sync.LdapEntrySelector;
import com.codenvy.ldap.sync.LdapEntrySelectorProvider;
import com.codenvy.ldap.sync.LdapSynchronizer;
import com.codenvy.ldap.sync.LdapSynchronizerService;
import com.codenvy.ldap.sync.DBUserFinder;
import com.codenvy.ldap.sync.DBUserFinderProvider;
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

        bind(DBUserFinder.class).toProvider(DBUserFinderProvider.class);
        bind(LdapEntrySelector.class).toProvider(LdapEntrySelectorProvider.class);
        bind(LdapSynchronizer.class).asEagerSingleton();
        bind(LdapSynchronizerService.class);
    }
}
