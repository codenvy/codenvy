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
package com.codenvy.api.permission.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.codenvy.api.permission.shared.Permissions;
import com.codenvy.api.permission.shared.PermissionsDomain;
import com.codenvy.api.permission.shared.dto.DomainDto;
import com.codenvy.api.permission.shared.dto.PermissionsDto;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Defines Permissions REST API
 *
 * @author Sergii Leschenko
 */
@Api(value = "/permissions", description = "Permissions REST API")
@Path("/permissions")
public class PermissionsService extends Service {
    private final PermissionManager permissionManager;

    @Inject
    public PermissionsService(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get all supported domains or only requested if domain parameter specified",
                  response = DomainDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The domains successfully fetched"),
                   @ApiResponse(code = 404, message = "Requested domain is not supported"),
                   @ApiResponse(code = 500, message = "Internal server error occurred during domains fetching")})
    public List<DomainDto> getSupportedDomains(@ApiParam("Id of requested domain")
                                               @QueryParam("domain") String domainId) throws NotFoundException {
        if (isNullOrEmpty(domainId)) {
            return permissionManager.getDomains()
                                    .stream()
                                    .map(this::asDto)
                                    .collect(Collectors.toList());
        } else {
            return singletonList(asDto(permissionManager.getDomain(domainId)));
        }
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @ApiOperation("Store given permissions")
    @ApiResponses({@ApiResponse(code = 200, message = "The permissions successfully stored"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 404, message = "Domain of permissions is not supported"),
                   @ApiResponse(code = 409, message = "New permissions removes last 'setPermissions' of given instance"),
                   @ApiResponse(code = 409, message = "Given domain requires non nullable value for instance but it is null"),
                   @ApiResponse(code = 500, message = "Internal server error occurred during permissions storing")})
    public void storePermissions(@ApiParam(value = "The permissions to store", required = true)
                                 PermissionsDto permissionsDto) throws ServerException,
                                                                       BadRequestException,
                                                                       ConflictException,
                                                                       NotFoundException {
        checkArgument(permissionsDto != null, "Permissions descriptor required");
        checkArgument(!isNullOrEmpty(permissionsDto.getUser()), "User required");
        checkArgument(!isNullOrEmpty(permissionsDto.getDomain()), "Domain required");
        checkArgument(!permissionsDto.getActions().isEmpty(), "One or more actions required");

        permissionManager.storePermission(new PermissionsImpl(permissionsDto));
    }

    @GET
    @Path("/{domain}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get permissions of current user which are related to specified domain and instance",
                  response = PermissionsDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The permissions successfully fetched"),
                   @ApiResponse(code = 404, message = "Specified domain is unsupported"),
                   @ApiResponse(code = 404, message = "Permissions for current user with specified domain and instance was not found"),
                   @ApiResponse(code = 409, message = "Given domain requires non nullable value for instance but it is null"),
                   @ApiResponse(code = 500, message = "Internal server error occurred during permissions fetching")})
    public PermissionsDto getCurrentUsersPermissions(@ApiParam(value = "Domain id to retrieve user's permissions")
                                                     @PathParam("domain") String domain,
                                                     @ApiParam(value = "Instance id to retrieve user's permissions")
                                                     @QueryParam("instance") String instance) throws ServerException,
                                                                                                     NotFoundException,
                                                                                                     ConflictException {
        return toDto(permissionManager.get(EnvironmentContext.getCurrent().getSubject().getUserId(), domain, instance));
    }

    @GET
    @Path("/{domain}/all")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get permissions which are related to specified domain and instance",
                  response = PermissionsDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The permissions successfully fetched"),
                   @ApiResponse(code = 404, message = "Specified domain is unsupported"),
                   @ApiResponse(code = 409, message = "Given domain requires non nullable value for instance but it is null"),
                   @ApiResponse(code = 500, message = "Internal server error occurred during permissions fetching")})
    public List<PermissionsDto> getUsersPermissions(@ApiParam(value = "Domain id to retrieve users' permissions")
                                                    @PathParam("domain") String domain,
                                                    @ApiParam(value = "Instance id to retrieve users' permissions")
                                                    @QueryParam("instance") String instance) throws ServerException,
                                                                                                    NotFoundException,
                                                                                                    ConflictException {
        return permissionManager.getByInstance(domain, instance)
                                .stream()
                                .map(this::toDto)
                                .collect(Collectors.toList());
    }

    @DELETE
    @Path("/{domain}")
    @ApiOperation("Removes user's permissions related to the particular instance of specified domain")
    @ApiResponses({@ApiResponse(code = 204, message = "The permissions successfully removed"),
                   @ApiResponse(code = 404, message = "Specified domain is unsupported"),
                   @ApiResponse(code = 409, message = "User has last 'setPermissions' of given instance"),
                   @ApiResponse(code = 409, message = "Given domain requires non nullable value for instance but it is null"),
                   @ApiResponse(code = 500, message = "Internal server error occurred during permissions removing")})
    public void removePermissions(@ApiParam("Domain id to remove user's permissions")
                                  @PathParam("domain") String domain,
                                  @ApiParam(value = "Instance id to remove user's permissions")
                                  @QueryParam("instance") String instance,
                                  @ApiParam(value = "User id", required = true)
                                  @QueryParam("user") @Required String user) throws ConflictException, ServerException, NotFoundException {
        permissionManager.remove(user, domain, instance);
    }

    private DomainDto asDto(PermissionsDomain domain) {
        return DtoFactory.newDto(DomainDto.class)
                         .withId(domain.getId())
                         .withAllowedActions(domain.getAllowedActions());
    }

    private void checkArgument(boolean expression, String message) throws BadRequestException {
        if (!expression) {
            throw new BadRequestException(message);
        }
    }

    private PermissionsDto toDto(Permissions permissions) {
        return DtoFactory.newDto(PermissionsDto.class)
                         .withUser(permissions.getUser())
                         .withDomain(permissions.getDomain())
                         .withInstance(permissions.getInstance())
                         .withActions(permissions.getActions());
    }
}
