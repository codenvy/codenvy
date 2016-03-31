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
package com.codenvy.service.machine.token;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.codenvy.service.machine.token.shared.dto.MachineTokenDto;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.commons.env.EnvironmentContext;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Machine token service.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */

@Api(value = "/machine/token", description = "Machine token REST API")
@Path("/machine/token")
public class MachineTokenService {

    private final MachineTokenRegistry registry;

    @Inject
    public  MachineTokenService(MachineTokenRegistry machineTokenRegistry) {
        this.registry = machineTokenRegistry;
    }


    @GET
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = "get machine token for user")
    @ApiOperation(value = "Get machine token for user", notes = "Get machine token for user")
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 404, message = "Not found")})
    public MachineTokenDto getMachineToken() throws NotFoundException {
        final String userId  = EnvironmentContext.getCurrent().getUser().getId();
        return newDto(MachineTokenDto.class).withUserId(userId).withMachineToken(registry.getToken(userId));
    }
}
