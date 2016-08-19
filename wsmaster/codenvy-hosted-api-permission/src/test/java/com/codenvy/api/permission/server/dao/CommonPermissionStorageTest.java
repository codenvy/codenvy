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
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.shared.model.Permissions;
import com.github.fakemongo.Fongo;
import com.google.common.collect.ImmutableSet;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link CommonPermissionsDao}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class CommonPermissionStorageTest {

    private MongoCollection<AbstractPermissions> collection;
    private CommonPermissionsDao                 permissionStorage;

    @BeforeMethod
    public void setUpDb() throws Exception {
        final Fongo fongo = new Fongo("Permissions test server");
        final CodecRegistry defaultRegistry = MongoClient.getDefaultCodecRegistry();
        final MongoDatabase database = fongo.getDatabase("permissions")
                                            .withCodecRegistry(fromRegistries(defaultRegistry,
                                                                              fromCodecs(new PermissionsImplCodec(defaultRegistry))));
        collection = database.getCollection("permissions", AbstractPermissions.class);
        permissionStorage = new CommonPermissionsDao(database, "permissions", ImmutableSet.of(new TestDomain()));
    }

    @Test
    public void shouldStorePermissions() throws Exception {
        final AbstractPermissions permissions = createPermissions();

        permissionStorage.store(permissions);

        final Permissions result = collection.find(and(eq("user", permissions.getUserId()),
                                                       eq("domain", permissions.getDomainId()),
                                                       eq("instance", permissions.getInstanceId())))
                                             .first();
        assertEquals(result, permissions);
    }

    @Test
    public void shouldUpdatePermissionsWhenItHasAlreadyExisted() throws Exception {
        AbstractPermissions oldPermissions = createPermissions();
        permissionStorage.store(oldPermissions);

        AbstractPermissions newPermissions =
                new AbstractPermissions(oldPermissions.getUserId(), oldPermissions.getDomainId(), oldPermissions.getInstanceId(),
                                        singletonList("read"));
        permissionStorage.store(newPermissions);

        final Permissions result = collection.find(and(eq("user", newPermissions.getUserId()),
                                                       eq("domain", newPermissions.getDomainId()),
                                                       eq("instance", newPermissions.getInstanceId())))
                                             .first();
        assertEquals(result, newPermissions);
    }

    @Test
    public void shouldReturnsSupportedDomainsIds() {
        assertEquals(permissionStorage.getDomains(), ImmutableSet.of(new TestDomain()));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenMongoExceptionWasThrewOnPermissionsStoring() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).replaceOne(any(), any(), any()));

        new CommonPermissionsDao(db, "permissions", ImmutableSet.of(new TestDomain())).store(createPermissions());
    }

    @Test
    public void shouldRemovePermissions() throws Exception {
        final AbstractPermissions permissions = createPermissions();
        collection.insertOne(permissions);

        permissionStorage.remove(permissions.getUserId(), permissions.getDomainId(), permissions.getInstanceId());

        assertEquals(collection.count(and(eq("user", permissions.getUserId()),
                                          eq("domain", permissions.getDomainId()),
                                          eq("instance", permissions.getInstanceId()))),
                     0);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Permissions for user 'user123' and instance 'instance' of domain 'domain' was not found")
    public void shouldThrowNotFoundExceptionWhenPermissionsWasNotFoundOnRemove() throws Exception {
        permissionStorage.remove("user123", "domain", "instance");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenMongoExceptionWasThrewOnPermissionsRemoving() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).deleteOne(any()));

        new CommonPermissionsDao(db, "permissions", ImmutableSet.of(new TestDomain())).remove("user", "test", "test123");
    }

    @Test
    public void shouldBeAbleToGetPermissionsByInstance() throws Exception {
        final AbstractPermissions permissions = createPermissions();
        collection.insertOne(permissions);
        collection.insertOne(new AbstractPermissions("user", "domain", "otherTest", singletonList("read")));

        final List<AbstractPermissions> result = permissionStorage.getByInstance(permissions.getDomainId(), permissions.getInstanceId());

        assertEquals(result.size(), 1);
        assertEquals(result.get(0), permissions);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenMongoExceptionWasThrewOnGettingPermissionsByInstance() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).find((Bson)any()));

        new CommonPermissionsDao(db, "permissions", ImmutableSet.of(new TestDomain())).getByInstance("domain", "test123");
    }

    @Test
    public void shouldBeAbleToGetPermissions() throws Exception {
        final AbstractPermissions permissions = createPermissions();
        collection.insertOne(permissions);

        final Permissions result = permissionStorage.get(permissions.getUserId(), permissions.getDomainId(), permissions.getInstanceId());

        assertEquals(result, permissions);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Permissions for user 'user' and instance 'instance' of domain 'domain' was not found")
    public void shouldThrowNotFoundExceptionWhenThereIsNotAnyPermissionsForGivenUserAndDomainAndInstance() throws Exception {
        final Permissions result = permissionStorage.get("user", "domain", "instance");

        assertEquals(result, new AbstractPermissions("user", "domain", "instance", emptyList()));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenMongoExceptionWasThrewOnGettingPermissions() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).find((Bson)any()));

        new CommonPermissionsDao(db, "permissions", ImmutableSet.of(new TestDomain())).get("user", "domain", "test123");
    }

    @Test
    public void shouldBeAbleToCheckPermissionExistence() throws Exception {
        final AbstractPermissions permissions = createPermissions();
        collection.insertOne(permissions);

        final boolean readPermissionExisted =
                permissionStorage.exists(permissions.getUserId(), permissions.getDomainId(), permissions.getInstanceId(), "read");
        final boolean fakePermissionExisted =
                permissionStorage.exists(permissions.getUserId(), permissions.getDomainId(), permissions.getInstanceId(), "fake");

        assertEquals(readPermissionExisted, permissions.getActions().contains("read"));
        assertEquals(fakePermissionExisted, permissions.getActions().contains("fake"));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenMongoExceptionWasThrewOnCheckinngPermissionExistence() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).find((Bson)any()));

        new CommonPermissionsDao(db, "permissions", ImmutableSet.of(new TestDomain())).exists("user", "domain", "test123", "read");
    }

    private AbstractPermissions createPermissions() {
        return new AbstractPermissions("user",
                                   "test",
                                   "test123",
                                   Arrays.asList("read", "write", "use", "delete"));
    }

    public class TestDomain extends AbstractPermissionsDomain {
        public TestDomain() {
            super("test", Arrays.asList("read", "write", "use", "delete"));
        }
    }

    private MongoDatabase mockDatabase(Consumer<MongoCollection<AbstractPermissions>> consumer) {
        @SuppressWarnings("unchecked")
        final MongoCollection<AbstractPermissions> collection = mock(MongoCollection.class);
        consumer.accept(collection);

        final MongoDatabase database = mock(MongoDatabase.class);
        when(database.getCollection("permissions", AbstractPermissions.class)).thenReturn(collection);

        return database;
    }
}
