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

import com.codenvy.api.permission.server.SystemDomain;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.List;

/**
 * @author Max Shaposhnik
 */
@Entity(name = "SystemPermissions")
@NamedQueries(
        {
                @NamedQuery(name = "SystemPermissions.getByUserId",
                            query = "SELECT recipe " +
                                    "FROM RecipePermissions recipe " +
                                    "WHERE recipe.userId = :userId ")
        }
)
public class SystemPermissionsImpl extends AbstractPermissions {

    public SystemPermissionsImpl() {
    }

    public SystemPermissionsImpl(String userId, List<String> actions) {
        super(userId, actions);
    }

    @Override
    public String getInstanceId() {
        return null;
    }

    @Override
    public String getDomainId() {
        return SystemDomain.DOMAIN_ID;
    }
}
