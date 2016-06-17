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
package com.codenvy.api.dao.mongo.recipe;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.dao.RecipeDao;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.api.dao.mongo.MongoUtil.asDBList;
import static com.codenvy.api.dao.mongo.MongoUtil.handleWriteConflict;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.mongodb.client.model.Filters.all;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.elemMatch;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.or;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link RecipeDao} based on MongoDB storage
 * <pre>
 * Database schema:
 * {
 *     "_id" : "recipe12345...",
 *     "name" : "recipe-name",
 *     "creator: "user12345...",
 *     "type" : "recipe-type",
 *     "script" : "script-content",
 *     "tags" : [ "tag1", "tag2" ],
 *     "acl" : [
 *         {
 *             "user" : "user12345...",
 *             "actions" : [ "read", "delete", "setPermissions" ]
 *         }
 *     ]
 * }
 * </pre>
 *
 * @author Eugene Voevodin
 * @author Sergii Leschenko
 */
@Singleton
public class RecipeDaoImpl implements RecipeDao {
    private final MongoCollection<RecipeImpl> recipes;

    @Inject
    public RecipeDaoImpl(@Named("mongo.db.organization") MongoDatabase mongoDatabase,
                         @Named("organization.storage.db.recipes.collection") String collectionName) {
        recipes = mongoDatabase.getCollection(collectionName, RecipeImpl.class);
        recipes.createIndex(new BasicDBObject("creator", 1));
        //TODO consider adding index on 'type' field
        //why NOT - for now we have the only type of recipe
        //why YES - if we got different recipe types it will improve search performance
    }

    @Override
    public void create(RecipeImpl recipe) throws ServerException, ConflictException {
        requireNonNull(recipe, "Recipe required");
        try {
            recipes.insertOne(recipe);
        } catch (MongoWriteException writeEx) {
            handleWriteConflict(writeEx, format("Recipe with id '%s' already exists", recipe.getId()));
        } catch (MongoException mongoEx) {
            throw new ServerException(mongoEx.getMessage(), mongoEx);
        }
    }

    @Override
    public RecipeImpl update(RecipeImpl recipe) throws ServerException, NotFoundException {
        requireNonNull(recipe, "Recipe required");
        final BasicDBObject dbUpdate = new BasicDBObject();
        if (!isNullOrEmpty(recipe.getType())) {
            dbUpdate.append("type", recipe.getType());
        }
        if (!isNullOrEmpty(recipe.getScript())) {
            dbUpdate.append("script", recipe.getScript());
        }
        if (!isNullOrEmpty(recipe.getName())) {
            dbUpdate.append("name", recipe.getName());
        }
        if (!recipe.getTags().isEmpty()) {
            dbUpdate.append("tags", asDBList(recipe.getTags()));
        }
        if (recipe.getAcl() != null && !recipe.getAcl().isEmpty()) {
            dbUpdate.append("acl", asDBList(recipe.getAcl()));
        }

        try {
            RecipeImpl newRecipe = recipes.findOneAndUpdate(Filters.eq("_id", recipe.getId()), new Document("$set", dbUpdate));
            if (newRecipe == null) {
                throw new NotFoundException("Recipe with id '" + recipe.getId() + "' was not found");
            }
            return newRecipe;
        } catch (MongoException ex) {
            throw new ServerException("Impossible to update recipe", ex);
        }
    }

    @Override
    public void remove(String id) throws ServerException {
        requireNonNull(id, "Recipe id required");
        recipes.findOneAndDelete(eq("_id", id));
    }

    @Override
    public RecipeImpl getById(String id) throws ServerException, NotFoundException {
        requireNonNull(id, "Recipe id required");
        final FindIterable<RecipeImpl> findIt = recipes.find(eq("_id", id));
        if (findIt.first() == null) {
            throw new NotFoundException(format("Recipe with id '%s' was not found", id));
        }
        return findIt.first();
    }

    @Override
    public List<RecipeImpl> search(String user, List<String> tags, String type, int skipCount, int maxItems) throws ServerException {
        try {
            Bson query = elemMatch("acl", and(in("actions", "search"),
                                              or(eq("user", "*"),
                                                 eq("user", user))));
            if (tags != null && !tags.isEmpty()) {
                query = and(query, all("tags", tags));
            }

            if (!Strings.isNullOrEmpty(type)) {
                query = and(query, eq("type", type));
            }

            return recipes.find(query)
                          .skip(skipCount)
                          .limit(maxItems)
                          .into(new ArrayList<>());
        } catch (MongoException mongoEx) {
            throw new ServerException("Impossible to retrieve stacks. ", mongoEx);
        }
    }
}
