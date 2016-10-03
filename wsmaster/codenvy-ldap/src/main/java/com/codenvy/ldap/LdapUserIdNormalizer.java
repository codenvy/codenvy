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
package com.codenvy.ldap;

import org.eclipse.che.commons.annotation.Nullable;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.regex.Pattern;

/**
 * Normalizes identifier to be compatible with system.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class LdapUserIdNormalizer {

    private static final Pattern NOT_VALID_ID_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9-_]");

    private final String idAttributeName;

    @Inject
    public LdapUserIdNormalizer(@Named("ldap.sync.user.attr.id") String idAttributeName) {
        this.idAttributeName = idAttributeName;
    }

    /**
     * Normalizes user identifier by modifying a {@link LdapEntry},
     * does nothing if the entry doesn't contain id attribute.
     *
     * @param entry
     *         the entry to normalize
     */
    public void normalize(@Nullable LdapEntry entry) {
        if (entry != null) {
            final LdapAttribute idAttr = entry.getAttribute(idAttributeName);
            if (idAttr != null) {
                final String normalizedId = normalize(idAttr.getStringValue());
                idAttr.clear();
                idAttr.addStringValue(normalizedId);
            }
        }
    }

    /**
     * Retrieves user identifier from the given {@code entry}
     * and returns a normalized value of it.
     *
     * @param entry
     *         the entry to retrieve id from
     * @return normalized id value or null if {@code entry} is null or id is missing from entry
     */
    @Nullable
    public String retrieveAndNormalize(@Nullable LdapEntry entry) {
        if (entry == null) {
            return null;
        }
        final LdapAttribute idAttr = entry.getAttribute(idAttributeName);
        if (idAttr == null) {
            return null;
        }
        return normalize(idAttr.getStringValue());
    }

    /**
     * Removes all the characters different from <i>a-zA-Z0-9-_</i>.
     *
     * @param id
     *         identifier to normalize
     * @return normalized identifier or null if {@code id} is null
     */
    @Nullable
    public String normalize(@Nullable String id) {
        if (id == null) {
            return null;
        }
        return NOT_VALID_ID_CHARS_PATTERN.matcher(id).replaceAll("");
    }

    /** Returns the name of user id attribute. */
    public String getIdAttributeName() {
        return idAttributeName;
    }
}
