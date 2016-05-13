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

import com.codenvy.api.permission.shared.Permissions;
import com.codenvy.api.permission.shared.dto.PermissionsDto;

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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Defines Permissions REST API
 *
 * @author Sergii Leschenko
 */
@Path("/permissions")
public class PermissionsService extends Service {
    private final PermissionManager permissionManager;

    @Inject
    public PermissionsService(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    /**
     * Returns supported domains' identifiers
     */
    @GET
    @Produces(APPLICATION_JSON)
    public Set<String> getSupportedDomains() {
        return permissionManager.getDomains();
    }

    /**
     * @param domain
     *         domain id to retrieve supported actions
     * @return supported actions by given domain
     */
    @GET
    @Path("/{domain}")
    @Produces(APPLICATION_JSON)
    public Set<String> getSupportedActions(@PathParam("domain") String domain) throws NotFoundException {
        return permissionManager.getDomainsActions(domain);
    }

    /**
     * Stores permissions
     *
     * @param permissionsDto
     *         permissions to storing
     */
    @POST
    @Consumes(APPLICATION_JSON)
    public void storePermissions(PermissionsDto permissionsDto)
            throws ServerException, BadRequestException, ConflictException, NotFoundException {

        checkArgument(permissionsDto != null, "Permissions descriptor required");
        checkArgument(!isNullOrEmpty(permissionsDto.getUser()), "User required");
        checkArgument(!isNullOrEmpty(permissionsDto.getDomain()), "Domain required");
        checkArgument(!isNullOrEmpty(permissionsDto.getInstance()), "Instance required");
        checkArgument(!permissionsDto.getActions().isEmpty(), "One or more actions required");

        permissionManager.storePermission(new PermissionsImpl(permissionsDto));
    }

    /**
     * Returns list of permissions of current user which are related to specified domain and instance
     *
     * @param domain
     *         domain id to retrieve permitted actions
     * @param instance
     *         instance id to retrieve permitted actions
     * @return a list of actions which can be performed by current user to given domain and instance
     */
    @GET
    @Path("/{domain}/{instance}")
    @Produces(APPLICATION_JSON)
    public PermissionsDto getUsersPermissions(@PathParam("domain") String domain,
                                              @PathParam("instance") String instance) throws ServerException,
                                                                                             ConflictException,
                                                                                             NotFoundException {
        return toDto(permissionManager.get(EnvironmentContext.getCurrent().getSubject().getUserId(), domain, instance));
    }

    /**
     * Returns list of permissions which are related to specified domain and instance
     *
     * @param domain
     *         id of domain
     * @param instance
     *         id of instance
     */
    @GET
    @Path("/{domain}/{instance}/list")
    public List<PermissionsDto> getUsersPermissionsByInstance(@PathParam("domain") String domain,
                                                              @PathParam("instance") String instance) throws ServerException,
                                                                                                             NotFoundException {
        return permissionManager.getByInstance(domain, instance)
                                .stream()
                                .map(this::toDto)
                                .collect(Collectors.toList());
    }

    /**
     * Removes permissions of user related to the particular instance of specified domain
     *
     * @param user
     *         user id
     * @param domain
     *         domain id
     * @param instance
     *         instance id
     */
    @DELETE
    @Path("/{domain}/{instance}/{user}")
    public void removePermissions(@PathParam("domain") String domain,
                                  @PathParam("instance") String instance,
                                  @PathParam("user") String user) throws ConflictException, ServerException, NotFoundException {
        permissionManager.remove(user, domain, instance);
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
