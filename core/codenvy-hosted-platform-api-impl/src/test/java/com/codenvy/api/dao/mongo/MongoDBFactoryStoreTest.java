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
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

import org.bson.Document;
import org.bson.codecs.BinaryCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.factory.server.FactoryImage;
import org.eclipse.che.api.factory.shared.dto.Action;
import org.eclipse.che.api.factory.shared.dto.Author;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.factory.shared.dto.Ide;
import org.eclipse.che.api.factory.shared.dto.OnAppLoaded;
import org.eclipse.che.api.factory.shared.dto.OnProjectsLoaded;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.machine.shared.dto.ServerConfDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests for {@link MongoDBFactoryStore}
 *
 */
public class MongoDBFactoryStoreTest {

    private static final String DB_NAME   = "test1";
    private static final String COLL_NAME = "factory1";
    private MongoCollection<Document> collection;
    private MongoDBFactoryStore       store;

    @BeforeMethod
    public void setUp() throws Exception {
        Fongo fongo = new Fongo("test server");
        final MongoDatabase database = fongo.getDatabase(DB_NAME).withCodecRegistry(fromRegistries(
                CodecRegistries.fromCodecs(new BinaryCodec()),
                MongoClient.getDefaultCodecRegistry()));
        collection = database.getCollection(COLL_NAME, Document.class);
        store = new MongoDBFactoryStore(database, COLL_NAME);
    }


    @Test
    public void shouldSaveFactory() throws Exception {
        Factory factory = DtoFactory.getInstance().createDto(Factory.class);
        factory.setV("4.0");

        factory.setCreator(DtoFactory.getInstance().createDto(Author.class)
                                     .withName("someAuthor")
                                     .withCreated(777777777L)
                                     .withEmail("test@test.com"));

        factory.setWorkspace(DtoFactory.getInstance().createDto(WorkspaceConfigDto.class)
                                       .withProjects(Collections.singletonList(DtoFactory.getInstance().createDto(
                                               ProjectConfigDto.class)
                                                                                         .withSource(
                                                                                                 DtoFactory.getInstance().createDto(
                                                                                                         SourceStorageDto.class)
                                                                                                           .withType("git")
                                                                                                           .withLocation("location"))
                                                                                         .withType("type")
                                                                                         .withAttributes(
                                                                                                 singletonMap("key",
                                                                                                              singletonList("value")))
                                                                                         .withDescription("description")
                                                                                         .withName("name")
                                                                                         .withPath("/path")))
                                       .withCommands(singletonList(DtoFactory.getInstance().createDto(CommandDto.class)
                                                                             .withName("command1")
                                                                             .withType("maven")
                                                                             .withCommandLine("mvn test")))
                                       .withDefaultEnv("env1")
                                       .withEnvironments(singletonList(DtoFactory.getInstance().createDto(EnvironmentDto.class)
                                                                                 .withName("test")
                                                                                 .withMachineConfigs(singletonList(
                                                                                         DtoFactory.getInstance().createDto(
                                                                                                 MachineConfigDto.class)
                                                                                                   .withName("name")
                                                                                                   .withType("docker")
                                                                                                   .withDev(true)
                                                                                                   .withSource(
                                                                                                           DtoFactory.getInstance()
                                                                                                                     .createDto(
                                                                                                                             MachineSourceDto.class)
                                                                                                                     .withType(
                                                                                                                             "git")
                                                                                                                     .withLocation(
                                                                                                                             "https://github.com/123/test.git"))
                                                                                                   .withServers(Arrays.asList(newDto(ServerConfDto.class).withRef("ref1")
                                                                                                                                                         .withPort("8080")
                                                                                                                                                         .withProtocol("https"),
                                                                                                                              newDto(ServerConfDto.class).withRef("ref2")
                                                                                                                                                         .withPort("9090/udp")
                                                                                                                                                         .withProtocol("someprotocol")))
                                                                                                   .withEnvVariables(Collections.singletonMap("key1", "value1"))
                                                                                 ))
                                                                                 .withRecipe(DtoFactory.getInstance().createDto(
                                                                                         RecipeDto.class)
                                                                                                       .withType("sometype")
                                                                                                       .withScript("some script")))));

        Ide ide = DtoFactory.getInstance().createDto(Ide.class)
                            .withOnAppLoaded(DtoFactory.getInstance().createDto(OnAppLoaded.class))
                            .withOnProjectsLoaded(DtoFactory.getInstance().createDto(OnProjectsLoaded.class));

        Action welcomePage = DtoFactory.getInstance().createDto(Action.class).withId("openWelcomePage");
        Map<String, String> welcomePageProperties = new HashMap<>();
        welcomePageProperties.put("authenticatedContentUrl", "content");
        welcomePageProperties.put("authenticatedTitle", "title");
        welcomePageProperties.put("authenticatedNotification", "notification");
        welcomePageProperties.put("nonAuthenticatedContentUrl", "content");
        welcomePageProperties.put("nonAuthenticatedTitle", "title");
        welcomePageProperties.put("nonAuthenticatedNotification", "notification");
        welcomePage.setProperties(welcomePageProperties);

        ide.getOnAppLoaded().getActions().add(welcomePage);

        Action findReplace = DtoFactory.getInstance().createDto(Action.class).withId("findReplace");
        Map<String, String> findReplaceProperties = new HashMap<>();
        findReplaceProperties.put("in", "content");
        findReplaceProperties.put("find", "title");
        findReplaceProperties.put("replace", "notification");
        findReplaceProperties.put("replaceMode", "content");
        findReplace.setProperties(findReplaceProperties);

        ide.getOnProjectsLoaded().getActions().add(findReplace);

        Set<FactoryImage> images = new HashSet<>();
        String id = store.saveFactory(factory, images);


        Factory result = store.getFactory(id);
        factory.setId(id);

        assertEquals(result, factory);
    }


