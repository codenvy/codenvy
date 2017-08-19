/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.api.invite;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codenvy.auth.sso.server.EmailValidator;
import com.codenvy.shared.invite.dto.InviteDto;
import com.codenvy.shared.invite.model.Invite;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;

/**
 * Defines Invite REST API.
 *
 * @author Sergii Leschenko
 */
@Api(value = "/invite", description = "Invite REST API")
@Path("/invite")
public class InviteService extends Service {
  private final EmailValidator emailValidator;
  private final InviteManager inviteManager;

  @Inject
  public InviteService(EmailValidator emailValidator, InviteManager inviteManager) {
    this.emailValidator = emailValidator;
    this.inviteManager = inviteManager;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @ApiOperation(
    value = "Invite unregistered user by email " + "or update permissions for already invited user",
    notes = "Invited user will receive email notification only on invitation creation"
  )
  @ApiResponses({
    @ApiResponse(code = 204, message = "The invitation successfully created/updated"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 409, message = "User with specified email is already registered"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void invite(@ApiParam(value = "The invite to store", required = true) InviteDto inviteDto)
      throws BadRequestException, NotFoundException, ConflictException, ServerException {
    checkArgument(inviteDto != null, "Invite required");
    checkArgument(!isNullOrEmpty(inviteDto.getEmail()), "Email required");
    checkArgument(!isNullOrEmpty(inviteDto.getDomainId()), "Domain id required");
    checkArgument(!isNullOrEmpty(inviteDto.getInstanceId()), "Instance id required");
    checkArgument(!inviteDto.getActions().isEmpty(), "One or more actions required");
    emailValidator.validateUserMail(inviteDto.getEmail());

    inviteManager.store(inviteDto);
  }

  @GET
  @Path("/{domain}")
  @Produces(APPLICATION_JSON)
  @ApiOperation(
    value = "Get invites by instance",
    response = InviteDto.class,
    responseContainer = "list"
  )
  @ApiResponses({
    @ApiResponse(code = 200, message = "The invitations successfully fetched"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response getInvites(
      @ApiParam(value = "Domain id") @PathParam("domain") String domain,
      @ApiParam(value = "Instance id", required = true) @QueryParam("instance") String instance,
      @ApiParam("Skip count") @QueryParam("skipCount") @DefaultValue("0") long skipCount,
      @ApiParam("Max items") @QueryParam("maxItems") @DefaultValue("30") int maxItems)
      throws BadRequestException, ServerException {
    checkArgument(maxItems >= 0, "The number of items to return can't be negative.");
    checkArgument(skipCount >= 0, "The number of items to skip can't be negative.");
    checkArgument(!isNullOrEmpty(instance), "Instance id required");
    Page<? extends Invite> invitesPage =
        inviteManager.getInvites(domain, instance, skipCount, maxItems);
    return Response.ok()
        .entity(invitesPage.getItems().stream().map(DtoConverter::asDto).collect(toList()))
        .header("Link", createLinkHeader(invitesPage))
        .build();
  }

  @DELETE
  @Path("/{domain}")
  @ApiOperation(value = "Remove invite")
  @ApiResponses({
    @ApiResponse(code = 204, message = "The invitation successfully removed"),
    @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void remove(
      @ApiParam("Domain id") @PathParam("domain") String domain,
      @ApiParam(value = "Instance id", required = true) @QueryParam("instance") String instance,
      @ApiParam(value = "User email", required = true) @QueryParam("email") String email)
      throws BadRequestException, ServerException {
    checkArgument(!isNullOrEmpty(instance), "Instance id required");
    checkArgument(!isNullOrEmpty(email), "User email required");
    inviteManager.remove(domain, instance, email);
  }

  private void checkArgument(boolean expression, String message) throws BadRequestException {
    if (!expression) {
      throw new BadRequestException(message);
    }
  }
}
