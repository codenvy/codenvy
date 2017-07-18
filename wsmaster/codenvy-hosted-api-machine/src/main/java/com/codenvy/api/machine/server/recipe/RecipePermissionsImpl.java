/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.api.machine.server.recipe;


import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.shared.model.Permissions;

import org.eclipse.che.api.machine.server.recipe.RecipeImpl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.List;

/**
 * Recipe permissions data object.
 *
 * @author Max Shaposhnik
 */
@Entity(name = "RecipePermissions")
@NamedQueries(
        {
                @NamedQuery(name = "RecipePermissions.getByRecipeId",
                            query = "SELECT recipePermission " +
                                    "FROM RecipePermissions recipePermission " +
                                    "WHERE recipePermission.recipeId = :recipeId "),
                @NamedQuery(name = "RecipePermissions.getCountByRecipeId",
                            query = "SELECT COUNT(recipePermission) " +
                                    "FROM RecipePermissions recipePermission " +
                                    "WHERE recipePermission.recipeId = :recipeId "),
                @NamedQuery(name = "RecipePermissions.getByUserId",
                            query = "SELECT recipePermission " +
                                    "FROM RecipePermissions recipePermission " +
                                    "WHERE recipePermission.userId = :userId "),
                @NamedQuery(name = "RecipePermissions.getByUserAndRecipeId",
                            query = "SELECT recipePermission " +
                                    "FROM RecipePermissions recipePermission " +
                                    "WHERE recipePermission.recipeId = :recipeId " +
                                    "AND recipePermission.userId = :userId"),
                @NamedQuery(name = "RecipePermissions.getByRecipeIdPublic",
                            query = "SELECT recipePermission " +
                                    "FROM RecipePermissions recipePermission " +
                                    "WHERE recipePermission.recipeId = :recipeId " +
                                    "AND recipePermission.userId IS NULL ")
        }
)
@Table(name = "recipepermissions")
public class RecipePermissionsImpl extends AbstractPermissions {

    @Column(name = "recipeid")
    private String recipeId;

    @ManyToOne
    @JoinColumn(name = "recipeid", insertable = false, updatable = false)
    private RecipeImpl recipe;

    public RecipePermissionsImpl() {}

    public RecipePermissionsImpl(Permissions permissions) {
        this(permissions.getUserId(), permissions.getInstanceId(), permissions.getActions());
    }

    public RecipePermissionsImpl(String userId, String instanceId, List<String> allowedActions) {
        super(userId, allowedActions);
        this.recipeId = instanceId;
    }

    @Override
    public String getInstanceId() {
        return recipeId;
    }

    @Override
    public String getDomainId() {
        return RecipeDomain.DOMAIN_ID;
    }

    @Override
    public String toString() {
        return "RecipePermissionsImpl{" +
               "userId='" + getUserId() + '\'' +
               ", recipeId='" + recipeId + '\'' +
               ", actions=" + actions +
               '}';
    }
}