    /**
     * Checks we can't save a null factory
     * Expects a shouldNotSaveNullFactory exception
     * @throws Exception
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void shouldNotSaveNullFactory() throws Exception {
        store.saveFactory(null, null);
    }

    @Test
    public void shouldRemoveFactory() throws Exception {
        String id = "123412341";
        Document obj = new Document("_id", id).append("key", "value");
        collection.insertOne(obj);

        store.removeFactory(id);

        assertNull(collection.find(new Document("_id", id)).first());
    }
    
    @Test(expectedExceptions = ConflictException.class)
    public void shouldNotSaveFactoryWithSameNameAndUser() throws Exception {

        Set<FactoryImage> images = new HashSet<>();

        Factory factory1 = DtoFactory.getInstance().createDto(Factory.class)
                                        .withName("testName")
                                        .withCreator(DtoFactory.getInstance().createDto(Author.class)
                                                               .withUserId("userOK"));

        Factory factory2 = DtoFactory.getInstance().createDto(Factory.class)
                                        .withName("testName")
                                        .withCreator(DtoFactory.getInstance().createDto(Author.class)
                                                               .withUserId("userOK"))
                                        .withWorkspace(DtoFactory.getInstance().createDto(WorkspaceConfigDto.class)
                                                                 .withName("wsName"));

        store.saveFactory(factory1, images);
        store.saveFactory(factory2, images);
    }



    @Test
    public void shouldGetFactoryImages() throws Exception {
        String id = "testid1234314";

        Set<FactoryImage> images = new HashSet<>();
        FactoryImage image = new FactoryImage();
        byte[] b = new byte[4096];
        new Random().nextBytes(b);
        image.setName("test123.jpg");
        image.setMediaType("image/jpeg");
        image.setImageData(b);
        images.add(image);

        List<DBObject> imageList =
                images.stream().map(one -> new BasicDBObjectBuilder().add("name", NameGenerator.generate("", 16) + one.getName())
                                                                     .add("type", one.getMediaType())
                                                                     .add("data", one.getImageData()).get()).collect(Collectors.toList());

        Document factoryBuilder = new Document();

        Document factoryData = new Document();
        factoryData.append("_id", id);
        factoryData.append("factory", factoryBuilder);
        factoryData.append("images", imageList);

        collection.insertOne(factoryData);

        Set<FactoryImage> newImages = store.getFactoryImages(id, null);
        assertNotNull(newImages);
        FactoryImage newImage = newImages.iterator().next();

        assertTrue(newImage.getName().endsWith(image.getName()));
        assertEquals(newImage.getMediaType(), image.getMediaType());
        assertEquals(newImage.getImageData(), image.getImageData());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldGetFactoryByAttributes() throws Exception {

        Set<FactoryImage> images = new HashSet<>();

        Factory factory1 = DtoFactory.getInstance().createDto(Factory.class)
                                        .withCreator(DtoFactory.getInstance().createDto(Author.class)
                                                               .withUserId("userOK"));

        Factory factory2 = DtoFactory.getInstance().createDto(Factory.class)
                                        .withCreator(DtoFactory.getInstance().createDto(Author.class)
                                                               .withUserId("userOK"))
                                        .withWorkspace(DtoFactory.getInstance().createDto(WorkspaceConfigDto.class)
                                                                 .withName("wsName"));

        Factory factory3 = DtoFactory.getInstance().createDto(Factory.class)
                                        .withCreator(DtoFactory.getInstance().createDto(Author.class)
                                                               .withUserId("userOK"))
                                        .withWorkspace(DtoFactory.getInstance().createDto(WorkspaceConfigDto.class)
                                                                 .withName("wsName")
                                                                 .withProjects(Collections.singletonList(
                                                                         newDto(ProjectConfigDto.class)
                                                                                   .withType("projectType"))));


        store.saveFactory(factory1, images);
        store.saveFactory(factory2, images);
        store.saveFactory(factory3, images);

        assertEquals(store.findByAttribute(0, 0, Collections.singletonList(Pair.of("creator.userId", "userOK"))).size(), 3);
        assertEquals(store.findByAttribute(0, 0, Collections.singletonList(Pair.of("workspace.name", "wsName"))).size(), 2);
        assertEquals(store.findByAttribute(0, 0, Arrays.asList(Pair.of("workspace.name", "wsName"),
                                                               Pair.of("workspace.projects.type", "projectType"))).size(), 1);
        assertEquals(store.findByAttribute(0, 0, Arrays.asList(Pair.of("creator.userId", "userOK"),
                                                               Pair.of("workspace.projects.type", "projectType"))).size(), 1);
    }
    
    /**
     * Checks that we can update a factory that has images and images are unchanged after the update
     * @throws Exception if there is failure
     */
    @Test
    public void shouldUpdateFactory() throws Exception {
        Factory factory = DtoFactory.getInstance().createDto(Factory.class);
        factory.setV("4.0");

        factory.setCreator(DtoFactory.getInstance().createDto(Author.class)
                                     .withName("Florent")
                                     .withCreated(System.currentTimeMillis())
                                     .withEmail("test@codenvy.com"));

        factory.setWorkspace(newDto(WorkspaceConfigDto.class)
                                       .withName("wsName")
                                       .withProjects(Collections.singletonList(newDto(ProjectConfigDto.class).withSource(
                                               newDto(SourceStorageDto.class).withLocation("gitUrlInitial")))));



        // new Factory
        Factory updatedFactory = DtoFactory.getInstance().clone(factory);
        updatedFactory.getWorkspace().getProjects().get(0).getSource().setLocation("gitUrlChanged");


        Set<FactoryImage> images = new HashSet<>();
        FactoryImage image = new FactoryImage();
        byte[] b = new byte[4096];
        new Random().nextBytes(b);
        image.setName("test123.jpg");
        image.setMediaType("image/jpeg");
        image.setImageData(b);
        images.add(image);
        // First save a factory
        String id = store.saveFactory(factory, images);

        // now update factory
        store.updateFactory(id, updatedFactory);


        FindIterable<Document> list = collection.find(new Document("_id", id));
        Document res = list.first().get("factory", Document.class);

        Factory result = DtoFactory.getInstance().createDtoFromJson(JSON.serialize(res), Factory.class);

        // check factory has been modified
        assertEquals(result.getWorkspace().getProjects().get(0).getSource().getLocation(), "gitUrlChanged");


        // check images have not been modified
        Set<FactoryImage> newImages = store.getFactoryImages(id, null);
        assertNotNull(newImages);
        FactoryImage newImage = newImages.iterator().next();

        assertTrue(newImage.getName().endsWith(image.getName()));
        assertEquals(newImage.getMediaType(), image.getMediaType());
        assertEquals(newImage.getImageData(), image.getImageData());

    }

