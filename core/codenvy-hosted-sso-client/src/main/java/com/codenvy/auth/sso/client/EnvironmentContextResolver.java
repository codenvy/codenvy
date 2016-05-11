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
package com.codenvy.auth.sso.client;

import org.eclipse.che.commons.env.EnvironmentContext;

import javax.servlet.http.HttpServletRequest;

/**
 * SSOContextResolver get parameter of environment such as workspaceId and accountId from  EnvironmentContext.
 *
 * @author Sergii Kabashniuk
 */
public class EnvironmentContextResolver implements SSOContextResolver {
    @Override
    public RolesContext getRequestContext(HttpServletRequest request) {
        EnvironmentContext context = EnvironmentContext.getCurrent();
        return new RolesContext(context.getWorkspaceId(), null);
    }
}
