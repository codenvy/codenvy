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
package com.codenvy.ldap;

import javax.ws.rs.Path;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Disables password restoring operations when LDAP sync mode is on.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Filter
@Path("/password{path:(/.*)?}")
public class DisablePasswordOperationsFilter extends CheMethodInvokerFilter {

  @Override
  protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments)
      throws ApiException {
    throw new ForbiddenException("This action is unavailable in LDAP synchronization mode.");
  }
}
