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

import com.codenvy.ldap.LdapUserIdNormalizer;
import com.codenvy.ldap.sync.LdapSynchronizer.SyncResult;
import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    private LdapUserIdNormalizer idNormalizer;

    @Mock
    private UserDao userDao;

    @Mock
    private DBUserFinder userFinder;

    @Mock
    private ProfileDao profileDao;

    private LdapSynchronizer synchronizer;
    private Set<String>      existingIds;

    @BeforeMethod
    @SuppressWarnings("unchecked") // synchronizer generic array of string pairs
    public void setUp() throws Exception {
        synchronizer = new LdapSynchronizer(connFactory,
                                            entrySelector,
                                            userDao,
                                            profileDao,
                                            idNormalizer,
                                            null,
                                            0,
                                            0,
                                            "uid",
                                            "cn",
                                            "mail",
                                            new Pair[] {Pair.of("firstName", "givenName")},
                                            true,
                                            true,
                                            userFinder);

        // mocking existing ids
        existingIds = new HashSet<>();
        when(userFinder.findLinkingIds()).thenReturn(existingIds);
        when(userFinder.extractLinkingId(any())).thenAnswer(inv -> ((User)inv.getArguments()[0]).getId());

        // mocking connection
        when(connFactory.getConnection()).thenReturn(connection);
    }

    @Test
    public void createsAllTheUsersWhenSynchronizedFirstTime() throws Exception {
        when(entrySelector.select(anyObject())).thenReturn(asList(createUserEntry("user123"),
                                                                  createUserEntry("user234"),
                                                                  createUserEntry("user345")));

        final SyncResult syncResult = synchronizer.syncAll();

        assertEquals(syncResult.getProcessed(), 3);
        assertEquals(syncResult.getCreated(), 3);
        assertEquals(syncResult.getRemoved(), 0);
        assertEquals(syncResult.getUpdated(), 0);
        assertEquals(syncResult.getUpToDate(), 0);
        assertEquals(syncResult.getRemoved(), 0);
        assertEquals(syncResult.getSkipped(), 0);
        verify(userDao, times(3)).create(anyObject());
        verify(profileDao, times(3)).create(anyObject());
    }

    @Test
    public void removesThoseUsersWhoAreNotPresentInSelection() throws Exception {
        final Map<String, LdapEntry> users = new HashMap<>();
        users.put("user123", createUserEntry("user123"));
        users.put("user234", createUserEntry("user234"));
        when(entrySelector.select(anyObject())).thenReturn(users.values());

        existingIds.add("missed-in-selection");
        when(userFinder.findOne("missed-in-selection")).thenReturn(new UserImpl("missed-in-selection", "email", "name"));

        final SyncResult syncResult = synchronizer.syncAll();

        assertEquals(syncResult.getProcessed(), 2);
        assertEquals(syncResult.getCreated(), 2);
        assertEquals(syncResult.getRemoved(), 1);
        assertEquals(syncResult.getUpdated(), 0);
        assertEquals(syncResult.getUpToDate(), 0);
        assertEquals(syncResult.getFailed(), 0);
        assertEquals(syncResult.getSkipped(), 0);
        verify(userDao, times(2)).create(anyObject());
        verify(profileDao, times(2)).create(anyObject());
        verify(userDao).remove("missed-in-selection");
    }

    @Test
    public void skipsUsersWhoAreAlreadyPresentInDatabaseAndNotModified() throws Exception {
        final Map<String, LdapEntry> users = new HashMap<>();
        users.put("user123", createUserEntry("user123"));
        users.put("user234", createUserEntry("user234"));
        users.put("user345", createUserEntry("user345"));
        when(entrySelector.select(anyObject())).thenReturn(users.values());
        when(profileDao.getById(any())).thenAnswer(inv -> {
            final String id = inv.getArguments()[0].toString();
            return new ProfileImpl(id, ImmutableMap.of("firstName", "firstName-" + id));
        });

        final UserMapper mapper = new UserMapper("uid", "cn", "mail");
        when(userFinder.findOne(any())).thenAnswer(inv -> {
            final String id = inv.getArguments()[0].toString();
            return mapper.apply(users.get(id));
        });

        existingIds.add("user123");
        existingIds.add("user345");

        final SyncResult syncResult = synchronizer.syncAll();

        assertEquals(syncResult.getProcessed(), 3);
        assertEquals(syncResult.getCreated(), 1);
        assertEquals(syncResult.getRemoved(), 0);
        assertEquals(syncResult.getUpToDate(), 2);
        assertEquals(syncResult.getUpdated(), 0);
        assertEquals(syncResult.getFailed(), 0);
        assertEquals(syncResult.getSkipped(), 0);
        verify(userDao).create(anyObject());
        verify(profileDao).create(anyObject());
        verify(userDao, never()).update(anyObject());
        verify(profileDao, never()).update(anyObject());
    }

    @Test
    public void updatesUsersWhoChangedInLdap() throws Exception {
        final Map<String, LdapEntry> users = new HashMap<>();
        users.put("user123", createUserEntry("user123"));
        users.put("user234", createUserEntry("user234"));
        when(entrySelector.select(anyObject())).thenReturn(users.values());

        when(profileDao.getById(any())).thenAnswer(inv -> {
            final String id = inv.getArguments()[0].toString();
            return new ProfileImpl(id, ImmutableMap.of("firstName", "new-firstName-" + id));
        });

        final UserMapper mapper = new UserMapper("uid", "cn", "mail");
        when(userFinder.findOne(any())).thenAnswer(inv -> {
            final String id = inv.getArguments()[0].toString();
            return mapper.apply(users.get(id));
        });

        existingIds.add("user123");
        existingIds.add("user234");

        final SyncResult syncResult = synchronizer.syncAll();

        assertEquals(syncResult.getProcessed(), 2);
        assertEquals(syncResult.getCreated(), 0);
        assertEquals(syncResult.getRemoved(), 0);
        assertEquals(syncResult.getUpToDate(), 0);
        assertEquals(syncResult.getUpdated(), 2);
        assertEquals(syncResult.getFailed(), 0);
        assertEquals(syncResult.getSkipped(), 0);
        verify(userDao, never()).update(anyObject());
        verify(profileDao, times(2)).update(anyObject());
    }

    @Test
    public void skipsUsersIfTheyAlreadyExistAndUpdateIfExistsAttributeIsSetToFalse() throws Exception {
        synchronizer = new LdapSynchronizer(connFactory,
                                            entrySelector,
                                            userDao,
                                            profileDao,
                                            idNormalizer,
                                            null,
                                            0,
                                            0,
                                            "uid",
                                            "cn",
                                            "mail",
                                            null,
                                            false, // <- don't update
                                            true,
                                            userFinder);
        when(entrySelector.select(anyObject())).thenReturn(asList(createUserEntry("user123"),
                                                                  createUserEntry("user234")));
        existingIds.add("user123");
        existingIds.add("user234");

        final SyncResult syncResult = synchronizer.syncAll();

        assertEquals(syncResult.getProcessed(), 2);
        assertEquals(syncResult.getCreated(), 0);
        assertEquals(syncResult.getUpdated(), 0);
        assertEquals(syncResult.getUpToDate(), 0);
        assertEquals(syncResult.getRemoved(), 0);
        assertEquals(syncResult.getFailed(), 0);
        assertEquals(syncResult.getSkipped(), 2);
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
