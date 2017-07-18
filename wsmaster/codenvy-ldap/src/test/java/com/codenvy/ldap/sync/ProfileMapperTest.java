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
