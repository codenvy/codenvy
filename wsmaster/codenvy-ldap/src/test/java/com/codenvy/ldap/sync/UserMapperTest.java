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

import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.testng.annotations.Test;

/**
 * Tests {@link UserMapper}.
 *
 * @author Yevhenii Voevodin
 */
public class UserMapperTest {

  @Test
  public void testMappingFromLdapAttributesToUser() throws Exception {
    final LdapEntry entry = new LdapEntry("uid=user123,dc=codenvy,dc=com");
    entry.addAttribute(new LdapAttribute("uid", "user123"));
    entry.addAttribute(new LdapAttribute("cn", "name"));
    entry.addAttribute(new LdapAttribute("mail", "user@codenvy.com"));
    entry.addAttribute(new LdapAttribute("sn", "LastName"));

    final UserMapper userMapper = new UserMapper("uid", "cn", "mail");

    final UserImpl user = userMapper.apply(entry);
    assertEquals(user, new UserImpl("user123", "user@codenvy.com", "name"));
  }
}
