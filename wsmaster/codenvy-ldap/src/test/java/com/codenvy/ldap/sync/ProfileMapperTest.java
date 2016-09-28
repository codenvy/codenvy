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

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Tests {@link ProfileMapper}.
 *
 * @author Yevhenii Voevodin
 */
public class ProfileMapperTest {

    @Test
    public void testMappingFromLdapAttributesToProfile() throws Exception {
        final LdapEntry entry = new LdapEntry("uid=user123,dc=codenvy,dc=com");
        entry.addAttribute(new LdapAttribute("uid", "user123"));
        entry.addAttribute(new LdapAttribute("cn", "name"));
        entry.addAttribute(new LdapAttribute("mail", "user@codenvy.com"));
        entry.addAttribute(new LdapAttribute("sn", "LastName"));
        entry.addAttribute(new LdapAttribute("givenName", "FirstName"));
        entry.addAttribute(new LdapAttribute("telephoneNumber", "0123456789"));

        @SuppressWarnings("unchecked") // all the values are strings
        final ProfileMapper profileMapper = new ProfileMapper("uid", new Pair[] {Pair.of("lastName", "sn"),
                                                                                 Pair.of("firstName", "givenName"),
                                                                                 Pair.of("phone", "telephoneNumber")});

        final ProfileImpl profile = profileMapper.apply(entry);
        assertEquals(profile, new ProfileImpl("user123",
                                              ImmutableMap.of("lastName", "LastName",
                                                              "firstName", "FirstName",
                                                              "phone", "0123456789")));
    }
}