    @Test
    public void shouldUpdateUnnamedFactoryWithNewName() throws Exception {
        // given

        Factory factory = DtoFactory.getInstance().createDto(Factory.class);
        factory.setV("4.0");

        factory.setCreator(DtoFactory.getInstance().createDto(Author.class)
                                     .withName("Florent")
                                     .withCreated(System.currentTimeMillis())
                                     .withEmail("test@codenvy.com")
                                     .withUserId("1234567890"));
        String factoryId = null;
        try {
            factoryId = store.saveFactory(factory, null);
        } catch (Exception e) {
            fail("Unexpected exception occured", e);
        }

        factory.setName("new-name");

        // when
        // update factory
        store.updateFactory(factoryId, factory);

        // then
        FindIterable<Document> list = collection.find(new Document("_id", factoryId));
        Document res = list.first().get("factory", Document.class);

        Factory result = DtoFactory.getInstance().createDtoFromJson(JSON.serialize(res), Factory.class);
        assertEquals(result.getName(), "new-name");
    }

    /**
     * Check that exception is thrown when trying to update
     * a factory with name, which already belongs to another
     * existing factory
     * @throws Exception the ConflictException expected one
     */
    @Test(expectedExceptions = ConflictException.class)
    public void shouldNotUpdateFactoryWithConflictingName() throws Exception {
        // given
        // prepare original factories and save them

        Factory originalFactory = DtoFactory.getInstance().createDto(Factory.class);
        originalFactory.setV("4.0");
        originalFactory.setName("factory-1");

        originalFactory.setCreator(DtoFactory.getInstance().createDto(Author.class)
                                             .withName("Florent")
                                             .withCreated(System.currentTimeMillis())
                                             .withEmail("test@codenvy.com")
                                             .withUserId("1234567890"));
        String originalFactoryId = null;
        try {
            originalFactoryId = store.saveFactory(originalFactory, null);
        } catch (Exception e) {
            fail("Unexpected exception occured", e);
        }
;
        Factory updatedFactory = DtoFactory.getInstance().clone(originalFactory);
        updatedFactory.setName("factory-2");
        String updatedFactoryId;
        try {
            updatedFactoryId = store.saveFactory(updatedFactory, null);
        } catch (Exception e) {
            fail("Unexpected exception occured", e);
        }

        // when
        // update factory
        store.updateFactory(originalFactoryId, updatedFactory);
    }

