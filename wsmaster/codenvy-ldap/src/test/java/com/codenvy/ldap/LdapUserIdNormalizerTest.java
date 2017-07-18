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
package com.codenvy.ldap;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests {@link LdapUserIdNormalizer}.
 *
 * @author Yevhenii Voevodin
 */
public class LdapUserIdNormalizerTest {

    private final LdapUserIdNormalizer idNormalizer = new LdapUserIdNormalizer("uid");

    @Test(dataProvider = "identifiersSet")
    public void shouldNormalizeUserIdentifier(String raw, String normalized) {
        assertEquals(idNormalizer.normalize(raw), normalized);
    }

    @Test
    public void shouldModifyLdapEntryByNormalizingIdAttribute() {
        final String id = "{123}";
        final LdapEntry entry = new LdapEntry("uid=user123", new LdapAttribute("uid", id));

        idNormalizer.normalize(entry);

        assertEquals(entry.getAttribute("uid").getStringValue(), "123");
    }

    @Test
    public void shouldRetrieveIdValueAndNormalizeIt() {
        final String id = "{123}";
        final LdapEntry entry = new LdapEntry("uid=user123", new LdapAttribute("uid", id));

        final String normalizedId = idNormalizer.retrieveAndNormalize(entry);

        assertEquals(normalizedId, "123");
        assertEquals(entry.getAttribute("uid").getStringValue(), "{123}");
    }

    @DataProvider
    public static Object[][] identifiersSet() {
        return new String[][] {
                {"{0000-1111-2222-3333-4444}", "0000-1111-2222-3333-4444"},
                {"(abc1234456789)01.01.2000", "abc123445678901012000"},
                {"abcd#efgh", "abcdefgh"},
                {"dot.separated.identifier", "dotseparatedidentifier"},
                {"A_Valid_Identifier-1234567890", "A_Valid_Identifier-1234567890"}
        };
    }
}
