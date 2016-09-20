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

import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests {@link MembershipSelector};
 *
 * @author Yevhenii Voevodin
 */
public class MembershipSelectorTest {

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


        // first 100 users don't belong to any group
        for (int i = 0; i < 100; i++) {
            server.addDefaultLdapUser(i);
        }

        // next 200 users are members of group1/group2
        final List<String> group1Members = new ArrayList<>(100);
        final List<String> group2Members = new ArrayList<>(100);
        for (int i = 100; i < 300; i++) {
            final ServerEntry entry = server.addDefaultLdapUser(i, Pair.of("givenName", "gn-" + i));
            if (i % 2 == 0) {
                group1Members.add(entry.getDn().toString());
            } else {
                group2Members.add(entry.getDn().toString());
            }
            group1Members.add(entry.getDn().toString());
        }
        server.addDefaultLdapGroup("group1", group1Members);
        server.addDefaultLdapGroup("group2", group2Members);
    }

    @AfterClass
    public void shutdownServer() throws Exception {
        server.shutdown();
    }

    @Test
    public void testMembershipSelection() throws Exception {
        final MembershipSelector selector =
                new MembershipSelector(server.getBaseDn(),
                                       "(objectClass=groupOfNames)",
                                       "(objectClass=inetOrgPerson)",
                                       "member",
                                       "uid",
                                       "givenName");
        try (Connection conn = connFactory.getConnection()) {
            conn.open();
            final Set<LdapEntry> selection = StreamSupport.stream(selector.select(conn).spliterator(), false)
                                                          .collect(toSet());
            assertEquals(selection.size(), 200);
            for (LdapEntry entry : selection) {
                assertNotNull(entry.getAttribute("givenName"));
                assertNotNull(entry.getAttribute("uid"));
            }
        }
    }
}
