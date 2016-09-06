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

import com.codenvy.api.workspace.server.dao.WorkerDao;
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
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConf2Impl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.codenvy.api.dao.mongo.MongoUtil.documentsListAsMap;
import static com.codenvy.api.dao.mongo.MongoUtil.mapAsDocumentsList;
import static com.codenvy.api.dao.mongo.MongoUtilTest.mockWriteEx;
import static com.mongodb.ErrorCategory.DUPLICATE_KEY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link WorkspaceDaoImpl}.
 *
 * @author Eugene Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class WorkspaceDaoImplTest {

    @Mock
    private WorkerDao workerDao;

    private MongoCollection<WorkspaceImpl> collection;
    private WorkspaceDaoImpl               workspaceDao;

    @BeforeMethod
    public void setUpDb() {
        final Fongo fongo = new Fongo("Workspace test server");
        final CodecRegistry defaultRegistry = MongoClient.getDefaultCodecRegistry();
        final MongoDatabase database = fongo.getDatabase("workspaces")
                                            .withCodecRegistry(fromRegistries(defaultRegistry,
                                                                              fromCodecs(new WorkspaceImplCodec(defaultRegistry))));
        collection = database.getCollection("workspaces", WorkspaceImpl.class);
        workspaceDao = new WorkspaceDaoImpl(database, "workspaces", workerDao);
    }

    @Test
    public void testCreateWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();

        workspaceDao.create(workspace);

        final WorkspaceImpl result = collection.find(Filters.eq("_id", workspace.getId())).first();
        assertEquals(result, workspace);
    }

    @Test(expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "Workspace must not be null")
    public void testCreateWorkspaceWhenWorkspaceIsNull() throws Exception {
        workspaceDao.create(null);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Workspace with id '.*' or name '.*' in namespace '.*' already exists")
    public void testCreateWorkspaceWhenWorkspaceWithSuchIdAlreadyExists() throws Exception {
        // fongo throws DuplicateKeyException in the case of duplicate key
        // but mongo 3.x driver throws MongoWriteException in this case
        // so we need to mock the collection to force fongo behave like mongo driver does
        final MongoDatabase db = mockDatabase(col -> doThrow(mockWriteEx(DUPLICATE_KEY)).when(col).insertOne(any()));

        new WorkspaceDaoImpl(db, "workspaces", workerDao).create(createWorkspace());
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Workspace with id '.*' or name '.*' in namespace '.*' already exists")
    public void testCreateWorkspaceWhenWorkspaceWithSuchNameAndOwnerAlreadyExists() throws Exception {
        // fongo throws DuplicateKeyException in the case of duplicate key
        // but mongo 3.x driver throws MongoWriteException in this case
        // so we need to mock the collection to force fongo behave like mongo driver does
        final MongoDatabase db = mockDatabase(col -> doThrow(mockWriteEx(DUPLICATE_KEY)).when(col).findOneAndReplace(any(), any()));

        new WorkspaceDaoImpl(db, "workspaces", workerDao).update(createWorkspace());
    }

    @Test(expectedExceptions = ServerException.class)
    public void testCreateWorkspaceWhenMongoExceptionWasThrew() throws Exception {
        final MongoDatabase db = mockDatabase(col -> doThrow(mock(MongoException.class)).when(col).insertOne(any()));

        new WorkspaceDaoImpl(db, "workspaces", workerDao).create(createWorkspace());
    }

    @Test
    public void testUpdateWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        collection.insertOne(workspace);
        workspace.getConfig().setName("new-workspace-name");
        workspace.getConfig().setDescription("new-workspace-description");

        workspaceDao.update(workspace);

        final WorkspaceImpl result = collection.find(Filters.eq("_id", workspace.getId())).first();
        assertEquals(result, workspace);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testUpdateWhenWorkspaceDoesNotExist() throws Exception {
        final MongoDatabase db = mockDatabase(col -> when(col.updateOne(any(), any())).thenReturn(null));

        new WorkspaceDaoImpl(db, "workspaces", workerDao).update(createWorkspace());
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace update must not be null")
    public void testUpdateWorkspaceWhenWorkspaceIsNull() throws Exception {
        workspaceDao.update(null);
    }

    @Test(expectedExceptions = ServerException.class)
    public void testUpdateWorkspaceWhenMongoExceptionWasThrew() throws Exception {
        final MongoDatabase db = mockDatabase(col -> when(col.findOneAndReplace(any(), any())).thenThrow(mock(MongoException.class)));

        new WorkspaceDaoImpl(db, "workspaces", workerDao).update(createWorkspace());
    }

    @Test
    public void testRemoveWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        collection.insertOne(workspace);
        assertEquals(collection.count(Filters.eq("_id", workspace.getId())), 1);

        workspaceDao.remove(workspace.getId());

        assertEquals(collection.count(Filters.eq("_id", workspace.getId())), 0);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace identifier must not be null")
    public void testRemoveWorkspaceWithNullId() throws Exception {
        workspaceDao.remove(null);
    }

    @Test
    public void testGetWorkspaceById() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        collection.insertOne(workspace);

        final WorkspaceImpl result = workspaceDao.get(workspace.getId());

        assertEquals(result, workspace);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetWorkspaceByIdWhenWorkspaceDoesNotExist() throws Exception {
        workspaceDao.get("workspace123");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace identifier must not be null")
    public void testGetWorkspaceByIdWithNullId() throws Exception {
        workspaceDao.get(null);
    }

    @Test
    public void testGetWorkspaceByNameAndOwner() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        collection.insertOne(workspace);

        final WorkspaceImpl result = workspaceDao.get(workspace.getConfig().getName(), workspace.getNamespace());

        assertEquals(result, workspace);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetWorkspaceByNameAndOwnerWhenWorkspaceDoesNotExist() throws Exception {
        workspaceDao.get("workspace-name", "workspace-owner");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace name must not be null")
    public void testGetWorkspaceByNameAndOwnerWithNullName() throws Exception {
        workspaceDao.get(null, "workspace-owner");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace namespace must not be null")
    public void testGetWorkspaceByNameAndOwnerWithNullOwner() throws Exception {
        workspaceDao.get("workspace name", null);
    }

    @Test
    public void testGetWorkspacesByOwner() throws Exception {
        final WorkspaceImpl workspace = createWorkspace();
        final WorkspaceImpl workspace2 = createWorkspace();
        workspace2.getConfig().setName(workspace.getConfig().getName() + '2');
        final WorkspaceImpl workspace3 = new WorkspaceImpl(generate("ws", 16),
                                                           workspace.getNamespace() + '2',
                                                           workspace.getConfig());
        workspace3.getConfig().setName(workspace.getConfig().getName() + '3');
        collection.insertMany(asList(workspace, workspace2, workspace3));

        final List<WorkspaceImpl> result = workspaceDao.getByNamespace(workspace.getNamespace());

        assertEquals(new HashSet<>(result), new HashSet<>(asList(workspace, workspace2)));
    }

    @Test
    public void testGetWorkspacesByOwnerWhenUserDoesNotOwnAnyWorkspace() throws Exception {
        assertTrue(workspaceDao.getByNamespace("test-user").isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Workspace namespace must not be null")
    public void testGetWorkspacesByOwnerWhenOwnerIsNull() throws Exception {
        workspaceDao.getByNamespace(null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWorkspaceEncoding() {
        // mocking DocumentCodec
        final DocumentCodec documentCodec = mock(DocumentCodec.class);
        when(documentCodec.getEncoderClass()).thenReturn(Document.class);
        final WorkspaceImplCodec codec = new WorkspaceImplCodec(CodecRegistries.fromCodecs(documentCodec));

        // get encoded document instance
        final Document[] documentHolder = new Document[1];
        doAnswer(invocation -> {
            if ("encode".equals(invocation.getMethod().getName())) {
                documentHolder[0] = (Document)invocation.getArguments()[1];
            }
            return null;
        }).when(documentCodec).encode(any(), any(), any());

        // prepare test workspace
        final WorkspaceImpl workspace = createWorkspace();

        // encode workspace
        codec.encode(null, workspace, null);

        // check encoding result
        final Document result = documentHolder[0];
        assertEquals(result.getString("_id"), workspace.getId(), "Workspace id");
        assertEquals(result.getString("namespace"), workspace.getNamespace(), "Workspace owner");
        final Document wsConfig = (Document)result.get("config");
        assertEquals(wsConfig.getString("name"), workspace.getConfig().getName(), "Workspace name");
        assertEquals(wsConfig.getString("description"), workspace.getConfig().getDescription(), "Workspace description");
        assertEquals(wsConfig.getString("defaultEnv"), workspace.getConfig().getDefaultEnv(), "Workspace defaultEnv");

        // check attributes
        final List<Document> attributes = (List<Document>)result.get("attributes");
        assertEquals(attributes, mapAsDocumentsList(workspace.getAttributes()), "Workspace attributes");

        // check commands
        final List<Document> commands = (List<Document>)wsConfig.get("commands");
        assertEquals(commands.size(), workspace.getConfig().getCommands().size(), "Workspace commands size");
        for (int i = 0; i < commands.size(); i++) {
            final CommandImpl command = workspace.getConfig().getCommands().get(i);
            final Document document = commands.get(i);

            assertEquals(document.getString("name"), command.getName(), "Command name");
            assertEquals(document.getString("commandLine"), command.getCommandLine(), "Command line");
            assertEquals(document.getString("type"), command.getType(), "Command type");
        }

        // check projects
        final List<Document> projects = (List<Document>)wsConfig.get("projects");
        assertEquals(projects.size(), workspace.getConfig().getProjects().size());
        for (int i = 0; i < projects.size(); i++) {
            final ProjectConfigImpl project = workspace.getConfig().getProjects().get(i);
            final Document projDoc = projects.get(0);

            assertEquals(project.getName(), projDoc.getString("name"), "Project nam");
            assertEquals(project.getType(), projDoc.getString("type"), "Project type");
            assertEquals(project.getDescription(), projDoc.getString("description"));
            assertEquals(project.getPath(), projDoc.getString("path"));

            final List<Document> mixins = (List<Document>)projDoc.get("mixins");
            assertEquals(project.getMixins(), mixins, "Mixin types");

            final List<Document> attrsList = (List<Document>)projDoc.get("attributes");
            assertEquals(attrsList.size(), project.getAttributes().size());
            for (Document attrDoc : attrsList) {
                final String attrName = attrDoc.getString("name");
                final List<String> attrValue = (List<String>)attrDoc.get("value");

                assertEquals(project.getAttributes().get(attrName), attrValue, "Attribute values");
            }

            if (project.getSource() != null) {
                final Document source = (Document)projDoc.get("source");

                assertNotNull(source);
                assertEquals(source.getString("type"), project.getSource().getType(), "Source type");
                assertEquals(source.getString("location"), project.getSource().getLocation(), "Source location");

                final List<Document> parameters = (List<Document>)source.get("parameters");
                assertEquals(documentsListAsMap(parameters), project.getSource().getParameters(), "Source parameters");
            }
        }

        // check environments
        final Map<String, Document> environments = (Map<String, Document>)wsConfig.get("environments");
        assertEquals(environments.size(), workspace.getConfig().getEnvironments().size());
        for (Map.Entry<String, Document> envEntry : environments.entrySet()) {
            final EnvironmentImpl environment = workspace.getConfig()
                                                         .getEnvironments()
                                                         .get(envEntry.getKey());
            assertNotNull(environment);
            if (environment.getRecipe() != null) {
                final Document document = envEntry.getValue().get("recipe", Document.class);
                assertEquals(document.getString("type"),
                             environment.getRecipe().getType(),
                             "Environment recipe type");
                assertEquals(document.getString("contentType"),
                             environment.getRecipe().getContentType(),
                             "Environment recipe content type");
                assertEquals(document.getString("content"),
                             environment.getRecipe().getContent(),
                             "Environment recipe content");
                assertEquals(document.getString("location"),
                             environment.getRecipe().getLocation(),
                             "Environment recipe location");
            }
            if (environment.getMachines() != null) {
                Map<String, Document> machinesDocs = (Map<String, Document>)envEntry.getValue().get("machines");
                for (Map.Entry<String, ExtendedMachineImpl> machineEntry : environment.getMachines()
                                                                                      .entrySet()) {

                    if (machineEntry.getValue() != null) {
                        Document machineDoc = machinesDocs.get(machineEntry.getKey());
                        assertNotNull(machineDoc);
                        ExtendedMachineImpl machine = machineEntry.getValue();
                        if (machine.getAgents() != null) {
                            List<String> agents = (List<String>)machineDoc.get("agents");
                            assertEquals(agents, machine.getAgents());
                        }
                        if (machine.getServers() != null) {
                            Map<String, Document> serversDocs = (Map<String, Document>)machineDoc.get("servers");
                            for (Map.Entry<String, ServerConf2Impl> serverEntry : machine.getServers()
                                                                                         .entrySet()) {
                                if (serverEntry.getValue() != null) {
                                    Document serverDoc = serversDocs.get(serverEntry.getKey());
                                    assertNotNull(serverDoc);

                                    assertEquals(serverDoc.getString("port"), serverEntry.getValue().getPort());

                                    assertEquals(serverDoc.getString("protocol"), serverEntry.getValue().getProtocol());

                                    List<Document> properties = (List<Document>)serverDoc.get("properties");
                                    if (serverEntry.getValue().getProperties() != null) {
                                        assertEquals(documentsListAsMap(properties),
                                                     serverEntry.getValue().getProperties(),
                                                     "Server properties");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * As workspace encoding was tested with assertion on each encoded document field
     * there is no reason to do the same with decoding. To check if document is decoded - it is
     * enough to check that decoding of encoded document produces exactly equal workspace to encoded one.
     * <p/>
     * <p>Simplified test case:
     * <pre>
     *     WorkspaceImpl ws = ...
     *
     *     Document encodedWs = codec.encode(ws)
     *
     *     WorkspaceImpl result = codec.decode(encodedWs)
     *
     *     assert ws.equals(result)
     * </pre>
     *
     * @see #testWorkspaceEncoding()
     */
    @Test(dependsOnMethods = "testWorkspaceEncoding")
    public void testWorkspaceDecoding() {
        // mocking DocumentCodec
        final DocumentCodec documentCodec = mock(DocumentCodec.class);
        when(documentCodec.getEncoderClass()).thenReturn(Document.class);
        final WorkspaceImplCodec codec = new WorkspaceImplCodec(CodecRegistries.fromCodecs(documentCodec));

        // get encoded document instance
        final Document[] documentHolder = new Document[1];
        doAnswer(invocation -> {
            if ("encode".equals(invocation.getMethod().getName())) {
                documentHolder[0] = (Document)invocation.getArguments()[1];
            }
            return null;
        }).when(documentCodec).encode(any(), any(), any());

        // prepare test workspace
        final WorkspaceImpl workspace = createWorkspace();

        // encode workspace
        codec.encode(null, workspace, null);

        // mocking document codec to return encoded workspace
        when(documentCodec.decode(any(), any())).thenReturn(documentHolder[0]);

        final WorkspaceImpl result = codec.decode(null, null);

        assertEquals(result, workspace);
    }

    private static WorkspaceImpl createWorkspace() {
        // environments
        Map<String, EnvironmentImpl> environments = new HashMap<>();

        Map<String, ExtendedMachineImpl> machines;
        Map<String, ServerConf2Impl> servers;
        Map<String, String> properties;
        EnvironmentImpl env;

        servers = new HashMap<>();
        properties = new HashMap<>();
        properties.put("prop1", "value1");
        properties.put("prop2", "value2");
        servers.put("ref1", new ServerConf2Impl("port1", "proto1", properties));
        properties = new HashMap<>();
        properties.put("prop3", "value3");
        properties.put("prop4", "value4");
        servers.put("ref2", new ServerConf2Impl("port2", "proto2", properties));
        machines = new HashMap<>();
        machines.put("machine1", new ExtendedMachineImpl(asList("org.eclipse.che.ws-agent", "someAgent"),
                                                         servers,
                                                         new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        servers = new HashMap<>();
        properties = new HashMap<>();
        properties.put("prop5", "value5");
        properties.put("prop6", "value6");
        servers.put("ref3", new ServerConf2Impl("port3", "proto3", properties));
        properties = new HashMap<>();
        properties.put("prop7", "value7");
        properties.put("prop8", "value8");
        servers.put("ref4", new ServerConf2Impl("port4", "proto4", properties));
        machines = new HashMap<>();
        machines.put("machine2", new ExtendedMachineImpl(asList("ws-agent2", "someAgent2"),
                                                         servers,
                                                         new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        env = new EnvironmentImpl();
        env.setRecipe(new EnvironmentRecipeImpl("type", "contentType", "content", null));
        env.setMachines(machines);

        environments.put("my-environment", env);

        env = new EnvironmentImpl();
        servers = new HashMap<>();
        properties = new HashMap<>();
        servers.put("ref11", new ServerConf2Impl("port11", "proto11", properties));
        servers.put("ref12", new ServerConf2Impl("port12", "proto12", null));
        machines = new HashMap<>();
        machines.put("machine11", new ExtendedMachineImpl(emptyList(),
                                                          servers,
                                                          new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        servers.put("ref13", new ServerConf2Impl("port13", "proto13", singletonMap("prop11", "value11")));
        servers.put("ref14", new ServerConf2Impl("port4", null, null));
        servers.put("ref15", new ServerConf2Impl(null, null, null));
        machines.put("machine12", new ExtendedMachineImpl(null,
                                                          servers,
                                                          new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        machines.put("machine13", new ExtendedMachineImpl(null,
                                                          null,
                                                          new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        env.setRecipe(new EnvironmentRecipeImpl("type", "contentType", "content", null));
        env.setMachines(machines);

        environments.put("my-environment-2", env);

        env = new EnvironmentImpl();
        env.setRecipe(new EnvironmentRecipeImpl(null, null, null, null));
        env.setMachines(null);

        environments.put("my-environment-3", env);

        // projects
        final ProjectConfigImpl project1 = new ProjectConfigImpl();
        project1.setName("test-project-name");
        project1.setDescription("This is test project");
        project1.setPath("/path/to/project");
        project1.setType("maven");
        project1.setMixins(singletonList("git"));

        final Map<String, List<String>> projectAttrs = new HashMap<>(4);
        projectAttrs.put("project.attribute1", singletonList("value1"));
        projectAttrs.put("project.attribute2", asList("value2", "value3"));
        project1.setAttributes(projectAttrs);

        final Map<String, String> sourceParameters = new HashMap<>(4);
        sourceParameters.put("source-parameter-1", "value1");
        sourceParameters.put("source-parameter-2", "value2");
        project1.setSource(new SourceStorageImpl("sources-type", "sources-location", sourceParameters));

        final List<ProjectConfigImpl> projects = singletonList(project1);

        // commands
        final List<CommandImpl> commands = new ArrayList<>(3);
        commands.add(new CommandImpl("MCI", "mvn clean install", "maven"));
        commands.add(new CommandImpl("bower install", "bower install", "bower"));
        commands.add(new CommandImpl("build without tests", "mvn clean install -Dmaven.test.skip", "maven"));

        // attributes
        final Map<String, String> attributes = new HashMap<>(8);
        attributes.put("test.attribute1", "test-value1");
        attributes.put("test.attribute2", "test-value2");
        attributes.put("test.attribute3", "test-value3");

        return WorkspaceImpl.builder()
                            .setId(generate("workspace", 16))
                            .setConfig(WorkspaceConfigImpl.builder()
                                                          .setName("workspace-name")
                                                          .setDescription("This is test workspace")
                                                          .setCommands(commands)
                                                          .setProjects(projects)
                                                          .setEnvironments(environments)
                                                          .setDefaultEnv("my-environment")
                                                          .build())
                            .setAttributes(attributes)
                            .setNamespace("user123")
                            .build();
    }

    private MongoDatabase mockDatabase(Consumer<MongoCollection<WorkspaceImpl>> consumer) {
        @SuppressWarnings("unchecked")
        final MongoCollection<WorkspaceImpl> collection = mock(MongoCollection.class);
        consumer.accept(collection);

        final MongoDatabase database = mock(MongoDatabase.class);
        when(database.getCollection("workspaces", WorkspaceImpl.class)).thenReturn(collection);

        return database;
    }
}
