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

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests {@link DBUserLinker}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class UserFinderTest {

    @Mock
    private UserDao userDao;

    @Mock
    private DBHelper dbHelper;

    @BeforeMethod
    private void setUp() {
        System.out.println();
    }

    @Test(dataProvider = "findsIdsProvider")
    public void findsIds(BiFunction<UserDao, DBHelper, DBUserLinker> provider, String query) throws Exception {
        final DBUserLinker finder = provider.apply(userDao, dbHelper);
        final List<String> ids = Arrays.asList("id1", "id2");
        when(dbHelper.executeNativeQuery(query)).thenReturn(ids);

        assertEquals(finder.findIds(), new HashSet<>(ids));
    }

    @Test(dataProvider = "findsUserProvider")
    public void findsUser(BiFunction<UserDao, DBHelper, DBUserLinker> provider, BiConsumer<UserDao, UserImpl> mocker) throws Exception {
        final DBUserLinker finder = provider.apply(userDao, dbHelper);
        final UserImpl user = new UserImpl("id", "id", "id");
        mocker.accept(userDao, user);

        assertEquals(finder.findUser("id"), user);
    }

    @Test(dataProvider = "extractsIdsProvider")
    public void extractsIds(BiFunction<UserDao, DBHelper, DBUserLinker> provider, Function<UserImpl, String> idExtractor) {
        final DBUserLinker finder = provider.apply(userDao, dbHelper);
        final UserImpl user = new UserImpl("id", "email", "name");

        assertEquals(finder.extractId(user), idExtractor.apply(user));
    }

    @DataProvider
    private Object[][] findsIdsProvider() {
        return new Object[][] {
                {
                        (BiFunction<UserDao, DBHelper, DBUserLinker>)DBUserLinker::newIdLinker,
                        "SELECT id FROM Usr"
                },
                {
                        (BiFunction<UserDao, DBHelper, DBUserLinker>)DBUserLinker::newEmailLinker,
                        "SELECT email FROM Usr"
                },
                {
                        (BiFunction<UserDao, DBHelper, DBUserLinker>)DBUserLinker::newNameLinker,
                        "SELECT name FROM Usr"
                }
        };
    }

    @DataProvider
    private Object[][] findsUserProvider() {
        return new Object[][] {
                {
                        (BiFunction<UserDao, DBHelper, DBUserLinker>)DBUserLinker::newIdLinker,
                        (BiConsumer<UserDao, UserImpl>)(userDao, user) -> {
                            try {
                                when(userDao.getById(user.getId())).thenReturn(user);
                            } catch (Exception x) {
                                throw new RuntimeException(x);
                            }
                        }
                },
                {
                        (BiFunction<UserDao, DBHelper, DBUserLinker>)DBUserLinker::newEmailLinker,
                        (BiConsumer<UserDao, UserImpl>)(userDao, user) -> {
                            try {
                                when(userDao.getByEmail(user.getEmail())).thenReturn(user);
                            } catch (Exception x) {
                                throw new RuntimeException(x);
                            }
                        }
                },
                {
                        (BiFunction<UserDao, DBHelper, DBUserLinker>)DBUserLinker::newNameLinker,
                        (BiConsumer<UserDao, UserImpl>)(userDao, user) -> {
                            try {
                                when(userDao.getByName(user.getName())).thenReturn(user);
                            } catch (Exception x) {
                                throw new RuntimeException(x);
                            }
                        }
                }
        };
    }

    @DataProvider
    private Object[][] extractsIdsProvider() {
        return new Object[][] {
                {
                        (BiFunction<UserDao, DBHelper, DBUserLinker>)DBUserLinker::newIdLinker,
                        (Function<UserImpl, String>)UserImpl::getId
                },
                {
                        (BiFunction<UserDao, DBHelper, DBUserLinker>)DBUserLinker::newEmailLinker,
                        (Function<UserImpl, String>)UserImpl::getEmail
                },
                {
                        (BiFunction<UserDao, DBHelper, DBUserLinker>)DBUserLinker::newNameLinker,
                        (Function<UserImpl, String>)UserImpl::getName
                }
        };
    }
}
