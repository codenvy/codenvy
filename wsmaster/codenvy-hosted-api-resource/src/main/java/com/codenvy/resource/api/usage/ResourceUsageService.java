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
package com.codenvy.resource.api.usage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.codenvy.resource.api.DtoConverter;
import com.codenvy.resource.shared.dto.ResourceDto;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Defines Resource REST API.
 *
 * @author Sergii Leschenko
 */
@Api(value = "/resource", description = "Resource REST API")
@Path("/resource")
public class ResourceUsageService extends Service {
    private final ResourceUsageManager resourceUsageManager;

    @Inject
    public ResourceUsageService(ResourceUsageManager resourceUsageManager) {
        this.resourceUsageManager = resourceUsageManager;
    }

    @GET
    @Path("/{accountId}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get list of resources which are available for given account",
                  response = ResourceDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The total resources are successfully fetched"),
                   @ApiResponse(code = 404, message = "Account with specified id was not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<ResourceDto> getTotalResources(@ApiParam("Account id")
                                               @PathParam("accountId") String accountId) throws NotFoundException,
                                                                                                ServerException,
                                                                                                ConflictException {
        return resourceUsageManager.getTotalResources(accountId)
                                   .stream()
                                   .map(DtoConverter::asDto)
                                   .collect(Collectors.toList());
    }

    @GET
    @Path("/{accountId}/available")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get list of resources which are available for usage by given account",
                  response = ResourceDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The available resources are successfully fetched"),
                   @ApiResponse(code = 404, message = "Account with specified id was not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<ResourceDto> getAvailableResources(@PathParam("accountId")
                                                   String accountId) throws NotFoundException, ServerException {
        return resourceUsageManager.getAvailableResources(accountId)
                                   .stream()
                                   .map(DtoConverter::asDto)
                                   .collect(Collectors.toList());
    }

    @GET
    @Path("/{accountId}/used")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get list of resources which are used by given account",
                  response = ResourceDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The used resources are successfully fetched"),
                   @ApiResponse(code = 404, message = "Account with specified id was not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<ResourceDto> getUsedResources(@PathParam("accountId")
                                              String accountId) throws NotFoundException, ServerException {
        return resourceUsageManager.getUsedResources(accountId)
                                   .stream()
                                   .map(DtoConverter::asDto)
                                   .collect(Collectors.toList());
    }
}
