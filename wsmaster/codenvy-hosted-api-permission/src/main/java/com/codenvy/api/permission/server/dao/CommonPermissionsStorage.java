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
package com.codenvy.api.permission.server.dao;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.model.impl.PermissionsImpl;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.api.permission.shared.model.Permissions;
import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;

import org.bson.Document;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

/**
 * Common implementation for {@link PermissionsDao} based on MongoDB storage.
 *
 * <p>Stores permissions of domains that bound by {@link CommonDomains}
 *
 * <p>Example of domain binding
 * <pre>
 *     Multibinder<PermissionsDomain> multibinder = Multibinder.newSetBinder(binder(), PermissionsDomain.class, CommonDomains.class);
 *     multibinder.addBinding().toInstance(new PermissionsDomain("myDomain",
 *                                                               new HashSet&lt;&gt;(Arrays.asList("read", "write", "use"))));
 * </pre>
 *
 * <p>Permissions collection document scheme:
 * <pre>
 *
 * {
 *     "user" : "user123",
 *     "domain" : "workspace",
 *     "instance" : "workspace123",
 *     "actions" : [
 *         "read",
 *         "write",
 *         ...
 *     ]
 * }
 *
 * </pre>
 *
 * @author Sergii Leschenko
 */
@Singleton
public class CommonPermissionsStorage implements PermissionsDao {
    private final MongoCollection<PermissionsImpl> collection;

    private final Map<String, AbstractPermissionsDomain> idToDomain;

    @Inject
    public CommonPermissionsStorage(@Named("mongo.db.organization") MongoDatabase database,
                                    @Named("organization.storage.db.permission.collection") String collectionName,
                                    @CommonDomains Set<AbstractPermissionsDomain> permissionsDomains) throws IOException {
        collection = database.getCollection(collectionName, PermissionsImpl.class);
        collection.createIndex(new Document("user", 1).append("domain", 1).append("instance", 1), new IndexOptions().unique(true));

        final ImmutableMap.Builder<String, AbstractPermissionsDomain> mapBuilder = ImmutableMap.builder();
        permissionsDomains.stream()
                          .forEach(domain -> mapBuilder.put(domain.getId(), domain));
        idToDomain = mapBuilder.build();
    }

    @Override
    public Set<AbstractPermissionsDomain> getDomains() {
        return new HashSet<>(idToDomain.values());
    }

    @Override
    public void store(PermissionsImpl permissions) throws ServerException {
        try {
            collection.replaceOne(and(eq("user", permissions.getUserId()),
                                      eq("domain", permissions.getDomainId()),
                                      eq("instance", permissions.getInstanceId())),
                                  permissions,
                                  new UpdateOptions().upsert(true));
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public void remove(String user, String domain, String instance) throws ServerException, NotFoundException {
        try {
            final DeleteResult deleteResult = collection.deleteOne(and(eq("user", user),
                                                                       eq("domain", domain),
                                                                       eq("instance", instance)));
            if (deleteResult.getDeletedCount() == 0) {
                throw new NotFoundException(String.format("Permissions for user '%s' and instance '%s' of domain '%s' was not found",
                                                          user, instance, domain));
            }
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public PermissionsImpl get(String user, String domain, String instance) throws ServerException, NotFoundException {
        PermissionsImpl found;
        try {
            found = collection.find(and(eq("user", user),
                                        eq("domain", domain),
                                        eq("instance", instance)))
                              .first();
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }

        if (found == null) {
            throw new NotFoundException(String.format("Permissions for user '%s' and instance '%s' of domain '%s' was not found",
                                                      user, instance, domain));
        }

        return found;
    }

    @Override
    public List<PermissionsImpl> getByInstance(String domain, String instance) throws ServerException {
        try {
            return collection.find(and(eq("domain", domain),
                                       eq("instance", instance)))
                             .into(new ArrayList<>());
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String user, String domain, String instance, String requiredAction) throws ServerException {
        try {
            final Permissions found = collection.find(and(eq("user", user),
                                                          eq("domain", domain),
                                                          eq("instance", instance),
                                                          in("actions", requiredAction)))
                                                .first();
            return found != null;
        } catch (MongoException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }
}
