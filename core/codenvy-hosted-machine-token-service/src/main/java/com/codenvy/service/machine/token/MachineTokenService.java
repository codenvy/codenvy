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

import com.codenvy.service.machine.token.shared.dto.MachineTokenDto;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.commons.env.EnvironmentContext;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Machine security token service.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Path("/machine/token")
public class MachineTokenService {

    private final MachineTokenRegistry registry;

    @Inject
    public MachineTokenService(MachineTokenRegistry machineTokenRegistry) {
        this.registry = machineTokenRegistry;
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public MachineTokenDto getMachineToken(@PathParam("id") String id) throws NotFoundException {
        final String userId = EnvironmentContext.getCurrent().getUser().getId();
        return newDto(MachineTokenDto.class).withUserId(userId)
                                            .withWorkspaceId(id)
                                            .withMachineToken(registry.getToken(userId, id));
    }
}
