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
package com.codenvy.api.dao.mongo.stack;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.conversions.Bson;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.codenvy.api.dao.mongo.MongoUtil.handleWriteConflict;
import static com.mongodb.client.model.Filters.all;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.elemMatch;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.or;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Implementation mongo storage for {@link StackImpl}
 *
 * <p>Stack collection document scheme:
 * <pre>
 * {
 *  "id": "stack1qerlsufdredfdre",
 *  "name": "Java",
 *  "description": "Default Java Stack with JDK 8, Maven and Tomcat.",
 *  "scope": "general",
 *  "creator": "userId12dsd",
 *  "tags": [
 *    "Java",
 *    "Maven",
 *    "JDK",
 *    "Tomcat",
 *    "Subversion",
 *    "Ubuntu",
 *    "Git"
 *  ],
 *  "workspaceConfig" : {
 *      "id" : "workspace123",
 *      "name" : "my-workspace",
 *      "description" : "This is workspace description",
 *      "owner" : "user123",
 *      "defaultEnv" : "dev-env",
 *      "commands" : [
 *          {
 *              "name" : "mci",
 *              "commandLine" : "maven clean install",
 *              "type" : "maven",
 *              "attributes" : [
 *                  {
 *                      "name" : "attribute1",
 *                      "value" : "value1"
 *                  }
 *               ]
 *          }
 *      ],
 *      "projects" : [
 *          {
 *              "name" : "my-project",
 *              "path" : "/path/to/project",
 *              "description" : "This is project description",
 *              "type" : "project-type",
 *              "source" : {
 *                  "type" : "storage-type",
 *                  "location" : "storage-location",
 *                  "parameters" : [
 *                      {
 *                          "name" : "parameter1",
 *                          "value" : "parameter-value"
 *                      }
 *                  ]
 *              },
 *              "modules": [
 *                  {
 *                      "name":"my-module",
 *                      "path":"/path/to/project/my-module",
 *                      "type":"maven",
 *                      "mixins" : [ "mixinType1", "mixinType2" ],
 *                      "description" : "This is module description",
 *                      "attributes" : [
 *                          {
 *                              "name" : "module-attribute-1",
 *                              "value" : [ "value1", "value2" ]
 *                          }
 *                      ]
 *                  }
 *              ],
 *              "mixins" : [ "mixinType1", "mixinType2" ],
 *              "attributes" : [
 *                  {
 *                      "name" : "project-attribute-1",
 *                      "value" : [ "value1", "value2" ]
 *                  }
 *              ]
 *          }
 *      ],
 *      "environments" : [
 *          {
 *              "name" : "dev-env",
 *              "recipe" : {
 *                  "type" : "dockerfile",
 *                  "script" : "FROM codenvy/jdk7\nCMD tail -f /dev/null"
 *              },
 *              "machineConfigs" : [
 *                  {
 *                      "isDev" : true,
 *                      "name" : "dev",
 *                      "type" : "machine-type",
 *                      "limits" : {
 *                          "ram" : 512
 *                      },
 *                      "source" : {
 *                          "type" : "recipe",
 *                          "location" : "recipe-url"
 *                      }
 *                  }
 *              ]
 *          }
 *      ],
 *      "attributes" : [
 *          {
 *              "name" : "attribute1",
 *              "value" : "value1"
 *          }
 *      ]
 * },
 *  "components": [
 *    {
 *      "name": "JDK",
 *      "version": "1.8.0_45"
 *    },
 *    {
 *      "name": "Maven",
 *    },
 *      "version": "3.2.2"
 *    {
 *      "name": "Tomcat",
 *      "version": "8.0.24"
 *    }
 *  ],
 *  "source": {
 *    "type": "image",
 *    "origin": "codenvy/ubuntu_jdk8"
 *  },
 *  "acl" : [
 *     {
 *         "user" : "user12345...",
 *         "actions" : [ "read", "delete"]
 *     }
 *  ],
 *  "stackIcon": {
 *    "name": "type-java.svg",
 *    "mediaType": "image/svg+xml"
 *  }
 * }
 * </pre>
 *
 * @author Alexander Andrienko
 */
@Singleton
public class StackDaoImpl implements StackDao {

    private final MongoCollection<StackImpl> collection;

    @Inject
    public StackDaoImpl(@Named("mongo.db.organization") MongoDatabase mongoDatabase,
                        @Named("organization.storage.db.stacks.collection") String collectionName) {
        collection = mongoDatabase.getCollection(collectionName, StackImpl.class);
        collection.createIndex(new BasicDBObject("creator", 1));
    }

    @Override
    public void create(StackImpl stack) throws ConflictException, ServerException {
        requireNonNull(stack, "Stack required");
        try {
            collection.insertOne(stack);
        } catch (MongoWriteException writeEx) {
            handleWriteConflict(writeEx, format("Stack with id '%s' already exists", stack.getId()));
        } catch (MongoException mongoEx) {
            throw new ServerException(mongoEx.getMessage(), mongoEx);
        }
    }

    @Override
    public StackImpl getById(String id) throws NotFoundException, ServerException {
        requireNonNull(id, "Stack id required");
        final FindIterable<StackImpl> findIt = collection.find(eq("_id", id));
        if (findIt.first() == null) {
            throw new NotFoundException(format("Stack with id '%s' was not found", id));
        }
        return findIt.first();
    }

    @Override
    public void remove(String id) throws ServerException {
        requireNonNull(id, "Stack id required");
        collection.findOneAndDelete(eq("_id", id));
    }

    @Override
    public StackImpl update(StackImpl update) throws NotFoundException, ServerException {
        requireNonNull(update, "Stack for updating required");
        requireNonNull(update.getId(), "Stack id required");
        try {
            if (update.getAcl() == null) {
                StackImpl byId = getById(update.getId());
                update.setAcl(byId.getAcl());
            }

            StackImpl updatedStack = collection.findOneAndReplace(eq("_id", update.getId()), update);
            if (updatedStack == null) {
                throw new NotFoundException(format("Stack with id '%s' was not found", update.getId()));
            }
            return updatedStack;
        } catch (MongoException mongoEx) {
            throw new ServerException(mongoEx.getMessage(), mongoEx);
        }
    }

    @Override
    public List<StackImpl> searchStacks(String user, @Nullable List<String> tags, int skipCount, int maxItems) throws ServerException {
        requireNonNull(user, "User id required");

        try {
            Bson query = elemMatch("acl", and(in("actions", "search"),
                                              or(eq("user", "*"),
                                                 eq("user", user))));
            if (tags != null && !tags.isEmpty()) {
                query = and(query, all("tags", tags));
            }

            return collection.find(query)
                             .skip(skipCount)
                             .limit(maxItems)
                             .into(new ArrayList<>());
        } catch (MongoException mongoEx) {
            throw new ServerException("Impossible to retrieve stacks. ", mongoEx);
        }
    }
}
