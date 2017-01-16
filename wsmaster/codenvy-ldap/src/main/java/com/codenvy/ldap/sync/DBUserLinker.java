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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.spi.UserDao;

import java.util.HashSet;
import java.util.Set;

/**
 * Links db users with ldap users by either id, email or name.
 *
 * @author Yevhenii Voevodin
 */
public abstract class DBUserLinker {

    /** Creates a new user finder based on his identifier. */
    public static DBUserLinker newIdLinker(UserDao userDao, DBHelper dbHelper) {
        return new IdLinker(userDao, dbHelper);
    }

    /** Creates a new user finder based on his email. */
    public static DBUserLinker newEmailLinker(UserDao userDao, DBHelper dbHelper) {
        return new EmailLinker(userDao, dbHelper);
    }

    /** Creates a new user finder based on his name. */
    public static DBUserLinker newNameLinker(UserDao userDao, DBHelper dbHelper) {
        return new NameLinker(userDao, dbHelper);
    }

    protected final UserDao  userDao;
    protected final DBHelper dbHelper;

    protected DBUserLinker(UserDao userDao, DBHelper dbHelper) {
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
    public abstract String extractId(User ldapUser);

    /**
     * Finds user in persistence layer by specified identifier.
     *
     * @param linkingAttribute
     *         the value returned from {@link #extractId(User)}
     * @return user from persistence layer
     * @throws NotFoundException
     *         when there is no such user
     * @throws ServerException
     *         when any other error occurs
     */
    public abstract User findUser(String linkingAttribute) throws NotFoundException, ServerException;

    /** Returns linking attribute values for those users who exist in persistence layer. */
    public abstract Set<String> findIds();

    /** Retrieves user by his id. */
    private static class IdLinker extends DBUserLinker {

        private IdLinker(UserDao userDao, DBHelper dbHelper) {
            super(userDao, dbHelper);
        }

        @Override
        public String extractId(User ldapUser) {
            return ldapUser.getId();
        }

        @Override
        public User findUser(String id) throws NotFoundException, ServerException {
            return userDao.getById(id);
        }

        @Override
        @SuppressWarnings("unchecked") // user id is always string
        public Set<String> findIds() {
            return new HashSet<>(dbHelper.executeNativeQuery("SELECT id FROM Usr"));
        }
    }

    /** Retrieves user by his email. */
    private static class EmailLinker extends DBUserLinker {

        protected EmailLinker(UserDao userDao, DBHelper dbHelper) {
            super(userDao, dbHelper);
        }

        @Override
        public String extractId(User ldapUser) {
            return ldapUser.getEmail();
        }

        @Override
        public User findUser(String email) throws NotFoundException, ServerException {
            return userDao.getByEmail(email);
        }

        @Override
        @SuppressWarnings("unchecked") // user email is always string
        public Set<String> findIds() {
            return new HashSet<>(dbHelper.executeNativeQuery("SELECT email FROM Usr"));
        }
    }

    /** Links & retrieves user by his name. */
    private static class NameLinker extends DBUserLinker {

        protected NameLinker(UserDao userDao, DBHelper dbHelper) {
            super(userDao, dbHelper);
        }

        @Override
        public String extractId(User ldapUser) {
            return ldapUser.getName();
        }

        @Override
        public User findUser(String name) throws NotFoundException, ServerException {
            return userDao.getByName(name);
        }

        @Override
        @SuppressWarnings("unchecked") // user name is always string
        public Set<String> findIds() {
            return new HashSet<>(dbHelper.executeNativeQuery("SELECT name FROM Usr"));
        }
    }
}
