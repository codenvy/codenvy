/*
 *  [2012] - [2017] Codenvy, S.A.
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

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;

import javax.inject.Named;
import javax.inject.Singleton;

import java.util.ArrayList;

import static java.lang.String.format;

/**
 * Chooses a strategy of ldap entries selection based on configuration properties.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class LdapEntrySelectorProvider implements Provider<LdapEntrySelector> {

    private static final int  DEFAULT_PAGE_SIZE         = 1000;
    private static final long DEFAULT_PAGE_READ_TIMEOUT = 30_000L;

    private final LdapEntrySelector selector;

    @Inject
    public LdapEntrySelectorProvider(@Named("ldap.base_dn") String baseDn,
                                     @Named("ldap.sync.user.filter") String usersFilter,
                                     @Named("ldap.sync.user.additional_dn") @Nullable String additionalUserDn,
                                     @Named("ldap.sync.group.filter") @Nullable String groupFilter,
                                     @Named("ldap.sync.group.additional_dn") @Nullable String additionalGroupDn,
                                     @Named("ldap.sync.group.attr.members") @Nullable String membersAttrName,
                                     @Named("ldap.sync.page.size") int pageSize,
                                     @Named("ldap.sync.page.read_timeout_ms") long pageReadTimeoutMs,
                                     @Named("ldap.sync.profile.attrs") @Nullable Pair<String, String>[] profileAttributes,
                                     @Named("ldap.sync.user.attr.id") String userIdAttr,
                                     @Named("ldap.sync.user.attr.name") String userNameAttr,
                                     @Named("ldap.sync.user.attr.email") String userEmailAttr) {
        if (groupFilter != null && membersAttrName == null) {
            throw new NullPointerException(format("Value of 'ldap.group.filter' is set to '%s', which means that groups search " +
                                                  "is enabled that also requires 'ldap.group.attr.members' to be set",
                                                  groupFilter));
        }

        // getting attribute names which should be synchronized
        final ArrayList<String> attrsList = new ArrayList<>();
        attrsList.add(userIdAttr);
        attrsList.add(userNameAttr);
        attrsList.add(userEmailAttr);
        if (profileAttributes != null) {
            for (Pair<String, String> profileAttribute : profileAttributes) {
                attrsList.add(profileAttribute.second);
            }
        }
        final String[] syncAttributes = attrsList.toArray(new String[attrsList.size()]);

        if (groupFilter == null) {
            selector = new LookupSelector(pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize,
                                          pageReadTimeoutMs <= 0 ? DEFAULT_PAGE_READ_TIMEOUT : pageReadTimeoutMs,
                                          normalizeDn(additionalUserDn, baseDn),
                                          usersFilter,
                                          syncAttributes);
        } else {
            selector = new MembershipSelector(normalizeDn(additionalGroupDn, baseDn),
                                              groupFilter,
                                              usersFilter,
                                              membersAttrName,
                                              syncAttributes);
        }
    }

    @Override
    public LdapEntrySelector get() {
        return selector;
    }

    private static String normalizeDn(String additionalDn, String baseDn) {
        if (additionalDn == null) {
            return baseDn;
        }
        return additionalDn + ',' + baseDn;
    }
}
