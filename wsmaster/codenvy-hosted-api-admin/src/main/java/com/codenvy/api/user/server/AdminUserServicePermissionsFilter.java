/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.api.user.server;

import static org.eclipse.che.multiuser.permission.user.UserServicePermissionsFilter.MANAGE_USERS_ACTION;

import javax.ws.rs.Path;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Filter that covers calls to {@link AdminUserService} with authorization
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/admin/user{path:.*}")
public class AdminUserServicePermissionsFilter extends CheMethodInvokerFilter {
  @Override
  protected void filter(GenericResourceMethod grm, Object[] arguments) throws ApiException {
    final String methodName = grm.getMethod().getName();
    if ("getAll".equals(methodName) || "find".equals(methodName)) {
      EnvironmentContext.getCurrent()
          .getSubject()
          .checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION);
    } else {
      // unknown method
      throw new ForbiddenException("User is not authorized to perform this operation");
    }
  }
}
