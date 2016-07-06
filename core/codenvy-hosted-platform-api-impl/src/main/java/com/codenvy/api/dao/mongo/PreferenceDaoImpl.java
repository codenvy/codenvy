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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.spi.PreferenceDao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.codenvy.api.dao.mongo.MongoUtil.asDBList;
import static com.codenvy.api.dao.mongo.MongoUtil.asMap;

/**
 * Implementation of {@link PreferenceDao} based on MongoDB storage
 * <p/>
 * <pre>
 * Database schema:
 * {
 *     "_id" : "userid",
 *     "preferences" : [
 *          ...
 *          {
 *              "name" : "preference name",
 *              "value" : "preference value",
 *          }
 *          ...
 *     ]
 * }
 * </pre>
 * Why do we use array of objects instead of key-value object for preferences?
 * <p/>
 * The main reason for it is that MongoDB will not persist any object
 * with key which contains dot.
 * <p/>
 * The most appropriate workaround is:
 * <pre>
 *     replace key-value object
 *
 *     "preferences" : {
 *         "name" : "value"
 *     }
 *
 *     with list of objects
 *
 *     "preferences" : [
 *          {
 *              "name" : "preference name",
 *              "value" : "preference value"
 *          }
 *     ]
 *
 * </pre>
 *
 * @author Eugene Voevodin
 */
@Singleton
public class PreferenceDaoImpl implements PreferenceDao {

    private static final String PREFERENCES_COLLECTION = "organization.storage.db.preferences.collection";

    private final DBCollection collection;

    @Inject
    public PreferenceDaoImpl(@Named("mongo.db.organization") DB database,
                             @Named(PREFERENCES_COLLECTION) String collectionName) {
        this.collection = database.getCollection(collectionName);
    }

    @Override
    public void setPreferences(String userId, Map<String, String> preferences) throws ServerException {
        if (preferences == null) {
            throw new IllegalArgumentException("Not null preferences required");
        }
        if (preferences.isEmpty()) {
            remove(userId);
        } else {
            try {
                save(userId, preferences);
            } catch (MongoException ex) {
                throw new ServerException("It is not possible to persist preferences", ex);
            }
        }
    }

    @Override
    public Map<String, String> getPreferences(String userId) throws ServerException {
        try {
            return load(userId, null);
        } catch (MongoException ex) {
            throw new ServerException("It is not possible to retrieve preferences", ex);
        }
    }

    @Override
    public Map<String, String> getPreferences(String userId, String filter) throws ServerException {
        try {
            return load(userId, filter);
        } catch (MongoException ex) {
            throw new ServerException("It is not possible to retrieve preferences", ex);
        }
    }

    @Override
    public void remove(String userId) throws ServerException {
        try {
            remove0(userId);
        } catch (MongoException ex) {
            throw new ServerException("It is not possible to remove preferences", ex);
        }
    }

    private void save(String userId, Map<String, String> preferences) {
        final BasicDBObject preferencesDocument = new BasicDBObject("_id", userId).append("preferences", asDBList(preferences));
        collection.save(preferencesDocument);
    }

    private Map<String, String> load(String userId, String filter) {
        final DBObject preferencesDocument = collection.findOne(userId);
        if (preferencesDocument != null) {
            final Map<String, String> preferences = asMap(preferencesDocument.get("preferences"));
            if (filter != null && !filter.isEmpty()) {
                return filter(preferences, filter);
            } else {
                return preferences;
            }
        }
        return new HashMap<>();
    }

    private void remove0(String userId) {
        collection.remove(new BasicDBObject("_id", userId));
    }

    private Map<String, String> filter(Map<String, String> preferences, String filter) {
        final Map<String, String> filtered = new HashMap<>();
        final Pattern pattern = Pattern.compile(filter);
        for (Map.Entry<String, String> entry : preferences.entrySet()) {
            if (pattern.matcher(entry.getKey()).matches()) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }
}
