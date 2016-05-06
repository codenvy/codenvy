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
package com.codenvy.api.dao.ldap;

import com.codenvy.api.user.server.dao.AdminUserDao;
import com.google.inject.Inject;

import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import java.io.IOException;

import static com.google.api.client.repackaged.com.google.common.base.Preconditions.checkArgument;

/**
 * Ldap based implementation of the {@link AdminUserDao}.
 *
 * @author Anatoliy Bazko
 * @author Yevhenii Voevodin
 */
@Singleton
public class AdminUserDaoImpl extends UserDaoImpl implements AdminUserDao {

    private static final Logger   LOG         = LoggerFactory.getLogger(AdminUserDaoImpl.class);
    private static final String[] EMPTY_ARRAY = new String[0];

    private final String             objectClassFilter;
    private final UserLdapPagination userLdapPagination;

    @Inject
    public AdminUserDaoImpl(
                            UserProfileDao profileDao,
                            PreferenceDao preferenceDao,
                            InitialLdapContextFactory contextFactory,
                            @Named("user.ldap.user_container_dn") String userContainerDn,
                            @Named("user.ldap.user_dn") String userDn,
                            @Named("user.ldap.old_user_dn") String oldUserDn,
                            UserAttributesMapper userAttributesMapper,
                            EventService eventService,
                            UserLdapPagination userLdapPagination) {
        super(profileDao,
              preferenceDao,
              contextFactory,
              userContainerDn,
              userDn,
              oldUserDn,
              userAttributesMapper,
              eventService);
        this.userLdapPagination = userLdapPagination;
        final StringBuilder sb = new StringBuilder();
        for (String objectClass : userAttributesMapper.userObjectClasses) {
            sb.append("(objectClass=");
            sb.append(objectClass);
            sb.append(')');
        }
        this.objectClassFilter = sb.toString();
    }

    @Override
    public Page<User> getAll(int maxItems, int skipCount) throws ServerException, IllegalArgumentException {
        checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
        checkArgument(skipCount >= 0, "The number of items to skip can't be negative.");

        try {
            return new Page<>(userLdapPagination.get(maxItems, skipCount), skipCount, maxItems, getTotalUsersCount());
        } catch (NamingException | IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServerException("Unable get all users.", e);
        }
    }

    /**
     * Returns count of total entities by counting them,
     * the important thing is that {@link #EMPTY_ARRAY} is used for
     * fetching empty ldap entries which allows to get less information
     * in the {@code NamingEnumeration} response and speeds up counting.
     *
     * TODO: consider WeakReference holder for returned value.
     * as this operation may be expensive on medium/large data sets
     */
    private int getTotalUsersCount() throws ServerException {
        InitialLdapContext context = null;
        try {
            context = contextFactory.createContext();
            final SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(EMPTY_ARRAY);
            NamingEnumeration<SearchResult> searchRes = null;
            try {
                searchRes = context.search(containerDn, objectClassFilter, controls);
                int count = 0;
                while (searchRes.hasMore()) {
                    count++;
                    searchRes.next();
                }
                return count;
            } finally {
                if (searchRes != null) {
                    searchRes.close();
                }
            }
        } catch (NamingException x) {
            LOG.error(x.getLocalizedMessage(), x);
            throw new ServerException(x.getLocalizedMessage(), x);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException x) {
                    LOG.error(x.getLocalizedMessage(), x);
                }
            }
        }
    }
}
