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
package com.codenvy.ldap.sync;

import com.codenvy.ldap.EmbeddedLdapServer;
import com.codenvy.ldap.sync.LdapSynchronizer.SyncResult;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.util.Providers;

import org.apache.directory.shared.ldap.entry.Modification;
import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.apache.directory.shared.ldap.entry.client.ClientModification;
import org.apache.directory.shared.ldap.entry.client.DefaultClientAttribute;
import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.jpa.JpaProfileDao;
import org.eclipse.che.api.user.server.jpa.UserJpaModule;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.ConnectionFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.apache.directory.shared.ldap.entry.ModificationOperation.REPLACE_ATTRIBUTE;
import static org.testng.Assert.assertEquals;

/**
 * Tests synchronization with database using real database.
 *
 * @author Yevhenii Voevodin
 */
public class LdapSynchronizationFlowTest {

    private LdapSynchronizer   synchronizer;
    private UserDao            userDao;
    private EmbeddedLdapServer server;

    @BeforeClass
    public void setUp(ITestContext context) throws Exception {
        (server = EmbeddedLdapServer.newDefaultServer()).start();
        final Injector injector = Guice.createInjector(new Module(server));
        synchronizer = injector.getInstance(LdapSynchronizer.class);
        userDao = injector.getInstance(UserDao.class);
        injector.getInstance(JpaProfileDao.RemoveProfileBeforeUserRemovedEventSubscriber.class).subscribe();
    }

    @AfterClass
    public void cleanUp() throws Exception {
        server.shutdown();
        final long totalCount = (int)userDao.getTotalCount();
        if (totalCount > 0) {
            for (UserImpl user : userDao.getAll((int)totalCount, 0).getItems()) {
                userDao.remove(user.getId());
            }
        }
    }

    @Test
    public void synchronization() throws Exception {
        // add a few users to ldap
        final UserImpl user1 = asUser(server.addDefaultLdapUser(1));
        final UserImpl user2 = asUser(server.addDefaultLdapUser(2));
        final UserImpl user3 = asUser(server.addDefaultLdapUser(3));

        // sync the first time, check all the users synchronized
        SyncResult syncResult = synchronizer.syncAll();
        assertEquals(syncResult.getCreated(), 3);
        assertEquals(syncResult.getUpdated(), 0);
        assertEquals(syncResult.getRemoved(), 0);
        assertEquals(syncResult.getFailed(), 0);
        assertEquals(userDao.getTotalCount(), 3);
        assertEquals(userDao.getAll(3, 0).fill(new HashSet<>()), new HashSet<>(asList(user1, user2, user3)));

        // add a new ldap user
        final UserImpl user4 = asUser(server.addDefaultLdapUser(4));

        // sync the second time
        syncResult = synchronizer.syncAll();
        assertEquals(syncResult.getCreated(), 1);
        assertEquals(syncResult.getUpToDate(), 3);
        assertEquals(syncResult.getUpdated(), 0);
        assertEquals(syncResult.getRemoved(), 0);
        assertEquals(syncResult.getFailed(), 0);
        assertEquals(user4, userDao.getById(user4.getId()));

        // remove user from ldap
        server.removeEntry("uid", user1.getId());

        // sync and check user is removed from database
        syncResult = synchronizer.syncAll();
        assertEquals(syncResult.getCreated(), 0);
        assertEquals(syncResult.getUpToDate(), 3);
        assertEquals(syncResult.getUpdated(), 0);
        assertEquals(syncResult.getRemoved(), 1);
        assertEquals(syncResult.getFailed(), 0);
        assertEquals(userDao.getAll(3, 0).fill(new HashSet<>()), new HashSet<>(asList(user2, user3, user4)));

        // modify ldap user
        server.modify("uid", user2.getId(), replaceMod("cn", "new-name"));

        // sync and check user is updated
        syncResult = synchronizer.syncAll();
        assertEquals(syncResult.getCreated(), 0);
        assertEquals(syncResult.getUpToDate(), 2);
        assertEquals(syncResult.getUpdated(), 1);
        assertEquals(syncResult.getRemoved(), 0);
        assertEquals(syncResult.getFailed(), 0);

        // cleanup ldap user entries
        server.removeDefaultUser(user2.getId());
        server.removeDefaultUser(user3.getId());
        server.removeDefaultUser(user4.getId());
    }

