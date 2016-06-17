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

import com.codenvy.api.dao.mongo.recipe.RecipeDaoImpl;
import com.codenvy.api.dao.mongo.recipe.RecipeImplCodec;
import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.codecs.configuration.CodecRegistry;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.acl.AclEntryImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.shared.ManagedRecipe;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests for {@link RecipeDaoImpl}
 *
 * @author Eugene Voevodin
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RecipeDaoImplTest {
    private MongoCollection<RecipeImpl> collection;

    private RecipeDaoImpl recipeDao;

    @BeforeMethod
    public void setUp() throws Exception {
        final Fongo fongo = new Fongo("Recipe test server");
        CodecRegistry codecRegistry = MongoClient.getDefaultCodecRegistry();
        codecRegistry = fromRegistries(codecRegistry, fromCodecs(new AclEntryImplCodec(codecRegistry)));
        codecRegistry = fromRegistries(codecRegistry, fromCodecs(new RecipeImplCodec(codecRegistry)));
        final MongoDatabase database = fongo.getDatabase("recipes")
                                            .withCodecRegistry(codecRegistry);
        collection = database.getCollection("recipes", RecipeImpl.class);
        recipeDao = new RecipeDaoImpl(database, "recipes");
    }

    @Test
    public void shouldBeAbleToGetRecipeById() throws Exception {
        final RecipeImpl example = new RecipeImpl().withId("recipe123")
                                                   .withName("name")
                                                   .withCreator("someone")
                                                   .withScript("script content")
                                                   .withType("script-type")
                                                   .withTags(asList("tag1", "tag2"))
                                                   .withAcl(singletonList(new AclEntryImpl("user123", asList("read", "update"))));
        collection.insertOne(example);

        final ManagedRecipe recipe = recipeDao.getById(example.getId());

        assertEquals(recipe, example);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Recipe with id 'fake' was not found")
    public void shouldThrowNotFoundExceptionWhenRecipeWithGivenIdWasNotFound() throws Exception {
        recipeDao.getById("fake");
    }

    @Test
    public void shouldBeAbleToCreateNewRecipe() throws Exception {
        final RecipeImpl toCreate = new RecipeImpl().withId("recipe123")
                                                    .withName("name")
                                                    .withCreator("someone")
                                                    .withScript("script content")
                                                    .withType("script-type")
                                                    .withTags(asList("tag1", "tag2"))
                                                    .withAcl(singletonList(new AclEntryImpl("user123", asList("read", "update"))));

        recipeDao.create(toCreate);

        final FindIterable<RecipeImpl> result = collection.find(eq("_id", toCreate.getId()));
        assertEquals(result.first(), toCreate);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Recipe with id 'recipe123' already exists")
    public void shouldThrowConflictExceptionWhenRecipeWithSameIdAsGivenRecipeIdAlreadyExists() throws Exception {
        collection.insertOne(new RecipeImpl().withId("recipe123"));

        recipeDao.create(new RecipeImpl().withId("recipe123"));
    }

    @Test
    public void shouldBeRemoveRecipeById() throws Exception {
        final RecipeImpl example = new RecipeImpl().withId("recipe123")
                                                   .withName("name")
                                                   .withCreator("someone")
                                                   .withScript("script content")
                                                   .withType("script-type")
                                                   .withTags(asList("tag1", "tag2"))
                                                   .withAcl(singletonList(new AclEntryImpl("user123", asList("read", "update"))));
        collection.insertOne(example);

        recipeDao.remove(example.getId());

        assertNull(collection.find(eq("_id", example.getId())).first());
    }

    @Test
    public void shouldBeAbleToUpdateRecipe() throws Exception {
        final RecipeImpl example = new RecipeImpl().withId("recipe123")
                                                   .withName("name")
                                                   .withCreator("someone")
                                                   .withScript("script content")
                                                   .withType("script-type")
                                                   .withTags(new ArrayList<>(asList("tag1", "tag2")))
                                                   .withAcl(singletonList(new AclEntryImpl("user123", asList("read", "update"))));
        collection.insertOne(example);

        //preparing update
        example.withName("new-name")
               .withType("new-type")
               .withScript("new-script")
               .withTags(singletonList("new-tags"));

        recipeDao.update(example);

        assertEquals(collection.find(eq("_id", example.getId())).first(), example);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Recipe with id 'fake' was not found")
    public void shouldThrowNotFoundExceptionIfRecipeWithIdInUpdateWasNotFound() throws Exception {
        recipeDao.update(new RecipeImpl().withId("fake"));
    }

    @Test
    public void shouldBeAbleToSearchByRecipes() throws Exception {
        final RecipeImpl example = new RecipeImpl().withId("recipe123")
                                                   .withName("name")
                                                   .withCreator("someone")
                                                   .withScript("script content")
                                                   .withType("script-type")
                                                   .withTags(asList("tag1", "tag2"))
                                                   .withAcl(singletonList(new AclEntryImpl("user123", singletonList("search"))));
        final RecipeImpl example2 = copy(example).withId("recipe234")
                                                 .withTags(asList("tag1", "tag2", "tag3"))
                                                 .withAcl(singletonList(new AclEntryImpl("user123", singletonList("update"))));
        collection.insertOne(example);
        collection.insertOne(example2);

        final List<RecipeImpl> recipes = recipeDao.search("user123", null, null, 0, 10);

        assertEquals(new HashSet<>(recipes), new HashSet<>(singletonList(example)));
    }

    @Test
    public void shouldBeAbleToSearchRecipesByTagsAndType() throws Exception {
        final RecipeImpl example = new RecipeImpl().withId("recipe123")
                                                   .withName("name")
                                                   .withCreator("someone")
                                                   .withScript("script content")
                                                   .withType("script-type")
                                                   .withTags(asList("tag1", "tag2"))
                                                   .withAcl(singletonList(new AclEntryImpl("user123", singletonList("search"))));
        final RecipeImpl example2 = copy(example).withId("recipe234")
                                                 .withTags(asList("tag1", "tag2", "tag3"));
        final RecipeImpl example3 = copy(example).withId("recipe345")
                                                 .withTags(singletonList("tag1"));
        final RecipeImpl example4 = copy(example).withId("recipe456")
                                                 .withType("another type");
        collection.insertOne(example);
        collection.insertOne(example2);
        collection.insertOne(example3);
        collection.insertOne(example4);

        final List<RecipeImpl> recipes = recipeDao.search("user123", asList("tag1", "tag2"), "script-type", 0, 10);

        assertEquals(new HashSet<>(recipes), new HashSet<>(asList(example, example2)));
    }

    @Test
    public void shouldBeAbleToSearchRecipesByTags() throws Exception {
        List<AclEntryImpl> acl = singletonList(new AclEntryImpl("user123", singletonList("search")));
        final RecipeImpl example = new RecipeImpl().withId("recipe123")
                                                   .withName("name")
                                                   .withCreator("someone")
                                                   .withScript("script content")
                                                   .withType("script-type")
                                                   .withTags(asList("tag1", "tag2"))
                                                   .withAcl(acl);
        final RecipeImpl example2 = copy(example).withId("recipe234")
                                                 .withTags(asList("tag1", "tag2", "tag3"))
                                                 .withAcl(acl);
        final RecipeImpl example3 = copy(example).withId("recipe345")
                                                 .withTags(singletonList("tag1"))
                                                 .withAcl(acl);
        final RecipeImpl example4 = copy(example).withId("recipe456")
                                                 .withType("another type")
                                                 .withAcl(acl);
        collection.insertOne(example);
        collection.insertOne(example2);
        collection.insertOne(example3);
        collection.insertOne(example4);

        final List<RecipeImpl> recipes = recipeDao.search("user123", asList("tag1", "tag2"), null, 0, 10);

        assertEquals(new HashSet<>(recipes), new HashSet<>(asList(example, example2, example4)));
    }

    private RecipeImpl copy(RecipeImpl recipe) {
        return new RecipeImpl().withId(recipe.getId())
                               .withName(recipe.getName())
                               .withCreator(recipe.getCreator())
                               .withType(recipe.getType())
                               .withScript(recipe.getScript())
                               .withTags(recipe.getTags())
                               .withAcl(recipe.getAcl());
    }
}
