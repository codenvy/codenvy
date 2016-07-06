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
package com.codenvy.api.user.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.codenvy.api.user.server.dao.AdminUserDao;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.user.server.DtoConverter;
import org.eclipse.che.api.user.server.UserLinksInjector;
import org.eclipse.che.api.user.server.model.impl.UserImpl;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Provides REST API for admin user management.
 *
 * @author Anatoliy Bazko
 */
@Api(value = "/admin/user", description = "Admin user manager")
@Path("/admin/user")
public class AdminUserService extends Service {

    private final AdminUserDao      adminUserDao;
    private final UserLinksInjector linksInjector;

    @Inject
    public AdminUserService(AdminUserDao adminUserDao, UserLinksInjector linksInjector) {
        this.adminUserDao = adminUserDao;
        this.linksInjector = linksInjector;
    }

    /**
     * Get all users.
     *
     * @param maxItems
     *         the maximum number of users to return
     * @param skipCount
     *         the number of users to skip
     * @return list of users
     * @throws BadRequestException
     *         when {@code maxItems} or {@code skipCount} are incorrect
     * @throws ServerException
     *         when some error occurred while retrieving users
     */
    @GET
    @GenerateLink(rel = "get all users")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get all users", notes = "Get all users in the system")
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 400, message = "Bad Request"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public Response getAll(@ApiParam(value = "Max items") @QueryParam("maxItems") @DefaultValue("30") int maxItems,
                           @ApiParam(value = "Skip count") @QueryParam("skipCount") @DefaultValue("0") int skipCount)
            throws ServerException, BadRequestException {
        try {
            final Page<UserImpl> usersPage = adminUserDao.getAll(maxItems, skipCount);
            return Response.ok()
                           .entity(usersPage.getItems(user -> linksInjector.injectLinks(DtoConverter.asDto(user), getServiceContext())))
                           .header("Link", createLinkHeader(usersPage))
                           .build();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
