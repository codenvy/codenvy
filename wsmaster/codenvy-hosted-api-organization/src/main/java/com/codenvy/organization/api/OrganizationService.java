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
package com.codenvy.organization.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.codenvy.organization.shared.dto.OrganizationDto;
import com.codenvy.organization.shared.model.Organization;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
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
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Defines Organization REST API.
 *
 * @author Sergii Leschenko
 */
@Api(value = "/organization", description = "Organization REST API")
@Path("/organization")
public class OrganizationService extends Service {
    private final OrganizationManager       organizationManager;
    private final OrganizationLinksInjector linksInjector;

    @Inject
    public OrganizationService(OrganizationManager organizationManager, OrganizationLinksInjector linksInjector) {
        this.organizationManager = organizationManager;
        this.linksInjector = linksInjector;
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Create new organization",
                  response = OrganizationDto.class)
    @ApiResponses({@ApiResponse(code = 201, message = "The organization successfully created"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 409, message = "Conflict error occurred during the organization creation" +
                                                      "(e.g. The organization with such name already exists)"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response create(@ApiParam(value = "Organization to create", required = true)
                           OrganizationDto organization) throws BadRequestException, ConflictException, ServerException {
        requiredNotNull(organization, "Organization");
        return Response.status(201)
                       .entity(linksInjector.injectLinks(toDto(organizationManager.create(organization)), getServiceContext()))
                       .build();
    }

    @POST
    @Path("/{id}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Update organization",
                  response = OrganizationDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The organization successfully updated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 404, message = "The organization with given id was not found"),
                   @ApiResponse(code = 409, message = "Conflict error occurred during the organization creation" +
                                                      "(e.g. The organization with such name already exists)"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public OrganizationDto update(@ApiParam("Organization id")
                                  @PathParam("id")
                                  String organizationId,
                                  @ApiParam(value = "Organization to update", required = true)
                                  OrganizationDto organization) throws BadRequestException,
                                                                       ConflictException,
                                                                       NotFoundException,
                                                                       ServerException {
        requiredNotNull(organization, "Organization");
        return linksInjector.injectLinks(toDto(organizationManager.update(organizationId, organization)),
                                         getServiceContext());
    }

    @DELETE
    @Path("/{id}")
    @ApiOperation("Remove organization with given id")
    @ApiResponses({@ApiResponse(code = 204, message = "The organization successfully removed"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void remove(@ApiParam("Organization id")
                       @PathParam("id") String organization) throws ServerException {
        organizationManager.remove(organization);
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{organizationId}")
    @ApiOperation(value = "Get organization by id",
                  response = OrganizationDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The organization successfully fetched"),
                   @ApiResponse(code = 404, message = "The organization with given id was not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public OrganizationDto getById(@ApiParam("Organization id")
                                   @PathParam("organizationId") String organizationId) throws NotFoundException,
                                                                                              ServerException {
        return linksInjector.injectLinks(toDto(organizationManager.getById(organizationId)), getServiceContext());
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/find")
    @ApiOperation(value = "Find organization by name",
                  response = OrganizationDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The organization successfully fetched"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 404, message = "The organization with given name was not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public OrganizationDto find(@ApiParam(value = "Organization name", required = true)
                                @QueryParam("name") String organizationName) throws NotFoundException,
                                                                                    ServerException,
                                                                                    BadRequestException {
        requiredNotNull(organizationName, "Missed organization's name");
        return linksInjector.injectLinks(toDto(organizationManager.getByName(organizationName)), getServiceContext());
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{parent}/organizations")
    @ApiOperation(value = "Get child organizations",
                  response = OrganizationDto.class,
                  responseContainer = "list")
    @ApiResponses({@ApiResponse(code = 200, message = "The child organizations successfully fetched"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<OrganizationDto> getByParent(@ApiParam("Parent organization id")
                                                 @PathParam("parent") String parent) throws ServerException {
        return organizationManager.getByParent(parent)
                                  .stream()
                                  .map(child -> linksInjector.injectLinks(toDto(child), getServiceContext()))
                                  .collect(Collectors.toList());
    }

    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get memberships of current user",
                  response = OrganizationDto.class,
                  responseContainer = "list")
    @ApiResponses({@ApiResponse(code = 200, message = "The organizations successfully fetched"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<OrganizationDto> getOrganizations() throws ServerException {
        return organizationManager.getByMember(EnvironmentContext.getCurrent().getSubject().getUserId())
                                  .stream()
                                  .map(child -> linksInjector.injectLinks(toDto(child), getServiceContext()))
                                  .collect(Collectors.toList());
    }

    private OrganizationDto toDto(Organization organization) {
        return DtoFactory.newDto(OrganizationDto.class)
                         .withId(organization.getId())
                         .withName(organization.getName())
                         .withParent(organization.getParent());
    }

    /**
     * Checks object reference is not {@code null}
     *
     * @param object
     *         object reference to check
     * @param subject
     *         used as subject of exception message "{subject} required"
     * @throws BadRequestException
     *         when object reference is {@code null}
     */
    private void requiredNotNull(Object object, String subject) throws BadRequestException {
        if (object == null) {
            throw new BadRequestException(subject + " required");
        }
    }
}
