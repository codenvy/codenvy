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

import org.eclipse.che.api.user.server.spi.UserDao;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * Provides {@link DBUserLinker} instances based on configuration.
 *
 * @author Yevhenii Voevodin
 */
public class DBUserLinkerProvider implements Provider<DBUserLinker> {

    @Inject
    private UserDao userDao;

    @Inject
    private DBHelper dbHelper;

    @com.google.inject.Inject(optional = true)
    @Named("ldap.sync.user_linking_attribute")
    private String linkAttr;

    @Override
    public DBUserLinker get() {
        if (linkAttr == null || linkAttr.equals("id")) {
            return DBUserLinker.newIdLinker(userDao, dbHelper);
        }
        if (linkAttr.equals("email")) {
            return DBUserLinker.newEmailLinker(userDao, dbHelper);
        }
        if (linkAttr.equals("name")) {
            return DBUserLinker.newNameLinker(userDao, dbHelper);
        }
        throw new IllegalStateException("Supported values for the property 'ldap.sync.user_linking_attribute' " +
                                        "are 'id', 'email' or 'name");
    }
}
