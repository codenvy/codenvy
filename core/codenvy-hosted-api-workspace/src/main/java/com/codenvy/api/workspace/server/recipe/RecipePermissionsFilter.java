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

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.recipe.RecipeService;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericMethodResource;

import javax.ws.rs.Path;

import static com.codenvy.api.workspace.server.recipe.RecipeDomain.DELETE;
import static com.codenvy.api.workspace.server.recipe.RecipeDomain.READ;
import static com.codenvy.api.workspace.server.recipe.RecipeDomain.UPDATE;

/**
 * Restricts access to methods of {@link RecipeService} by users' permissions
 *
 * <p>Filter should contain rules for protecting of all methods of {@link RecipeService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/recipe{path:(/.*)?}")
public class RecipePermissionsFilter extends CheMethodInvokerFilter {
    @Override
    public void filter(GenericMethodResource genericMethodResource, Object[] arguments) throws ForbiddenException, ServerException {
        final String methodName = genericMethodResource.getMethod().getName();

        final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
        String action;
        String recipeId;

        switch (methodName) {
            case "getRecipe":
            case "getRecipeScript":
                recipeId = ((String)arguments[0]);
                action = READ;
                break;

            case "updateRecipe":
                RecipeUpdate recipeUpdate = (RecipeUpdate)arguments[0];
                recipeId = recipeUpdate.getId();
                action = UPDATE;
                break;

            case "removeRecipe":
                recipeId = ((String)arguments[0]);
                action = DELETE;
                break;

            case "createRecipe":
            case "searchRecipes":
                //available for all
                return;
            default:
                throw new ForbiddenException("The user does not have permission to perform this operation");
        }

        if (!currentSubject.hasPermission(RecipeDomain.DOMAIN_ID, recipeId, action)) {
            throw new ForbiddenException("The user does not have permission to " + action + " recipe with id '" + recipeId + "'");
        }
    }
}
