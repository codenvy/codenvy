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
