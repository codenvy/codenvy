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
package com.codenvy.api.dao.mongo;

import org.eclipse.che.api.core.NotFoundException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.codenvy.api.dao.mongo.MongoUtil.asDBList;
import static com.codenvy.api.dao.mongo.MongoUtil.asMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link PreferenceDaoImpl}
 *
 * @author Eugene Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class PreferenceDaoTest extends BaseDaoTest {

    private PreferenceDao preferenceDao;

    @BeforeMethod
    public void setUp() throws Exception {
        final String collectionName = "preferences";
        setUp(collectionName);
        preferenceDao = new PreferenceDaoImpl(db, collectionName);
    }

    @Test
    public void shouldBeAbleToSetPreferences() throws Exception {
        final String userId = "test-user-id";
        final Map<String, String> preferences = createPreferences();

        preferenceDao.setPreferences(userId, preferences);

        final DBObject preferencesDocument = collection.findOne(new BasicDBObject("_id", userId));
        assertNotNull(preferencesDocument);
        assertEquals(asMap(preferencesDocument.get("preferences")), preferences);
    }

    @Test
    public void shouldRemovePreferencesWhenPreferencesAreEmpty() throws Exception {
        final String userId = "test-user-id";

        preferenceDao.setPreferences(userId, new HashMap<String, String>());

        final DBObject preferencesDocument = collection.findOne(new BasicDBObject("_id", userId));
        assertNull(preferencesDocument);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldNotPersistNullPreferences() throws Exception {
        final String userId = "test-user-id";

        preferenceDao.setPreferences(userId, null);
    }

    @Test
    public void shouldBeAbleToGetPreferences() throws Exception {
        final String userId = "test-user-id";
        final Map<String, String> preferences = createPreferences();
        insertPreferences(userId, preferences);

        final Map<String, String> actual = preferenceDao.getPreferences(userId);

        assertEquals(actual, preferences);
    }

    @Test
    public void shouldReturnEmptyMapIfPreferencesAreAbsentInDatabase() throws Exception {
        final Map<String, String> actual = preferenceDao.getPreferences("test-user-id");

        assertTrue(actual.isEmpty());
    }

    @Test
    public void shouldBeAbleToGetFilteredPreferences() throws Exception {
        final String userId = "test-user-id";
        final Map<String, String> preferences = new HashMap<>(8);
        preferences.put("test-key-1", "test-value-1");
        preferences.put("test-key-2", "test-value-2");
        preferences.put("preference-name-1", "test-value-3");
        preferences.put("preference-name-2", "test-value-4");
        insertPreferences(userId, preferences);

        final Map<String, String> actual = preferenceDao.getPreferences(userId, ".*key.*");

        assertEquals(actual.size(), 2);
        assertTrue(actual.containsKey("test-key-1"));
        assertTrue(actual.containsKey("test-key-2"));
    }

    @Test
    public void shouldReturnEmptyMapIfMatchedPreferencesNotFound() throws Exception {
        final String userId = "test-user-id";
        final Map<String, String> preferences = new HashMap<>(8);
        preferences.put("test-key-1", "test-value-1");
        preferences.put("test-key-2", "test-value-2");
        preferences.put("preference-name-1", "test-value-3");
        preferences.put("preference-name-2", "test-value-4");
        insertPreferences(userId, preferences);

        final Map<String, String> actual = preferenceDao.getPreferences(userId, "test");

        assertTrue(actual.isEmpty());
    }

    @Test
    public void shouldIgnoreNullFilter() throws Exception {
        final String userId = "test-user-id";
        final Map<String, String> preferences = createPreferences();
        insertPreferences(userId, preferences);

        final Map<String, String> actual = preferenceDao.getPreferences(userId, null);

        assertEquals(actual, preferences);
    }

    @Test
    public void shouldIgnoreEmptyFilter() throws Exception {
        final String userId = "test-user-id";
        final Map<String, String> preferences = createPreferences();
        insertPreferences(userId, preferences);

        final Map<String, String> actual = preferenceDao.getPreferences(userId, null);

        assertEquals(actual, preferences);
    }

    @Test
    public void shouldBeAbleToRemovePreferences() throws Exception {
        final String userId = "test-user-id";
        final Map<String, String> preferences = createPreferences();
        insertPreferences(userId, preferences);
        assertNotNull(collection.findOne(userId));

        preferenceDao.remove(userId);

        assertNull(collection.findOne(userId));
    }

    private void insertPreferences(String userId, Map<String, String> preferences) {
        collection.insert(new BasicDBObject("_id", userId).append("preferences", asDBList(preferences)));
    }

    private Map<String, String> createPreferences() {
        final Map<String, String> preferences = new HashMap<>(8);
        preferences.put("test-key-1", "test-value-1");
        preferences.put("test-key-2", "test-value-2");
        preferences.put("test-key-3", "test-value-3");
        return preferences;
    }
}
