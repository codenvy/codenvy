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
import org.ldaptive.LdapEntry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author Yevhenii Voevodin
 */
@Singleton
public class UserMapper implements Function<LdapEntry, UserImpl> {

    private final String idAttr;
    private final String nameAttr;
    private final String mailAttr;
    public static final Pattern NOT_VALID_ID_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9-_]");

    /**
     * @param userIdAttr
     *         ldap attribute indicating user identifier, it must be unique, otherwise
     *         synchronization will fail on user which has the same identifier.
     *         e.g. 'uid'
     * @param userNameAttr
     *         ldap attribute indicating user name, it must be unique, otherwise
     *         synchronization will fail on user which has the same name.
     *         e.g. 'cn'
     * @param userEmailAttr
     *         ldap attribute indicating user email, it must be unique, otherwise
     *         synchronization will fail on user which has the same email
     *         e.g. 'mail'
     */
    @Inject
    public UserMapper(@Named("ldap.sync.user.attr.id") String userIdAttr,
                      @Named("ldap.sync.user.attr.name") String userNameAttr,
                      @Named("ldap.sync.user.attr.email") String userEmailAttr) {
        this.idAttr = userIdAttr;
        this.nameAttr = userNameAttr;
        this.mailAttr = userEmailAttr;
    }

    public String[] getLdapEntryAttributeNames() {
        return new String[]{idAttr, nameAttr, mailAttr};
    }

    @Override
    public UserImpl apply(LdapEntry entry) {
        return new UserImpl(NOT_VALID_ID_CHARS_PATTERN.matcher(entry.getAttribute(idAttr).getStringValue()).replaceAll(""),
                            entry.getAttribute(mailAttr).getStringValue(),
                            entry.getAttribute(nameAttr).getStringValue());
    }
}
