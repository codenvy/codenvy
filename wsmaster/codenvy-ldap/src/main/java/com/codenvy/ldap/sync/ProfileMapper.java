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

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * Maps {@link LdapEntry} to {@link ProfileImpl}.
 *
 * @author Yevhenii Voevodin
 */
public class ProfileMapper implements Function<LdapEntry, ProfileImpl> {

    /** App attribute name -> ldap attribute name . */
    private final ImmutableMap<String, String> appToLdapAttrNames;
    private final String                       idAttr;

    public ProfileMapper(String idAttr, Pair<String, String>[] attributes) {
        this.idAttr = idAttr;
        if (attributes == null) {
            this.appToLdapAttrNames = ImmutableMap.of();
        } else {
            this.appToLdapAttrNames = ImmutableMap.copyOf(Arrays.stream(attributes)
                                                                .collect(toMap(p -> p.first, p -> p.second.toLowerCase())));
        }
    }

    @Override
    public ProfileImpl apply(LdapEntry entry) {
        final ProfileImpl profile = new ProfileImpl();
        profile.setUserId(entry.getAttribute(idAttr).getStringValue());
        for (Map.Entry<String, String> attrMapping : appToLdapAttrNames.entrySet()) {
            final LdapAttribute ldapAttr = entry.getAttribute(attrMapping.getValue());
            if (ldapAttr != null) {
                profile.getAttributes().put(attrMapping.getKey(), ldapAttr.getStringValue());
            }
        }
        return profile;
    }
}
