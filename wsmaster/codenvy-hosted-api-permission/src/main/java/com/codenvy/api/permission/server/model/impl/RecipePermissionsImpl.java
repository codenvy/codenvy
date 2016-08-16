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
package com.codenvy.api.permission.server.model.impl;


import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.user.server.model.impl.UserImpl;

import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.List;

/**
 * @author Max Shaposhnik
 *
 */
public class RecipePermissionsImpl extends PermissionsImpl {

    public static final String DOMAIN = "recipe";

    @OneToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private UserImpl user;

    @OneToOne
    @JoinColumn(name = "instanceId", insertable = false, updatable = false)
    private Recipe recipe;

    public RecipePermissionsImpl() {

    }

    public RecipePermissionsImpl(RecipePermissionsImpl recipePermissions) {
        super(recipePermissions.getUserId(), DOMAIN, recipePermissions.getInstanceId(), recipePermissions.getActions());
    }

    public RecipePermissionsImpl(String userId, String instanceId, List<String> actions) {
        super(userId, DOMAIN, instanceId, actions);
    }
}
