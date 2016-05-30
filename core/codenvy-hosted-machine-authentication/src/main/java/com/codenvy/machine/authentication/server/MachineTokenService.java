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
package com.codenvy.machine.authentication.server;

import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static com.codenvy.api.workspace.server.WorkspaceDomain.DOMAIN_ID;
import static com.codenvy.api.workspace.server.WorkspaceDomain.USE;

/**
 * Machine security token service.
 * Allows user to retrieve token to access to the particular workspace, and,
 * in the reverse case, allows get the user by his token.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Path("/machine/token")
public class MachineTokenService {

    private final MachineTokenRegistry   registry;
    private final HttpJsonRequestFactory requestFactory;
    private final String                 apiEndpoint;

    @Inject
    public MachineTokenService(MachineTokenRegistry machineTokenRegistry,
                               HttpJsonRequestFactory requestFactory,
                               @Named("api.endpoint") String apiEndpoint) {
        this.registry = machineTokenRegistry;
        this.requestFactory = requestFactory;
        this.apiEndpoint = apiEndpoint;
    }

    /**
     * Gets the access token for current user for particular workspace.
     * If workspace is started by this user, token was generated on startup time, this method will just return it.
     * If workspace is started by other user, but current user has permissions to use it, token will be generated on demand.
     *
     * @param wsId
     *        id of workspace to generate token for.
     * @return entity of machine token
     * @throws NotFoundException
     *         if no workspace exists with given id
     * @throws ForbiddenException
     *         if no user does not have permissions to use this workspace
     */
    @GET
    @Path("/{wsId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public MachineTokenDto getMachineToken(@PathParam("wsId") String wsId) throws NotFoundException, ForbiddenException {
        final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
        final String userId = currentSubject.getUserId();
        String token = registry.getToken(userId, wsId);
        if (token == null) {
           currentSubject.checkPermission(DOMAIN_ID, wsId, USE);
           token = registry.generateToken(userId, wsId);
        }
        return newDto(MachineTokenDto.class).withUserId(userId)
                                            .withWorkspaceId(wsId)
                                            .withMachineToken(token);
    }

    /**
     * Finds a user by his machine token.
     *
     * @param token
     *        token to find user by
     * @return user entity
     * @throws ApiException
     *         when token is not found, or there is problem retrieving user via api.
     * @throws IOException
     */
    @GET
    @Path("/user/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public UserDescriptor getUser(@PathParam("token") String token) throws ApiException, IOException {
        final String userId = registry.getUserId(token);
        return requestFactory.fromUrl(apiEndpoint + "/user/" + userId)
                             .useGetMethod()
                             .request()
                             .asDto(UserDescriptor.class);
    }
}
