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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * REST API for {@link LdapSynchronizer}.
 *
 * @author Yevhenii Voevodin
 */
@Path("/ldap/sync")
@Api("/ldap/sync")
public class LdapSynchronizerService {

    @Inject
    private LdapSynchronizer synchronizer;

    @POST
    @ApiOperation("Forces immediate users/profiles synchronization")
    @ApiResponses({@ApiResponse(code = 204, message = "Synchronization successfully started"),
                   @ApiResponse(code = 409, message = "Failed to perform synchronization because of " +
                                                      "another in-progress synchronization process")})
    public Response sync() throws ConflictException {
        try {
            synchronizer.syncAllAsynchronously();
        } catch (SyncException x) {
            throw new ConflictException(x.getLocalizedMessage());
        }
        return Response.noContent().build();
    }
}
