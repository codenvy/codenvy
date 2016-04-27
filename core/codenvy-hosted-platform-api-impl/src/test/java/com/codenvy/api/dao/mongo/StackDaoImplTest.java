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
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.recipe.GroupImpl;
import org.eclipse.che.api.machine.server.recipe.PermissionsImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.shared.Group;
import org.eclipse.che.api.machine.shared.Permissions;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackSourceImpl;
import org.eclipse.che.api.workspace.shared.stack.StackComponent;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.codenvy.api.dao.mongo.MongoUtil.documentsListAsMap;
import static com.codenvy.api.dao.mongo.MongoUtilTest.mockWriteEx;
import static com.mongodb.ErrorCategory.DUPLICATE_KEY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Test for {@link StackDaoImpl}
 *
 * @author Alexander Andrienko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class StackDaoImplTest extends BaseDaoTest {
    private static final String BD_COLLECTION_NAME = "stacks";
    private MongoCollection<StackImpl> collection;

    //variables for init test stackTest
    private static final String ID_TEST           = "randomId123";
    private static final String NAME              = "Java";
    private static final String DESCRIPTION       = "Simple java stackTest for generation java projects";
    private static final String USER_ID           = "user123";
    private static final String CREATOR           = USER_ID;
    private static final String SCOPE             = "advanced";
    private static final String SOURCE_TYPE       = "image";
    private static final String SOURCE_ORIGIN     = "codenvy/ubuntu_jdk8";
    private static final String COMPONENT_NAME    = "Java";
    private static final String COMPONENT_VERSION = "1.8_45";

    private List<String>        tags      = Arrays.asList("java", "maven");
    private WorkspaceConfigImpl workspace = createWorkspace();
    private StackImpl stackTest;
    private StackImpl stackTest2;

    private StackDaoImpl stackDao;

    @Captor
    ArgumentCaptor<Document> documentHolderCaptor;

    @BeforeMethod
    public void setUp() throws ServerException {
        final Fongo fongo = new Fongo("Stack test server");
        final CodecRegistry defaultRegistry = MongoClient.getDefaultCodecRegistry();

        StackImplCodec codec = new StackImplCodec(defaultRegistry);
        database = fongo.getDatabase("stacks").withCodecRegistry(fromRegistries(defaultRegistry, fromCodecs(codec)));
        collection = database.getCollection(BD_COLLECTION_NAME, StackImpl.class);

        stackDao = new StackDaoImpl(database, "stacks");

        List<String> tags = asList("Java", "Maven");
        StackComponentImpl stackComponent = new StackComponentImpl("some component", "1.0.0");
        StackSourceImpl stackSource = new StackSourceImpl("location", "http://some/url");
        Map<String, List<String>> users = singletonMap("user", singletonList("read"));
        List<Group> groups = singletonList(new GroupImpl("public", null, singletonList("search")));
        PermissionsImpl permissions = new PermissionsImpl(users, groups);
        stackTest = StackImpl.builder().setId("testId")
                             .setName("name")
                             .setCreator("creator")
                             .setDescription("description")
                             .setScope("advanced")
                             .setTags(tags)
                             .setComponents(singletonList(stackComponent))
                             .setSource(stackSource)
                             .setPermissions(permissions)
                             .build();
        stackTest2 = StackImpl.builder()
                              .setId("testId2")
                              .setCreator("creator")
                              .setScope("advanced")
                              .setSource(stackSource)
                              .setTags(tags)
                              .setPermissions(permissions)
                              .build();
    }

    @Test
    public void stackShouldBeCreated() throws ConflictException, ServerException {
        stackDao.create(stackTest);

        StackImpl StackImpl = collection.find(Filters.eq("_id", stackTest.getId())).first();

        assertEquals(StackImpl, stackTest);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Stack with id 'testId' already exists")
    public void shouldThrowConflictExceptionWhenStackIsAlreadyCreated() throws ConflictException, ServerException {
        final MongoDatabase db = mockDatabase(col -> doThrow(mockWriteEx(DUPLICATE_KEY)).when(col).insertOne(any()));
        new StackDaoImpl(db, "stacks").create(stackTest);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Stack required")
    public void shouldThrowNullPointerExceptionWhenStackForCreationIsNull() throws ConflictException, ServerException {
        stackDao.create(null);
    }

    @Test
    public void stackByIdShouldBeReturned() throws NotFoundException, ServerException {
        collection.insertOne(stackTest);

        StackImpl result = stackDao.getById("testId");

        assertEquals(stackTest, result);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenStackByIdWasNotFound() throws NotFoundException, ServerException {
        stackDao.getById("id");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Stack id required")
    public void shouldThrowNullPointerExceptionWhenStackIsNull() throws NotFoundException, ServerException {
        stackDao.getById(null);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Stack id required")
    public void shouldThrowNullPointerExceptionWhenWeTryRemoveNull() throws ServerException {
        stackDao.remove(null);
    }

    @Test
    public void stackShouldBeRemoved() throws ServerException {
        collection.insertOne(stackTest);

        stackDao.remove(stackTest.getId());

        StackImpl StackImpl = collection.find(Filters.eq("_id", stackTest.getId())).first();
        assertTrue(StackImpl == null);
    }

    @Test
    public void stackShouldBeUpdated() throws NotFoundException, ServerException {
        collection.insertOne(stackTest);

        StackImpl updateStack = new StackImpl(stackTest);
        updateStack.setName("NewName");
        updateStack.setScope("general");

        stackDao.update(updateStack);

        StackImpl expected = collection.find(Filters.eq("_id", stackTest.getId())).first();
        assertEquals(updateStack, expected);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Stack for updating required")
    public void shouldThrowNullPointerExceptionWhenWeTryUpdateNullStack() throws NotFoundException, ServerException {
        stackDao.update(null);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Required non-null stack id")
    public void shouldThrowNullPointerExceptionWhenIdStackForUpdateIsNull() throws NotFoundException, ServerException {
        stackDao.update(StackImpl.builder().build());
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Stack with id 'testId' was not found")
    public void shouldThrowNotFoundExceptionWhenStackTargetForUpdateIsNull() throws NotFoundException, ServerException {
        stackDao.update(stackTest);
    }

    @Test
    public void stacksByCreatorShouldBeReturned() throws ServerException {
        collection.insertOne(stackTest);
        collection.insertOne(stackTest2);

        List<StackImpl> StackImpls = stackDao.getByCreator("creator", 0, 30);
        List<StackImpl> expected = collection.find(Filters.eq("creator", "creator")).into(new ArrayList<>());

        assertEquals(StackImpls.size(), 2);
        assertTrue(StackImpls.equals(expected));
    }

    @Test
    public void shouldReturnEmptyListWhenWereFoundStacksByCreator() throws ServerException {
        List<StackImpl> StackImpls = stackDao.getByCreator("creator", 0, 30);
        assertTrue(StackImpls.isEmpty());
    }

    @Test
    public void shouldReturnOnlyOneStackByCreator() throws ServerException {
        collection.insertOne(stackTest);
        collection.insertOne(stackTest2);

        List<StackImpl> StackImpls = stackDao.getByCreator("creator", 0, 1);
        StackImpl expected = collection.find(Filters.eq("creator", "creator")).first();

        assertEquals(StackImpls.size(), 1);
        assertTrue(StackImpls.get(0).equals(expected));
    }

    @Test
    public void stacksByTagsShouldBeFound() throws ServerException {
        collection.insertOne(stackTest);
        collection.insertOne(stackTest2);

        List<String> tags = singletonList("Java");
        List<StackImpl> StackImpls = stackDao.searchStacks(tags, 0, 30);
        List<StackImpl> expected = collection.find(Filters.eq("creator", "creator")).into(new ArrayList<>());

        assertEquals(StackImpls.size(), 2);
        assertTrue(StackImpls.equals(expected));
    }

    @Test
    public void shouldReturnOneStackByTags() throws ServerException {
        collection.insertOne(stackTest);
        collection.insertOne(stackTest2);

        List<String> tags = singletonList("Java");
        List<StackImpl> StackImpls = stackDao.searchStacks(tags, 0, 1);
        StackImpl expected = collection.find(Filters.eq("creator", "creator")).first();

        assertEquals(StackImpls.size(), 1);
        assertTrue(StackImpls.get(0).equals(expected));
    }

    // suppress warnings about unchecked cast because we have to cast Mongo documents to lists
    @SuppressWarnings("unchecked")
    @Test
    public void encodeStackTest() {
        // mocking DocumentCodec
        final DocumentCodec documentCodec = mock(DocumentCodec.class);
        when(documentCodec.getEncoderClass()).thenReturn(Document.class);
        StackImplCodec stackImplCodec = new StackImplCodec(CodecRegistries.fromCodecs(documentCodec));

        //prepare test stackTest
        final StackImpl stack = createStack();

        //launch test action
        stackImplCodec.encode(null, stack, null);

        verify(documentCodec).encode(any(), documentHolderCaptor.capture(), any());
        Document stackDocument = documentHolderCaptor.getValue();

        // check encoding result
        assertEquals(stackDocument.getString("_id"), stack.getId(), "Stack id");
        assertEquals(stackDocument.getString("name"), stack.getName(), "Stack name");
        assertEquals(stackDocument.getString("description"), stack.getDescription(), "Stack description");
        assertEquals(stackDocument.getString("scope"), stack.getScope(), "Stack scope");
        assertEquals(stackDocument.getString("creator"), stack.getCreator(), "Stack creator");

        assertEquals((List<String>)stackDocument.get("tags"), tags, "Stack tags");

        Document sourceDocument = (Document)stackDocument.get("source");
        assertEquals(sourceDocument.getString("type"), SOURCE_TYPE, "Stack source type");
        assertEquals(sourceDocument.getString("origin"), SOURCE_ORIGIN, "Stack source origin");

        List<Document> components = (List<Document>)stackDocument.get("components");
        Document componentDocument = components.get(0);
        assertEquals(componentDocument.getString("name"), COMPONENT_NAME, "Stack component type");
        assertEquals(componentDocument.getString("version"), COMPONENT_VERSION, "Stack component origin");

        //verify workspaceConfig
        Document worspaceDocument = (Document)stackDocument.get("workspaceConfig");

        assertEquals(worspaceDocument.getString("name"), workspace.getName(), "Workspace name");
        assertEquals(worspaceDocument.getString("description"), workspace.getDescription(), "Workspace description");
        assertEquals(worspaceDocument.getString("defaultEnv"), workspace.getDefaultEnv(), "Workspace defaultEnvName");

        // check commands
        final List<Document> commands = (List<Document>)worspaceDocument.get("commands");
        assertEquals(commands.size(), workspace.getCommands().size(), "Workspace commands size");
        for (int i = 0; i < commands.size(); i++) {
            final Command command = workspace.getCommands().get(i);
            final Document document = commands.get(i);

            assertEquals(document.getString("name"), command.getName(), "Command name");
            assertEquals(document.getString("commandLine"), command.getCommandLine(), "Command line");
            assertEquals(document.getString("type"), command.getType(), "Command type");
        }

        // check projects
        final List<Document> projects = (List<Document>)worspaceDocument.get("projects");
        assertEquals(projects.size(), workspace.getProjects().size());
        for (int i = 0; i < projects.size(); i++) {
            final ProjectConfig project = workspace.getProjects().get(i);
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
        final List<Document> environments = (List<Document>)worspaceDocument.get("environments");
        assertEquals(environments.size(), workspace.getEnvironments().size());
        for (Document envDoc : environments) {
            String envName = envDoc.getString("name");
            final EnvironmentImpl environment = workspace.getEnvironments()
                                                         .stream()
                                                         .filter(environmentState -> envName.equals(environmentState.getName()))
                                                         .collect(toList()).get(0);

            assertEquals(envDoc.getString("name"), environment.getName());

            if (environment.getRecipe() != null) {
                final Document document = envDoc.get("recipe", Document.class);
                assertEquals(document.getString("type"), environment.getRecipe().getType(), "Environment recipe type");
                assertEquals(document.getString("script"), environment.getRecipe().getScript(), "Environment recipe script");
            }

            final List<Document> machineConfigs = (List<Document>)envDoc.get("machineConfigs");
            assertEquals(machineConfigs.size(), environment.getMachineConfigs().size());
            for (int i = 0; i < machineConfigs.size(); i++) {
                final Document machineDoc = machineConfigs.get(i);
                final MachineConfigImpl machine = environment.getMachineConfigs().get(i);

                assertEquals(machineDoc.getBoolean("isDev"), Boolean.valueOf(machine.isDev()));
                assertEquals(machineDoc.getString("name"), machine.getName(), "Machine name");
                assertEquals(machineDoc.getString("type"), machine.getType(), "Machine type");
                if (machine.getSource() != null) {
                    final Document sourceDoc = machineDoc.get("source", Document.class);

                    assertEquals(sourceDoc.getString("type"), machine.getSource().getType(), "Machine source type");
                    assertEquals(sourceDoc.getString("location"), machine.getSource().getLocation(), "Machine source location");
                }
                if (machine.getLimits() != null) {
                    final Document limitsDoc = machineDoc.get("limits", Document.class);

                    assertEquals(limitsDoc.getInteger("ram", 0), machine.getLimits().getRam(), "Machine RAM limit");
                }

                List<Document> serversDocs = (List<Document>)machineDoc.get("servers");
                assertEquals(serversDocs.size(), machine.getServers().size());
                for (int j = 0; j < serversDocs.size(); ++j) {
                    Document serverDoc = serversDocs.get(j);
                    ServerConfImpl server = machine.getServers().get(j);
                    assertEquals(serverDoc.getString("ref"), server.getRef());
                    assertEquals(serverDoc.getString("port"), server.getPort());
                    assertEquals(serverDoc.getString("protocol"), server.getProtocol());
                    assertEquals(serverDoc.getString("path"), server.getPath());
                }

                List<Document> envVariablesDocs = (List<Document>)machineDoc.get("envVariables");
                assertEquals(envVariablesDocs.size(), machine.getEnvVariables().size());
                for (Document envVarDoc : envVariablesDocs) {
                    final String envVarName = envVarDoc.getString("name");
                    final String envVarValue = envVarDoc.getString("value");

                    assertEquals(machine.getEnvVariables().get(envVarName), envVarValue, "envVariable value");
                }
            }
        }
    }

    @Test
    public void decodeStackTest() {
        // mocking DocumentCodec
        final DocumentCodec documentCodec = mock(DocumentCodec.class);
        when(documentCodec.getEncoderClass()).thenReturn(Document.class);
        final StackImplCodec stackImplCodec = new StackImplCodec(CodecRegistries.fromCodecs(documentCodec));

        // prepare test workspace
        final StackImpl stack = createStack();

        // encode workspace
        stackImplCodec.encode(null, stack, null);

        verify(documentCodec).encode(any(), documentHolderCaptor.capture(), any());
        Document document = documentHolderCaptor.getValue();

        // mocking document codec to return encoded workspace
        when(documentCodec.decode(any(), any())).thenReturn(document);

        final StackImpl result = stackImplCodec.decode(null, null);

        assertEquals(result, stack);
    }

    private StackImpl createStack() {
        List<StackComponent> componentsImpl = Collections.singletonList(new StackComponentImpl(COMPONENT_NAME, COMPONENT_VERSION));
        StackSourceImpl source = new StackSourceImpl(SOURCE_TYPE, SOURCE_ORIGIN);
        List<String> list = Arrays.asList("read", "write");
        Map<String, List<String>> users = new HashMap<>();
        users.put("user", list);
        List<Group> groups = Collections.singletonList(new GroupImpl("public", null, list));
        Permissions permissions = new PermissionsImpl(users, groups);
        return StackImpl.builder()
                        .setId(ID_TEST)
                        .setName(NAME)
                        .setDescription(DESCRIPTION)
                        .setScope(SCOPE)
                        .setCreator(CREATOR)
                        .setTags(tags)
                        .setSource(source)
                        .setWorkspaceConfig(workspace)
                        .setComponents(componentsImpl)
                        .setPermissions(permissions)
                        .build();
    }

    private static WorkspaceConfigImpl createWorkspace() {
        // environments
        final RecipeImpl recipe = new RecipeImpl();
        recipe.setType("dockerfile");
        recipe.setScript("FROM codenvy/jdk7\nCMD tail -f /dev/null");

        final MachineSourceImpl machineSource = new MachineSourceImpl("recipe", "recipe-url");
        final MachineConfigImpl machineCfg1 = new MachineConfigImpl(true,
                                                                    "dev-machine",
                                                                    "machine-type",
                                                                    machineSource,
                                                                    new LimitsImpl(512),
                                                                    Arrays.asList(new ServerConfImpl("ref1",
                                                                                                     "8080",
                                                                                                     "https",
                                                                                                     "some/path"),
                                                                                  new ServerConfImpl("ref2",
                                                                                                     "9090/udp",
                                                                                                     "someprotocol",
                                                                                                     "/some/path")),
                                                                    Collections.singletonMap("key1", "value1"));
        final MachineConfigImpl machineCfg2 = new MachineConfigImpl(false,
                                                                    "non-dev-machine",
                                                                    "machine-type-2",
                                                                    machineSource,
                                                                    new LimitsImpl(2048),
                                                                    Arrays.asList(new ServerConfImpl("ref1",
                                                                                                     "8080",
                                                                                                     "https",
                                                                                                     "some/path"),
                                                                                  new ServerConfImpl("ref2",
                                                                                                     "9090/udp",
                                                                                                     "someprotocol",
                                                                                                     "/some/path")),
                                                                    Collections.singletonMap("key1", "value1"));

        final EnvironmentImpl env1 = new EnvironmentImpl("my-environment", recipe, asList(machineCfg1, machineCfg2));
        final EnvironmentImpl env2 = new EnvironmentImpl("my-environment-2", recipe, singletonList(machineCfg1));

        final List<EnvironmentImpl> environments = new ArrayList<>(4);
        environments.add(env1);
        environments.add(env2);

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

        return WorkspaceConfigImpl.builder()
                                  .setName("workspace-name")
                                  .setDescription("This is test workspace")
                                  .setCommands(commands)
                                  .setProjects(projects)
                                  .setEnvironments(environments)
                                  .setDefaultEnv(env1.getName())
                                  .build();
    }

    private MongoDatabase mockDatabase(Consumer<MongoCollection<StackImpl>> consumer) {
        @SuppressWarnings("unchecked")
        final MongoCollection<StackImpl> collection = mock(MongoCollection.class);
        consumer.accept(collection);

        final MongoDatabase database = mock(MongoDatabase.class);
        when(database.getCollection("stacks", StackImpl.class)).thenReturn(collection);

        return database;
    }
}
