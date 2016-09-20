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

import com.codenvy.ldap.DefaultPropertiesModule;
import com.codenvy.ldap.MyLdapServer;
import com.codenvy.ldap.TestConnectionModule;
import com.google.inject.Injector;

import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests {@link LookupSelector}.
 *
 * @author Yevhenii Voevodin
 */
public class LookupSelectorTest {

    private MyLdapServer      server;
    private ConnectionFactory connFactory;

    @BeforeClass
    public void setUpServer() throws Exception {
        (server = MyLdapServer.newDefaultServer()).start();
        final Injector injector = com.google.inject.Guice
                .createInjector(new TestConnectionModule(),
                                new MyLdapServer.MyLdapModule(server),
                                new DefaultPropertiesModule());


        connFactory = injector.getInstance(ConnectionFactory.class);


        // first 100 users have additional attributes
        for (int i = 0; i < 100; i++) {
            server.addDefaultLdapUser(i,
                                      Pair.of("givenName", "test-user-first-name" + i),
                                      Pair.of("sn", "test-user-last-name"),
                                      Pair.of("telephoneNumber", "00000000" + i));
        }

        // next 100 users have only required attributes
        for (int i = 100; i < 200; i++) {
            server.addDefaultLdapUser(i);
        }
    }

    @AfterClass
    public void shutdownServer() throws Exception {
        server.shutdown();
    }

    @Test
    public void testLookupSelection() throws Exception {
        final LookupSelector selector = new LookupSelector(10,
                                                           30_000L,
                                                           server.getBaseDn(),
                                                           "(&(objectClass=inetOrgPerson)(givenName=*))",
                                                           "uid",
                                                           "givenName");
        try (Connection conn = connFactory.getConnection()) {
            conn.open();
            final Set<LdapEntry> selection = StreamSupport.stream(selector.select(conn).spliterator(), false)
                                                          .collect(toSet());
            assertEquals(selection.size(), 100);
            for (LdapEntry entry : selection) {
                assertNotNull(entry.getAttribute("givenName"));
                assertNotNull(entry.getAttribute("uid"));
            }
        }
    }
}
