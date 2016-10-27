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

import org.eclipse.che.api.user.server.spi.UserDao;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Provides {@link DBUserFinder} instances based on configuration.
 *
 * @author Yevhenii Voevodin
 */
public class DBUserFinderProvider implements Provider<DBUserFinder> {

    @Inject
    private UserDao userDao;

    @Inject
    private DBHelper dbHelper;

    @com.google.inject.Inject(optional = true)
    @Named("ldap.sync.user_linking_attribute")
    private String linkAttr;

    @Override
    public DBUserFinder get() {
        if (linkAttr == null || linkAttr.equals("id")) {
            return DBUserFinder.newIdFinder(userDao, dbHelper);
        }
        if (linkAttr.equals("email")) {
            return DBUserFinder.newEmailFinder(userDao, dbHelper);
        }
        throw new IllegalStateException("Supported values for the property 'ldap.sync.user_linking_attribute' are 'id' or 'email'");
    }
}
