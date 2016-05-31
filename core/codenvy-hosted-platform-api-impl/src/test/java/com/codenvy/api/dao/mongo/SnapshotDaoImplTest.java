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

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import static com.codenvy.api.dao.mongo.WorkspaceImplCodec.MACHINE_SOURCE;
import static com.codenvy.api.dao.mongo.WorkspaceImplCodec.MACHINE_SOURCE_CONTENT;
import static com.codenvy.api.dao.mongo.WorkspaceImplCodec.MACHINE_SOURCE_LOCATION;
import static com.codenvy.api.dao.mongo.WorkspaceImplCodec.MACHINE_SOURCE_TYPE;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
/**
 * Tests for {@link WorkspaceDaoImpl}.
 *
 * @author Sergii Kabashniuk
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SnapshotDaoImplTest {
    MongoCollection<SnapshotImpl> collection;
    SnapshotDaoImpl               snapshotDao;

    private static final String CUSTOM_MACHINE_SOURCE_TYPE     = "my-custom-type";
    private static final String CUSTOM_MACHINE_SOURCE_LOCATION = "my-custom-location";
    private static final String CUSTOM_MACHINE_SOURCE_CONTENT  = "my-custom-content";

    @BeforeMethod
    public void setUpDb() {
        final Fongo fongo = new Fongo("Snapshot test server");
        final CodecRegistry defaultRegistry = MongoClient.getDefaultCodecRegistry();
        final MongoDatabase database = fongo.getDatabase("snapshot")
                                            .withCodecRegistry(fromRegistries(defaultRegistry,
                                                                              fromCodecs(new SnapshotImplCodec(defaultRegistry))));
        collection = database.getCollection("snapshot", SnapshotImpl.class);
        snapshotDao = new SnapshotDaoImpl(database, "snapshot");
    }

    @Test
    public void testCreateSnapshot() throws Exception {
        //given
        final SnapshotImpl snapshot = createSnapshot();
        //when
        snapshotDao.saveSnapshot(snapshot);
        //then
        final SnapshotImpl result = collection.find(Filters.eq("_id", snapshot.getId())).first();
        assertEquals(result, snapshot);
    }

    @Test(expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "Snapshot must not be null")
    public void testCreateSnapshotWhenSnapshotIsNull() throws Exception {
        snapshotDao.saveSnapshot(null);
    }

    @Test(expectedExceptions = SnapshotException.class)
    public void testCreateSnapshotWhenSnapshotWithSuchIdAlreadyExists() throws Exception {
        //given
        final SnapshotImpl.SnapshotBuilder builder = createSnapshotBuilder();
        final SnapshotImpl snapshot1 = builder.build();
        final SnapshotImpl snapshot2 = builder.setDescription("descr").build();

        //when
        snapshotDao.saveSnapshot(snapshot1);
        snapshotDao.saveSnapshot(snapshot2);
    }

    @Test(expectedExceptions = SnapshotException.class)
    public void testCreateSnapshotWhenSnapshotWithSuchWorkspaceIdEnvOrMachineNameAlreadyExists() throws Exception {
        //given
        final SnapshotImpl.SnapshotBuilder builder = createSnapshotBuilder();
        final SnapshotImpl snapshot1 = builder.build();
        final SnapshotImpl snapshot2 = builder.generateId().build();

        //when
        snapshotDao.saveSnapshot(snapshot1);
        snapshotDao.saveSnapshot(snapshot2);
    }

    @Test(expectedExceptions = SnapshotException.class)
    public void testCreateSnapshotWhenMongoExceptionWasThrew() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).insertOne(any()));

        new SnapshotDaoImpl(db, "snapshot").saveSnapshot(createSnapshot());
    }

    @Test
    public void testRemoveWorkspace() throws Exception {
        //given
        final SnapshotImpl snapshot = createSnapshot();
        snapshotDao.saveSnapshot(snapshot);
        //when
        snapshotDao.removeSnapshot(snapshot.getId());

        assertEquals(collection.count(Filters.eq("_id", snapshot.getId())), 0);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Snapshot identifier must not be null")
    public void testRemoveSnapshotWithNullId() throws Exception {
        snapshotDao.removeSnapshot(null);
    }

    @Test
    public void testGetSnapshotById() throws Exception {
        //given
        final SnapshotImpl snapshot = createSnapshot();
        snapshotDao.saveSnapshot(snapshot);
        //
        final SnapshotImpl result = snapshotDao.getSnapshot(snapshot.getId());

        assertEquals(result, snapshot);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetSnapshotByIdWhenSnapshotDoesNotExist() throws Exception {
        snapshotDao.getSnapshot("sn223534");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Snapshot identifier must not be null")
    public void testGetSnapshotByIdWithNullId() throws Exception {
        snapshotDao.getSnapshot(null);
    }

    @Test
    public void testGetSnapshotByWorkspaceIdEnvNameAndMachineName() throws Exception {
        //given
        final SnapshotImpl snapshot = createSnapshot();
        collection.insertOne(snapshot);
        //when

        final SnapshotImpl result = snapshotDao.getSnapshot(snapshot.getWorkspaceId(), snapshot.getEnvName(), snapshot.getMachineName());
        //then
        assertEquals(result, snapshot);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetUnexistedSnapshot() throws Exception {
        snapshotDao.getSnapshot("workspace-id", "env-name", "machine-name");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace id must not be null")
    public void testGetSnapshotByWorkspaceIdEnvNameAndMachineNameWithNullWorkspace() throws Exception {
        snapshotDao.getSnapshot(null, "env-name", "machine-name");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Environment name must not be null")
    public void testGetSnapshotByWorkspaceIdEnvNameAndMachineNameWithNullEnv() throws Exception {
        snapshotDao.getSnapshot("workspace-id", null, "machine-name");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Machine name must not be null")
    public void testGetSnapshotByWorkspaceIdEnvNameAndMachineNameWithNullMachine() throws Exception {
        snapshotDao.getSnapshot("workspace-id", "env-name", null);
    }

    @Test
    public void testGetSnapshotByOwner() throws Exception {
        //given
        final SnapshotImpl.SnapshotBuilder builder = createSnapshotBuilder();
        final SnapshotImpl snapshot1 = builder.build();
        final SnapshotImpl snapshot2 = builder.generateId().setEnvName("env44").build();
        final SnapshotImpl snapshot3 = builder.generateId().setEnvName("ev55").setNamespace("ow2").build();
        collection.insertMany(asList(snapshot1, snapshot2, snapshot3));
        //when
        final List<SnapshotImpl> result = snapshotDao.findSnapshots(snapshot1.getNamespace(),snapshot1.getWorkspaceId());
       //then
        assertEquals(new HashSet<>(result), new HashSet<>(asList(snapshot1, snapshot2)));
    }

    @Test
    public void testGetSnapshotByOwnerWhenUserDoesNotOwnAnyWorkspace() throws Exception {
        assertTrue(snapshotDao.findSnapshots("test-user", "wsid-1232").isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Snapshot namespace must not be null")
    public void testGetSnapshotByOwnerWhenOwnerIsNull() throws Exception {
        snapshotDao.findSnapshots(null, "wsid-1232");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace id must not be null")
    public void testGetSnapshotByOwnerWhenWsIdIsNull() throws Exception {
        snapshotDao.findSnapshots("user-234", null);
    }

    @Test
    public void testSnapshotEncoding() {
        // mocking DocumentCodec
        final DocumentCodec documentCodec = mock(DocumentCodec.class);
        when(documentCodec.getEncoderClass()).thenReturn(Document.class);
        final SnapshotImplCodec codec = new SnapshotImplCodec(CodecRegistries.fromCodecs(documentCodec));

        // get encoded document instance
        final Document[] documentHolder = new Document[1];
        Mockito.doAnswer(invocation -> {
            if ("encode".equals(invocation.getMethod().getName())) {
                documentHolder[0] = (Document)invocation.getArguments()[1];
            }
            return null;
        }).when(documentCodec).encode(any(), any(), any());

        // prepare test snapshot
        final SnapshotImpl snapshot = createSnapshot();

        // encode workspace
        codec.encode(null, snapshot, null);

        // check encoding result
        final Document result = documentHolder[0];
        assertEquals(result.getString("_id"), snapshot.getId(), "Snapshot id");
        assertEquals(result.getString("workspaceId"), snapshot.getWorkspaceId(), "Workspace id");
        assertEquals(result.getString("machineName"), snapshot.getMachineName(), "Machine name");
        assertEquals(result.getString("envName"), snapshot.getEnvName(), "Environment name");
        assertEquals(result.getString("type"), snapshot.getType(), "Snapshot type");
        assertEquals(result.getString("namespace"), snapshot.getNamespace(), "Snapshot owner");
        assertEquals(result.getBoolean("isDev").booleanValue(), snapshot.isDev(), "Snapshot isdev");
        assertEquals(result.getLong("creationDate").longValue(), snapshot.getCreationDate(), "Snapshot creation date");
        assertEquals(result.getString("type"), snapshot.getType(), "Snapshot defaultEnvName");

        // check attributes
        final Document machineSource = (Document) result.get(MACHINE_SOURCE);
        assertEquals(machineSource.get(MACHINE_SOURCE_TYPE), snapshot.getMachineSource().getType());
        assertEquals(machineSource.get(MACHINE_SOURCE_LOCATION), snapshot.getMachineSource().getLocation());
        assertEquals(machineSource.get(MACHINE_SOURCE_CONTENT), snapshot.getMachineSource().getContent());
    }

    /**
     * As snapshot encoding was tested with assertion on each encoded document field
     * there is no reason to do the same with decoding. To check if document is decoded - it is
     * enough to check that decoding of encoded document produces exactly equal snapshot to encoded one.
     * <p/>
     * <p>Simplified test case:
     * <pre>
     *     SnapshotImpl snapshot = ...
     *
     *     Document encodedSnapshot = codec.encode(snapshot)
     *
     *     SnapshotImpl result = codec.decode(encodedSnapshot)
     *
     *     assert snapshot.equals(result)
     * </pre>
     *
     * @see #testSnapshotEncoding()
     */
    @Test(dependsOnMethods = "testSnapshotEncoding")
    public void testWorkspaceDecoding() {
        // mocking DocumentCodec
        final DocumentCodec documentCodec = mock(DocumentCodec.class);
        when(documentCodec.getEncoderClass()).thenReturn(Document.class);
        final SnapshotImplCodec codec = new SnapshotImplCodec(CodecRegistries.fromCodecs(documentCodec));


        // get encoded document instance
        final Document[] documentHolder = new Document[1];
        Mockito.doAnswer(invocation -> {
            if ("encode".equals(invocation.getMethod().getName())) {
                documentHolder[0] = (Document)invocation.getArguments()[1];
            }
            return null;
        }).when(documentCodec).encode(any(), any(), any());

        // prepare test workspace
        final SnapshotImpl snapshot = createSnapshot();

        // encode workspace
        codec.encode(null, snapshot, null);

        // mocking document codec to return encoded workspace
        when(documentCodec.decode(any(), any())).thenReturn(documentHolder[0]);

        final SnapshotImpl result = codec.decode(null, null);

        assertEquals(result, snapshot);
    }

    private SnapshotImpl createSnapshot() {
        return createSnapshotBuilder()
                .build();
    }

    private SnapshotImpl.SnapshotBuilder createSnapshotBuilder() {
        return SnapshotImpl.builder()
                           .generateId()
                           .setType("docker")
                           .setMachineSource(new MachineSourceImpl(CUSTOM_MACHINE_SOURCE_TYPE).setContent(CUSTOM_MACHINE_SOURCE_CONTENT).setLocation(
                                   CUSTOM_MACHINE_SOURCE_LOCATION))
                           .setNamespace("user123")
                           .setWorkspaceId("workspace123")
                           .setMachineName("machine123")
                           .setEnvName("env123")
                           .setDescription("Test snapshot")
                           .setDev(true)
                           .useCurrentCreationDate();
    }

    private MongoDatabase mockDatabase(Consumer<MongoCollection<SnapshotImpl>> consumer) {
        final MongoCollection<SnapshotImpl> collection = mock(MongoCollection.class);
        consumer.accept(collection);

        final MongoDatabase database = mock(MongoDatabase.class);
        when(database.getCollection("snapshot", SnapshotImpl.class)).thenReturn(collection);

        return database;
    }
}
