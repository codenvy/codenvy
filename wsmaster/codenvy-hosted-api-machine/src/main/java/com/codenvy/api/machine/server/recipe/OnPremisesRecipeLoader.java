/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.api.machine.server.recipe;

import com.codenvy.api.machine.server.jpa.JpaRecipePermissionsDao;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;

/**
 * Loads predefined recipes, with public permissions.
 *
 * @author Anton Korneta.
 */
@Singleton
public class OnPremisesRecipeLoader {

    private static final Logger LOG = LoggerFactory.getLogger(OnPremisesRecipeLoader.class);

    private static final Gson GSON = new GsonBuilder().create();

    private final Set<String>             recipesPaths;
    private final RecipeDao               recipeDao;
    private final JpaRecipePermissionsDao permissionsDao;

    @Inject
    public OnPremisesRecipeLoader(@Nullable @Named("predefined.recipe.path") Set<String> recipesPaths,
                                  JpaRecipePermissionsDao permissionsDao,
                                  RecipeDao recipeDao) {
        this.permissionsDao = permissionsDao;
        this.recipesPaths = firstNonNull(recipesPaths, Collections.<String>emptySet());
        this.recipeDao = recipeDao;
    }

    @PostConstruct
    public void start() {
        for (String recipesPath : recipesPaths) {
            if (recipesPath != null && !recipesPath.isEmpty()) {
                try {
                    for (RecipeImpl recipe : loadRecipes(recipesPath)) {

                        doCreate(recipe);
                        permissionsDao.store(new RecipePermissionsImpl("*",
                                                                       recipe.getId(),
                                                                       singletonList("search")));
                    }
                } catch (ConflictException | ServerException ex) {
                    LOG.error("Failed to load recipe from ", recipesPath, ex.getMessage());
                }
            }
        }
    }

    private void doCreate(RecipeImpl recipe) throws ServerException, ConflictException {
        try {
            recipeDao.update(recipe);
        } catch (NotFoundException e) {
            recipeDao.create(recipe);
        }
    }

    /**
     * Loads recipes by specified path.
     *
     * @param recipesPath
     *         path to recipe file
     * @return list of predefined recipes
     * @throws ServerException
     *         when problems occurs with getting or parsing recipe file
     */
    private List<RecipeImpl> loadRecipes(String recipesPath) throws ServerException {
        try (InputStream is = getResource(recipesPath)) {
            return firstNonNull(GSON.fromJson(new InputStreamReader(is), new TypeToken<List<RecipeImpl>>() {}.getType()), emptyList());
        } catch (IOException | JsonIOException | JsonSyntaxException e) {
            throw new ServerException("Failed to get recipes from specified path " + recipesPath, e);
        }
    }

    /**
     * Searches for resource by given path.
     *
     * @param resource
     *         path to resource
     * @return resource InputStream
     * @throws IOException
     *         when problem occurs during resource getting
     */
    private InputStream getResource(String resource) throws IOException {
        File resourceFile = new File(resource);
        if (resourceFile.exists() && !resourceFile.isFile()) {
            throw new IOException(format("%s is not a file. ", resourceFile.getAbsolutePath()));
        }
        InputStream is = resourceFile.exists() ? new FileInputStream(resourceFile)
                                               : Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (is == null) {
            throw new IOException(format("Not found resource: %s", resource));
        }
        return is;
    }
}
