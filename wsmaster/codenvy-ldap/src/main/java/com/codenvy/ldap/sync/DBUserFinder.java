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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.spi.UserDao;

import java.util.HashSet;
import java.util.Set;

/**
 * Retrieves user from persistence layer.
 *
 * @author Yevhenii Voevodin
 */
public abstract class DBUserFinder {

    /** Creates a new user finder based on his identifier. */
    public static DBUserFinder newIdFinder(UserDao userDao, DBHelper dbHelper) {
        return new ByIdUserFinder(userDao, dbHelper);
    }

    /** Creates a new user finder based on his email. */
    public static DBUserFinder newEmailFinder(UserDao userDao, DBHelper dbHelper) {
        return new ByEmailUserFinder(userDao, dbHelper);
    }

    protected final UserDao  userDao;
    protected final DBHelper dbHelper;

    protected DBUserFinder(UserDao userDao, DBHelper dbHelper) {
        this.userDao = userDao;
        this.dbHelper = dbHelper;
    }

    /**
     * Gets user linking identifier from ldap user.
     *
     * @param ldapUser
     *         user retrieved from ldap
     * @return an identifier
     */
    public abstract String extractLinkingId(User ldapUser);

    /**
     * Finds user in persistence layer by specified identifier.
     *
     * @param linkingAttribute
     *         the value returned from {@link #extractLinkingId(User)}
     * @return user from persistence layer
     * @throws NotFoundException
     *         when there is no such user
     * @throws ServerException
     *         when any other error occurs
     */
    public abstract User findOne(String linkingAttribute) throws NotFoundException, ServerException;

    /** Returns linking attribute values for those users who exist in persistence layer. */
    public abstract Set<String> findLinkingIds();

    /** Retrieves user by his id. */
    private static class ByIdUserFinder extends DBUserFinder {

        private ByIdUserFinder(UserDao userDao, DBHelper dbHelper) {
            super(userDao, dbHelper);
        }

        @Override
        public String extractLinkingId(User ldapUser) {
            return ldapUser.getId();
        }

        @Override
        public User findOne(String id) throws NotFoundException, ServerException {
            return userDao.getById(id);
        }

        @Override
        @SuppressWarnings("unchecked") // user id is always string
        public Set<String> findLinkingIds() {
            return new HashSet<>(dbHelper.executeNativeQuery("SELECT id FROM Usr"));
        }
    }

    /** Retrieves user by his email. */
    private static class ByEmailUserFinder extends DBUserFinder {

        protected ByEmailUserFinder(UserDao userDao, DBHelper dbHelper) {
            super(userDao, dbHelper);
        }

        @Override
        public String extractLinkingId(User ldapUser) {
            return ldapUser.getEmail();
        }

        @Override
        public User findOne(String email) throws NotFoundException, ServerException {
            return userDao.getByEmail(email);
        }

        @Override
        @SuppressWarnings("unchecked") // user email is always string
        public Set<String> findLinkingIds() {
            return new HashSet<>(dbHelper.executeNativeQuery("SELECT email FROM Usr"));
        }
    }
}
