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

import com.codenvy.ldap.sync.LdapSynchronizer.SyncResult;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests {@link LdapSynchronizer}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class LdapSynchronizerTest {

    @Mock
    private ConnectionFactory connFactory;

    @Mock
    private Connection connection;

    @Mock
    private EntityManager entityManager;

    @Mock
    private LdapEntrySelector entrySelector;

    @Mock
    private UserDao userDao;

    @Mock
    private ProfileDao profileDao;

    private LdapSynchronizer synchronizer;
    private List<String>     existingIds;

    @BeforeMethod
    @SuppressWarnings("unchecked") // synchronizer generic array of string pairs
    public void setUp() throws LdapException {
        synchronizer = new LdapSynchronizer(connFactory,
                                            () -> entityManager,
                                            entrySelector,
                                            userDao,
                                            profileDao,
                                            null,
                                            new UserMapper("uid",
                                                                   "cn",
                                                                   "mail"),
                                            0,
                                            0,
                                            "uid",
                                            new Pair[] {Pair.of("firstName", "givenName")});

        // mocking existing ids
        final Query q = mock(Query.class);
        when(q.getResultList()).thenReturn(existingIds = new ArrayList<>());
        when(entityManager.createNativeQuery(anyString())).thenReturn(q);

        // mocking connection
        when(connFactory.getConnection()).thenReturn(connection);
    }

    @Test
    public void shouldCreateAllTheUsersWhenSynchronizedFirstTime() throws Exception {
        when(entrySelector.select(anyObject())).thenReturn(asList(createUserEntry("user123"),
                                                                  createUserEntry("user234"),
                                                                  createUserEntry("user345")));

        final SyncResult syncResult = synchronizer.syncAll();

        assertEquals(syncResult.getCreated(), 3);
        assertEquals(syncResult.getRemoved(), 0);
        assertEquals(syncResult.getRefreshed(), 0);
        verify(userDao, times(3)).create(anyObject());
        verify(profileDao, times(3)).create(anyObject());
    }

    @Test
    public void shouldRemoveThoseUsersWhichAreNotPresentInSelection() throws Exception {
        when(entrySelector.select(anyObject())).thenReturn(asList(createUserEntry("user123"),
                                                                  createUserEntry("user234")));
        existingIds.add("missed-in-selection");

        final SyncResult syncResult = synchronizer.syncAll();

        assertEquals(syncResult.getCreated(), 2);
        verify(userDao, times(2)).create(anyObject());
        verify(profileDao, times(2)).create(anyObject());
        assertEquals(syncResult.getRemoved(), 1);
        verify(userDao).remove("missed-in-selection");
        assertEquals(syncResult.getRefreshed(), 0);
    }

    @Test
    public void shouldUpdateUsersWhichAreAlreadyPresentInDatabase() throws Exception {
        when(entrySelector.select(anyObject())).thenReturn(asList(createUserEntry("user123"),
                                                                  createUserEntry("user234"),
                                                                  createUserEntry("user345")));
        existingIds.add("user123");
        existingIds.add("user345");
        existingIds.add("missing-in-selection");

        final SyncResult syncResult = synchronizer.syncAll();

        assertEquals(syncResult.getCreated(), 1);
        verify(userDao).create(anyObject());
        verify(profileDao).create(anyObject());
        assertEquals(syncResult.getRemoved(), 1);
        verify(userDao).remove("missing-in-selection");
        assertEquals(syncResult.getRefreshed(), 2);
        verify(userDao, times(2)).update(anyObject());
        verify(profileDao, times(2)).update(anyObject());
    }

    @Test(dataProvider = "identifiersSet")
    public void shouldNormalizeUserIdentifier(String raw, String normalized) throws Exception {
        when(entrySelector.select(anyObject())).thenReturn(singletonList(createUserEntry(raw)));

        synchronizer.syncAll();

        final ArgumentCaptor<UserImpl> captor = ArgumentCaptor.forClass(UserImpl.class);
        verify(userDao).create(captor.capture());
        assertEquals(captor.getValue().getId(), normalized);
    }

    @DataProvider
    public static Object[][] identifiersSet() {
        return new String[][] {
                {"{0000-1111-2222-3333-4444}", "0000-1111-2222-3333-4444"},
                {"(abc1234456789)01.01.2000", "abc123445678901012000"},
                {"abcd#efgh", "abcdefgh"},
                {"dot.separated.identifier", "dotseparatedidentifier"},
                {"A_Valid_Identifier-1234567890", "A_Valid_Identifier-1234567890"}
        };
    }

    private static LdapEntry createUserEntry(String id) {
        return createUserEntry(id, "name-" + id, "email" + id, "firstName-" + id);
    }

    private static LdapEntry createUserEntry(String id, String name, String email, String firstName) {
        final LdapEntry newEntry = new LdapEntry("uid=" + id + ",dc=codenvy,dc=com");
        newEntry.addAttribute(new LdapAttribute("uid", id));
        newEntry.addAttribute(new LdapAttribute("cn", name));
        newEntry.addAttribute(new LdapAttribute("mail", email));
        newEntry.addAttribute(new LdapAttribute("givenName", firstName));
        return newEntry;
    }
}