    /**
     * Check that exception is thrown when using an unknown id
     * @throws Exception the NotFoundException expected one
     */
    @Test(expectedExceptions = NotFoundException.class)
    public void shouldNotUpdateUnexistingFactory() throws Exception {
        Factory factory = DtoFactory.getInstance().createDto(Factory.class);
        factory.setV("2.1");

        // new Factory
        Factory updatedFactory = DtoFactory.getInstance().clone(factory);

        // now update factory
        store.updateFactory("1234", updatedFactory);
    }


    /**
     * Check encoding with dot
     */
    @Test
    public void shouldEncodedDot() {
        String original = "hello.my.string";
        String encoded = store.encode(original);
        assertEquals(encoded, "hello" + MongoDBFactoryStore.ESCAPED_DOT + "my" + MongoDBFactoryStore.ESCAPED_DOT + "string");
        String decoded = store.decode(encoded);
        assertEquals(original, decoded);
    }

    /**
     * Check encoding with dollar
     */
    @Test
    public void shouldEncodedDollar() {
        String original = "hello$my$string";
        String encoded = store.encode(original);
        assertEquals("hello" + MongoDBFactoryStore.ESCAPED_DOLLAR + "my" + MongoDBFactoryStore.ESCAPED_DOLLAR + "string", encoded);
        String decoded = store.decode(encoded);
        assertEquals(original, decoded);
    }

    /**
     * Check encoding with dot and dollar
     */
    @Test
    public void shouldEncodedDotDollar() {
        String original = "hello.my$string";
        String encoded = store.encode(original);
        assertEquals("hello" + MongoDBFactoryStore.ESCAPED_DOT + "my" + MongoDBFactoryStore.ESCAPED_DOLLAR + "string", encoded);
        String decoded = store.decode(encoded);
        assertEquals(original, decoded);
    }

}