    @Test(dependsOnMethods = "synchronization")
    public void conflictUpdateIsSolvedByTwoSynchronizations() throws Exception {
        // add a few users to ldap
        final UserImpl user1 = asUser(server.addDefaultLdapUser(1));
        final UserImpl user2 = asUser(server.addDefaultLdapUser(2));
        final UserImpl user3 = asUser(server.addDefaultLdapUser(3));

        // sync first time
        synchronizer.syncAll();

        // make conflict modification
        server.modify("uid", user2.getId(), replaceMod("cn", user1.getName()));
        server.removeDefaultUser(user1.getId());
        user2.setName(user1.getName());

        // should fail to sync one of users, due to name conflict
        SyncResult syncResult = synchronizer.syncAll();
        assertEquals(syncResult.getCreated(), 0);
        assertEquals(syncResult.getUpdated(), 0);
        assertEquals(syncResult.getUpToDate(), 1);
        assertEquals(syncResult.getRemoved(), 1);
        assertEquals(syncResult.getFailed(), 1);

        // sync second time, and check that failed synchronization is resolved
        syncResult = synchronizer.syncAll();
        assertEquals(syncResult.getCreated(), 0);
        assertEquals(syncResult.getUpToDate(), 1);
        assertEquals(syncResult.getUpdated(), 1);
        assertEquals(syncResult.getRemoved(), 0);
        assertEquals(syncResult.getFailed(), 0);
        assertEquals(userDao.getAll(2, 0).fill(new HashSet<>()), new HashSet<>(asList(user2, user3)));


        // cleanup ldap user entries
        server.removeDefaultUser(user2.getId());
        server.removeDefaultUser(user3.getId());
    }

    private static Modification replaceMod(String attrName, String newValue) {
        return new ClientModification(REPLACE_ATTRIBUTE, new DefaultClientAttribute(attrName, newValue));
    }

    private static UserImpl asUser(ServerEntry entry) {
        return new UserImpl(entry.get("uid").get(0).toString(),
                            entry.get("mail").get(0).toString(),
                            entry.get("cn").get(0).toString());
    }

    public static class Module extends AbstractModule {

        private final EmbeddedLdapServer server;

        public Module(EmbeddedLdapServer server) {
            this.server = server;
        }

        @Override
        protected void configure() {
            // configure dependencies
            bind(EventService.class).in(Singleton.class);
            bind(JpaInitializer.class).asEagerSingleton();
            bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();

            install(new JpaPersistModule("test"));
            install(new UserJpaModule());

            // configure synchronizer
            bind(LdapEntrySelector.class).toProvider(LdapEntrySelectorProvider.class);
            bind(DBUserFinder.class).toProvider(DBUserFinderProvider.class);
            bind(ConnectionFactory.class).toInstance(server.getConnectionFactory());
            bindConstant().annotatedWith(Names.named("ldap.sync.initial_delay_ms")).to(0L);
            bindConstant().annotatedWith(Names.named("ldap.sync.period_ms")).to(-1L);
            bindConstant().annotatedWith(Names.named("ldap.sync.user.attr.email")).to("mail");
            bindConstant().annotatedWith(Names.named("ldap.sync.user.attr.id")).to("uid");
            bindConstant().annotatedWith(Names.named("ldap.sync.user.attr.name")).to("cn");
            bindConstant().annotatedWith(Names.named("ldap.sync.page.size")).to(10);
            bindConstant().annotatedWith(Names.named("ldap.sync.page.read_timeout_ms")).to(30_000L);
            bindConstant().annotatedWith(Names.named("ldap.sync.remove_if_missing")).to(true);
            bindConstant().annotatedWith(Names.named("ldap.sync.update_if_exists")).to(true);
            bindConstant().annotatedWith(Names.named("ldap.base_dn")).to(server.getBaseDn());
            bindConstant().annotatedWith(Names.named("ldap.sync.user.filter")).to("(objectClass=inetOrgPerson)");
            bind(String.class).annotatedWith(Names.named("ldap.sync.group.additional_dn")).toProvider(Providers.of(null));
            bind(String.class).annotatedWith(Names.named("ldap.sync.group.filter")).toProvider(Providers.of(null));
            bind(String.class).annotatedWith(Names.named("ldap.sync.group.attr.members")).toProvider(Providers.of(null));
            bind(String.class).annotatedWith(Names.named("ldap.sync.user.additional_dn")).toProvider(Providers.of(null));
            @SuppressWarnings("unchecked") // all the pairs are (string, string) pairs
            final Pair<String, String>[] attributes = new Pair[] {
                    Pair.of("firstName", "giveName"),
                    Pair.of("lastName", "sn"),
                    Pair.of("telephoneNumber", "phone")
            };
            bind(new TypeLiteral<Pair<String, String>[]>() {}).annotatedWith(Names.named("ldap.sync.profile.attrs"))
                                                              .toInstance(attributes);

        }
    }
}
