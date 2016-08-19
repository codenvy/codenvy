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
package com.codenvy.api.workspace.server.jpa;

import com.codenvy.api.permission.server.jpa.AbstractPermissionsDao;
import com.codenvy.api.workspace.server.recipe.RecipeDomain;
import com.codenvy.api.workspace.server.recipe.RecipePermissionsImpl;


import org.eclipse.che.api.core.ServerException;

import java.io.IOException;
import java.util.List;

/**
 * @author Max Shaposhnik
 */
public class JpaRecipePermissionsDao extends AbstractPermissionsDao<RecipePermissionsImpl> {
    public JpaRecipePermissionsDao() throws IOException {
        super(new RecipeDomain(), RecipePermissionsImpl.class);
    }

    @Override
    public RecipePermissionsImpl get(String userId, String instanceId) throws ServerException {
        return null;
    }

    @Override
    public List<RecipePermissionsImpl> getByInstance(String instanceId) throws ServerException {
        return null;
    }

    @Override
    public List<RecipePermissionsImpl> getByUser(String userId) throws ServerException {
        return null;
    }
}
