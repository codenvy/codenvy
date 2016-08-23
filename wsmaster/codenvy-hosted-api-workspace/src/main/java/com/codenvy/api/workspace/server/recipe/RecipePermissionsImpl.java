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
package com.codenvy.api.workspace.server.recipe;


import com.codenvy.api.permission.server.model.impl.AbstractPermissions;

import org.eclipse.che.api.machine.server.recipe.RecipeImpl;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;
import java.util.Objects;

/**
 *
 *
 * @author Max Shaposhnik
 */
@Entity(name = "RecipePermissions")
@NamedQueries(
        {
                @NamedQuery(name = "RecipePermissions.getByRecipeId",
                            query = "SELECT recipe " +
                                    "FROM RecipePermissions recipe " +
                                    "WHERE recipe.recipeId = :recipeId "),
                @NamedQuery(name = "RecipePermissions.getByUserId",
                            query = "SELECT recipe " +
                                    "FROM RecipePermissions recipe " +
                                    "WHERE recipe.userId = :userId "),
                @NamedQuery(name = "RecipePermissions.getByUserAndRecipeId",
                            query = "SELECT recipe " +
                                    "FROM RecipePermissions recipe " +
                                    "WHERE recipe.recipeId = :recipeId " +
                                    "AND recipe.userId = :userId")
        }
)

@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "recipeId"}))
public class RecipePermissionsImpl extends AbstractPermissions {

    private String recipeId;

    @ManyToOne
    @JoinColumn(name = "recipeId", insertable = false, updatable = false)
    private RecipeImpl recipe;

    public RecipePermissionsImpl() {

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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RecipePermissionsImpl)) return false;
        final RecipePermissionsImpl other = (RecipePermissionsImpl)obj;
        return Objects.equals(userId, other.userId) &&
               Objects.equals(recipeId, other.recipeId) &&
               actions.equals(other.actions);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(userId);
        hash = 31 * hash + Objects.hashCode(recipeId);
        hash = 31 * hash + actions.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "RecipePermissionsImpl{" +
               "userId='" + userId + '\'' +
               ", recipeId='" + recipeId + '\'' +
               ", actions=" + actions +
               '}';
    }
}
