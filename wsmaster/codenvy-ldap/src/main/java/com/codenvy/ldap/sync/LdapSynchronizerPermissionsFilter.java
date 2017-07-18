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
package com.codenvy.ldap.sync;

import com.codenvy.api.permission.server.SystemDomain;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.ws.rs.Path;

/**
 * Rejects/allows access to the methods of {@link LdapSynchronizerService}.
 *
 * <p>All the service methods MUST be allowed only to those users who have
 * {@link SystemDomain#MANAGE_SYSTEM_ACTION} permission.
 *
 * @author Yevhenii Voevodin
 */
@Filter
@Path("/ldap/sync{path:.*}")
public class LdapSynchronizerPermissionsFilter extends CheMethodInvokerFilter {

    @Override
    protected void filter(GenericResourceMethod resource, Object[] args) throws ApiException {
        EnvironmentContext.getCurrent()
                          .getSubject()
                          .checkPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
    }
}
