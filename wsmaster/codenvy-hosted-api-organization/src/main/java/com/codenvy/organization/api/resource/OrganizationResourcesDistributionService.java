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
package com.codenvy.organization.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.codenvy.organization.api.DtoConverter;
import com.codenvy.organization.shared.dto.OrganizationDistributedResourcesDto;
import com.codenvy.organization.shared.model.OrganizationDistributedResources;
import com.codenvy.resource.api.free.ResourceValidator;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.shared.dto.ResourceDto;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * REST API for resources distribution between suborganizations
 *
 * @author Sergii Leschenko
 */
@Api(value = "/organization/resource", description = "REST API for resources distribution between suborganizations")
@Path("/organization/resource")
public class OrganizationResourcesDistributionService extends Service {
    private final OrganizationResourcesDistributor resourcesDistributor;
    private final ResourceValidator                resourceValidator;

    @Inject
    public OrganizationResourcesDistributionService(OrganizationResourcesDistributor resourcesDistributor,
                                                    ResourceValidator resourceValidator) {
        this.resourcesDistributor = resourcesDistributor;
        this.resourceValidator = resourceValidator;
    }

    @POST
    @Path("/{suborganizationId}")
    @Consumes(APPLICATION_JSON)
    @ApiOperation(value = "Distribute resources for suborganization.",
                  notes = "Distributed resources is unavailable for usage by parent organization." +
                          "Distributed resources should be reset to be available for usage by parent organization.")
    @ApiResponses({@ApiResponse(code = 204, message = "Resources successfully distributed for suborganization usage"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 404, message = "Specified organization was not found"),
                   @ApiResponse(code = 409, message = "Specified organization is root organization"),
                   @ApiResponse(code = 409, message = "Parent organization doesn't have enough resources to specified distribution"),
                   @ApiResponse(code = 409, message = "Suborganization doesn't have enough available resources " +
                                                      "(which are not in use and not distributed)"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void distribute(@ApiParam("Suborganization id")
                           @PathParam("suborganizationId") String suborganizationId,
                           @ApiParam("Resources to distribute") List<ResourceDto> resources) throws BadRequestException,
                                                                                                    ServerException,
                                                                                                    ConflictException,
                                                                                                    NotFoundException {
        Set<String> resourcesToSet = new HashSet<>();
        for (Resource resource : resources) {
            if (!resourcesToSet.add(resource.getType())) {
                throw new BadRequestException(format("Resources to distribute must contain only one resource with type '%s'.",
                                                     resource.getType()));
            }
            resourceValidator.check(resource);
        }

        resourcesDistributor.distribute(suborganizationId, resources);
    }

    @GET
    @Path("/{organizationId}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get distributed resources for suborganizations.",
                  response = OrganizationDistributedResourcesDto.class,
                  responseContainer = "list")
    @ApiResponses({@ApiResponse(code = 200, message = "Distributed resources successfully fetched"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response getDistributedResources(@ApiParam("Organization id")
                                            @PathParam("organizationId") String organizationId,
                                            @ApiParam(value = "Max items")
                                            @QueryParam("maxItems") @DefaultValue("30") int maxItems,
                                            @ApiParam(value = "Skip count")
                                            @QueryParam("skipCount") @DefaultValue("0") long skipCount) throws ServerException,
                                                                                                               BadRequestException {

        checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
        checkArgument(skipCount >= 0, "The number of items to skip can't be negative.");

        final Page<? extends OrganizationDistributedResources> distributedResourcesPage = resourcesDistributor.getByParent(organizationId,
                                                                                                                           maxItems,
                                                                                                                           skipCount);
        return Response.ok()
                       .entity(distributedResourcesPage.getItems(DtoConverter::asDto))
                       .header("Link", createLinkHeader(distributedResourcesPage))
                       .build();
    }

    @DELETE
    @Path("/{organizationId}")
    @ApiOperation(value = "Reset resources distribution.",
                  notes = "After distribution resetting suborganization won't be able to use parent resources.")
    @ApiResponses({@ApiResponse(code = 204, message = "Resources distribution successfully reset"),
                   @ApiResponse(code = 404, message = "Specified organization was not found"),
                   @ApiResponse(code = 409, message = "Suborganization doesn't have enough available resources " +
                                                      "(which are not in use and not distributed)"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void reset(@ApiParam(value = "Organization id")
                      @PathParam("organizationId") String organizationId) throws ServerException,
                                                                                 ConflictException,
                                                                                 NotFoundException {
        resourcesDistributor.reset(organizationId);
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression
     *         a boolean expression
     * @param errorMessage
     *         the exception message to use if the check fails
     * @throws BadRequestException
     *         if {@code expression} is false
     */
    private void checkArgument(boolean expression, String errorMessage) throws BadRequestException {
        if (!expression) {
            throw new BadRequestException(errorMessage);
        }
    }
}
