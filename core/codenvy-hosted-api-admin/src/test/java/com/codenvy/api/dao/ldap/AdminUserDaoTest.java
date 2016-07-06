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

import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@Listeners(value = {MockitoTestNGListener.class})
public class AdminUserDaoTest extends BaseTest {

    UserLdapPagination        userLdapPagination;
    AdminUserDao              userDao;
    InitialLdapContextFactory factory;
    UserAttributesMapper      mapper;
    UserImpl[]                users;

    @BeforeMethod
    public void setUp() throws Exception {
        factory = spy(new InitialLdapContextFactory(() -> embeddedLdapServer.getUrl(),
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null));
        mapper = spy(new UserAttributesMapper());
        userLdapPagination = new UserLdapPagination("dc=codenvy;dc=com", mapper, factory);
        userDao = new AdminUserDaoImpl(factory,
                                       "dc=codenvy;dc=com",
                                       "uid",
                                       mapper,
                                       new EventService(),
                                       userLdapPagination);
        users = new UserImpl[] {
                new UserImpl("1", "user1@mail.com", "user1", "secret", singletonList("user1@mail.com")),
                new UserImpl("2", "user2@mail.com", "user2", "secret", singletonList("user2@mail.com")),
                new UserImpl("3", "user3@mail.com", "user3", "secret", singletonList("user3@mail.com"))
        };
        for (UserImpl user : users) {
            userDao.create(user);
        }
    }

    @Test
    public void getAllShouldReturnEmptyListIfNoMoreUsers() throws Exception {
        List<UserImpl> users = userDao.getAll(1, 4).getItems();
        assertTrue(users.isEmpty());
    }

    @Test
    public void getAllShouldReturnPageByPage() throws Exception {
        List<UserImpl> users = userDao.getAll(2, 0).getItems();
        assertEquals(users.size(), 2);
        assertEquals(users.get(0).getId(), "1");
        assertEquals(users.get(1).getId(), "2");

        users = userDao.getAll(2, 2).getItems();
        assertEquals(users.size(), 1);
        assertEquals(users.get(0).getId(), "3");

        users = userDao.getAll(2, 0).getItems();
        assertEquals(users.size(), 2);
        assertEquals(users.get(0).getId(), "1");
        assertEquals(users.get(1).getId(), "2");
    }

    @Test
    public void getAllShouldReturnAllUsersWithinSingleResponse() throws Exception {
        List<UserImpl> users = userDao.getAll(4, 0).getItems();
        assertEquals(users.size(), 3);
        assertEquals(users.get(0).getId(), "1");
        assertEquals(users.get(1).getId(), "2");
        assertEquals(users.get(2).getId(), "3");
    }

    @Test
    public void getAllShouldReturnRestUsers() throws Exception {
        List<UserImpl> users = userDao.getAll(3, 1).getItems();
        assertEquals(users.size(), 2);
        assertEquals(users.get(0).getId(), "2");
        assertEquals(users.get(1).getId(), "3");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void getAllShouldThrowIllegalArgumentExceptionIfMaxItemsWrong() throws Exception {
        userDao.getAll(-1, 5);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void getAllShouldThrowIllegalArgumentExceptionIfSkipCountWrong() throws Exception {
        userDao.getAll(2, -1);
    }

    @Test
    public void shouldReturnCorrectTotalCountAlongWithRequestedUsers() throws Exception {
        final Page<UserImpl> page = userDao.getAll(2, 0);

        assertEquals(page.getItems().size(), 2);
        assertEquals(page.getTotalItemsCount(), 3);
    }
}
