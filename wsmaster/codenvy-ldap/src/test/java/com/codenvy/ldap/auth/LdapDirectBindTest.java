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
package com.codenvy.ldap.auth;

import com.codenvy.api.dao.authentication.AuthenticationHandler;
import com.codenvy.ldap.DefaultPropertiesModule;
import com.codenvy.ldap.LdapConnectionFactoryProvider;
import com.codenvy.ldap.MyLdapServer;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Names;

import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.apache.directory.shared.ldap.exception.LdapInvalidAttributeValueException;
import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.pool.PooledConnectionFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


public class LdapDirectBindTest {

    private static final String BASE_DN = "dc=codenvy,dc=com";

    private MyLdapServer               server;
    private LdapAuthenticationHandler  handler;
    private List<ServerEntry>          createdEntries;
    private List<Pair<String, String>> users;


    public static class LdapInitModule extends AbstractModule {


        @Override
        protected void configure() {
            Multibinder<AuthenticationHandler> handlerBinder =
                    Multibinder.newSetBinder(binder(), com.codenvy.api.dao.authentication.AuthenticationHandler.class);
            handlerBinder.addBinding().to(LdapAuthenticationHandler.class);

            bind(Authenticator.class).toProvider(AuthenticatorProvider.class);
            bind(PooledConnectionFactory.class).toProvider(LdapConnectionFactoryProvider.class);

            bind(EntryResolver.class).toProvider(EntryResolverProvider.class);
            bindConstant().annotatedWith(Names.named("ldap.auth.authentication_type")).to(AuthenticationType.DIRECT.toString());

            OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.auth.user.filter")))
                          .setBinding().toInstance("cn={user}");
            OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named("ldap.auth.dn_format")))
                          .setBinding().toInstance("uid=%s," + BASE_DN);

        }
    }


    @BeforeClass
    public void startServer() throws Exception {
        server = MyLdapServer.builder()
                             .setPartitionId("codenvy")
                             .setPartitionDn(BASE_DN)
                             .useTmpWorkingDir()
                             .setMaxSizeLimit(1000)
                             .build();
        server.start();
        final Injector injector = com.google.inject.Guice
                .createInjector(new LdapInitModule(),
                                new MyLdapServer.MyLdapModule(server),
                                new DefaultPropertiesModule());
        handler = injector.getInstance(LdapAuthenticationHandler.class);

        // create a set of users
        users = new ArrayList<>();
        createdEntries = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String password = NameGenerator.generate("pwd", 20);
            users.add(new Pair<>("id" + i, password));
            createdEntries.add(server.addDefaultLdapUser(i,
                                                         Pair.of("givenName", "test-user-first-name" + i),
                                                         Pair.of("sn", "test-user-last-name"),
                                                         Pair.of("userPassword", password.getBytes()),
                                                         Pair.of("telephoneNumber", "00000000" + i)));
        }

    }

    @AfterClass
    public void stopServer() throws Exception {
        server.shutdown();
    }

    @Test
    public void testAuthenticatedSearch() throws LdapInvalidAttributeValueException, AuthenticationException {
        for (Pair<String, String> pair : users) {
            handler.authenticate(pair.first, pair.second);
        }
    }

    @Test(expectedExceptions = AuthenticationException.class, expectedExceptionsMessageRegExp = "Authentication failed. Please check username and password.")
    public void testWrongPassword() throws LdapInvalidAttributeValueException, AuthenticationException {
        handler.authenticate("name1", "nwrongpass");
    }

    @Test(expectedExceptions = AuthenticationException.class, expectedExceptionsMessageRegExp = "Authentication failed. Please check username and password.")
    public void testWrongUser() throws LdapInvalidAttributeValueException, AuthenticationException {
        handler.authenticate("name23431", "nwrongpass");
    }

}
