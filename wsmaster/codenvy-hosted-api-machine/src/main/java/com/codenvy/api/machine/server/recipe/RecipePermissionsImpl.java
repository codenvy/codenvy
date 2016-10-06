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
@Table(indexes = {@Index(columnList = "userId, recipeId", unique = true),
                  @Index(columnList = "recipeId")})
public class RecipePermissionsImpl extends AbstractPermissions {

    @Column
    private String recipeId;

    @ManyToOne
    @JoinColumn(name = "recipeId", insertable = false, updatable = false)
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
