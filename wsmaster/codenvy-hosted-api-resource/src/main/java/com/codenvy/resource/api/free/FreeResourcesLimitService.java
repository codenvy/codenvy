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
package com.codenvy.resource.api.free;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.codenvy.resource.api.DtoConverter;
import com.codenvy.resource.model.FreeResourcesLimit;
import com.codenvy.resource.shared.dto.FreeResourcesLimitDto;

import org.eclipse.che.api.core.BadRequestException;
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Defines REST API for managing of free resources limits
 *
 * @author Sergii Leschenko
 */
@Api(value = "resource-free", description = "Free resources limit REST API")
@Path("/resource/free")
public class FreeResourcesLimitService extends Service {
    private final FreeResourcesLimitManager   freeResourcesLimitManager;
    private final FreeResourcesLimitValidator freeResourcesLimitValidator;

    @Inject
    public FreeResourcesLimitService(FreeResourcesLimitValidator freeResourcesLimitValidator,
                                     FreeResourcesLimitManager freeResourcesLimitManager) {
        this.freeResourcesLimitManager = freeResourcesLimitManager;
        this.freeResourcesLimitValidator = freeResourcesLimitValidator;
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Store free resources limit",
                  response = FreeResourcesLimitDto.class)
    @ApiResponses({@ApiResponse(code = 201, message = "The resources limit successfully stored"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response storeFreeResourcesLimit(@ApiParam(value = "Free resources limit")
                                            FreeResourcesLimitDto resourcesLimit) throws BadRequestException,
                                                                                         NotFoundException,
                                                                                         ServerException {
        freeResourcesLimitValidator.check(resourcesLimit);
        return Response.status(201)
                       .entity(DtoConverter.asDto(freeResourcesLimitManager.store(resourcesLimit)))
                       .build();
    }

    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get free resources limits",
                  response = FreeResourcesLimitDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The resources limits successfully fetched"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response getFreeResourcesLimits(@ApiParam(value = "Max items")
                                           @QueryParam("maxItems") @DefaultValue("30") int maxItems,
                                           @ApiParam(value = "Skip count")
                                           @QueryParam("skipCount") @DefaultValue("0") int skipCount) throws ServerException {

        final Page<? extends FreeResourcesLimit> limitsPage = freeResourcesLimitManager.getAll(maxItems, skipCount);

        return Response.ok()
                       .entity(limitsPage.getItems(DtoConverter::asDto))
                       .header("Link", createLinkHeader(limitsPage))
                       .build();
    }

    @GET
    @Path("/{accountId}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get free resources limit for account with given id",
                  response = FreeResourcesLimitDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The resources limit successfully fetched"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 404, message = "Resources limit for given account was not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public FreeResourcesLimitDto getFreeResourcesLimit(@ApiParam(value = "Account id")
                                                       @PathParam("accountId") String accountId) throws BadRequestException,
                                                                                                        NotFoundException,
                                                                                                        ServerException {
        return DtoConverter.asDto(freeResourcesLimitManager.get(accountId));
    }

    @DELETE
    @Path("/{accountId}")
    @ApiOperation(value = "Remove free resources limit for account with given id",
                  response = FreeResourcesLimitDto.class)
    @ApiResponses({@ApiResponse(code = 204, message = "The resources limit successfully removed"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void removeFreeResourcesLimit(@ApiParam(value = "Account id")
                                         @PathParam("accountId") String accountId) throws ServerException {
        freeResourcesLimitManager.remove(accountId);
    }
}
