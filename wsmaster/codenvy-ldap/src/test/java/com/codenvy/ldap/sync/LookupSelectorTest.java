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
package com.codenvy.ldap.sync;

import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.codenvy.ldap.EmbeddedLdapServer;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests {@link LookupSelector}.
 *
 * @author Yevhenii Voevodin
 */
public class LookupSelectorTest {

  private EmbeddedLdapServer server;
  private ConnectionFactory connFactory;

  @BeforeClass
  public void setUpServer() throws Exception {
    (server = EmbeddedLdapServer.newDefaultServer()).start();

    connFactory = server.getConnectionFactory();

    // first 100 users have additional attributes
    for (int i = 0; i < 100; i++) {
      server.addDefaultLdapUser(
          i,
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
    final LookupSelector selector =
        new LookupSelector(
            10,
            30_000L,
            server.getBaseDn(),
            "(&(objectClass=inetOrgPerson)(givenName=*))",
            "uid",
            "givenName");
    try (Connection conn = connFactory.getConnection()) {
      conn.open();
      final Set<LdapEntry> selection =
          StreamSupport.stream(selector.select(conn).spliterator(), false).collect(toSet());
      assertEquals(selection.size(), 100);
      for (LdapEntry entry : selection) {
        assertNotNull(entry.getAttribute("givenName"));
        assertNotNull(entry.getAttribute("uid"));
      }
    }
  }
}
