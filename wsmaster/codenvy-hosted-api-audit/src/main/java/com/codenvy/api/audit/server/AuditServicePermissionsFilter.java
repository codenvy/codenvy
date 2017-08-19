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
package com.codenvy.api.audit.server;

import static com.codenvy.api.permission.server.SystemDomain.MANAGE_SYSTEM_ACTION;

import com.codenvy.api.permission.server.SystemDomain;
import javax.ws.rs.Path;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Filter that covers calls to {@link AuditService} with authorization
 *
 * @author Igor Vinokur
 */
@Filter
@Path("/audit")
public class AuditServicePermissionsFilter extends CheMethodInvokerFilter {
  @Override
  protected void filter(GenericResourceMethod GenericResourceMethod, Object[] arguments)
      throws ApiException {
    String methodName = GenericResourceMethod.getMethod().getName();
    if ("downloadReport".equals(methodName)) {
      EnvironmentContext.getCurrent()
          .getSubject()
          .checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION);
    } else {
      //unknown method
      throw new ForbiddenException("User is not authorized to perform this operation");
    }
  }
}
