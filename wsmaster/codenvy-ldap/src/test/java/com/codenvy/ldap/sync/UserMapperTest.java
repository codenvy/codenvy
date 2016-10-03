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

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

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
