/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.selenium.core.client;

import com.codenvy.api.permission.shared.dto.PermissionsDto;
import com.codenvy.organization.shared.dto.OrganizationDto;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * This util is handling the requests to Organization API.
 */
@Singleton
public class OnpremTestOrganizationServiceClient {
    private static final Logger LOG = LoggerFactory.getLogger(OnpremTestOrganizationServiceClient.class);

    private final String                 apiEndpoint;
    private final HttpJsonRequestFactory requestFactory;
    private final TestUser adminTestUser;

    @Inject
    public OnpremTestOrganizationServiceClient(TestApiEndpointUrlProvider apiEndpointUrlProvider,
                                               HttpJsonRequestFactory requestFactory,
                                               AdminTestUser adminTestUser) {
        this.apiEndpoint = apiEndpointUrlProvider.get().toString();
        this.requestFactory = requestFactory;
        this.adminTestUser = adminTestUser;
    }

    public List<OrganizationDto> getOrganizationsAsAdmin() throws Exception {
        return getOrganizationsAsAdmin(null);
    }

    public List<OrganizationDto> getOrganizationsAsAdmin(@Nullable String parent) throws Exception {
        List<OrganizationDto> organizations = requestFactory.fromUrl(getApiUrl())
                                                            .setAuthorizationHeader(adminTestUser.getAuthToken())
                                                            .request()
                                                            .asList(OrganizationDto.class);

        if (parent == null) {
            organizations.removeIf(o -> o.getParent() != null);
        }

        return organizations;
    }

    public List<OrganizationDto> getOrganizations(@Nullable String parent, String authToken) throws Exception {
        List<OrganizationDto> organizations = requestFactory.fromUrl(getApiUrl())
                                                            .setAuthorizationHeader(authToken)
                                                            .request()
                                                            .asList(OrganizationDto.class);

        if (parent == null) {
            organizations.removeIf(o -> o.getParent() != null);
        }

        return organizations;
    }

    private String getApiUrl() {return apiEndpoint + "organization/";}

    public OrganizationDto createOrganizationAsAdmin(String name, String parentId) throws Exception {
        OrganizationDto data = newDto(OrganizationDto.class)
                .withName(name)
                .withParent(parentId);

        OrganizationDto organizationDto = requestFactory.fromUrl(getApiUrl())
                                                        .setAuthorizationHeader(adminTestUser.getAuthToken())
                                                        .setBody(data)
                                                        .usePostMethod().request()
                                                        .asDto(OrganizationDto.class);

        LOG.debug("Organization with name='{}', id='{}' and parent's id='{}' created", name, organizationDto.getId(), parentId);

        return organizationDto;
    }

    public OrganizationDto createOrganizationAsAdmin(String name) throws Exception {
        return createOrganizationAsAdmin(name, null);
    }

    public OrganizationDto createOrganization(String name, String parentId, String authToken) throws Exception {
        OrganizationDto data = newDto(OrganizationDto.class)
                .withName(name)
                .withParent(parentId);

        OrganizationDto organizationDto = requestFactory.fromUrl(getApiUrl())
                                                        .setAuthorizationHeader(authToken)
                                                        .setBody(data)
                                                        .usePostMethod().request()
                                                        .asDto(OrganizationDto.class);

        LOG.debug("Organization with name='{}', id='{}' and parent's id='{}' created", name, organizationDto.getId(), parentId);

        return organizationDto;
    }

    public void deleteOrganizationByIdAsAdmin(String id) throws Exception {
        String apiUrl = format("%s%s", getApiUrl(), id);

        try {
            requestFactory.fromUrl(apiUrl)
                          .setAuthorizationHeader(adminTestUser.getAuthToken())
                          .useDeleteMethod()
                          .request();
        } catch (NotFoundException e) {
            // ignore if there is no organization of certain id
        }

        LOG.debug("Organization with id='{}' removed", id);
    }

    public void deleteOrganizationById(String id, String authToken) throws Exception {
        String apiUrl = format("%s%s", getApiUrl(), id);

        try {
            requestFactory.fromUrl(apiUrl)
                          .setAuthorizationHeader(authToken)
                          .useDeleteMethod()
                          .request();
        } catch (NotFoundException e) {
            // ignore if there is no organization of certain id
        }

        LOG.debug("Organization with id='{}' removed", id);
    }

    public void deleteOrganizationByNameAsAdmin(String name) throws Exception {
        OrganizationDto organization = getOrganizationByNameAsAdmin(name);

        if (organization != null) {
            deleteOrganizationByIdAsAdmin(organization.getId());
        }
    }

    public void deleteAllOrganizationsOfUser(TestUser testUser) throws Exception {
        deleteAllOrganizationsOfUser(testUser.getName(), testUser.getAuthToken());
    }

    public void deleteAllOrganizationsOfUser(String parentId, String authToken) throws Exception {
        getOrganizations(parentId, authToken).stream()
                                             .filter(organization -> organization.getParent() != null)
                                             .forEach(organization -> {
                                                 try {
                                                     deleteOrganizationById(organization.getId(), authToken);
                                                 } catch (Exception e) {
                                                     throw new RuntimeException(e.getMessage(), e);
                                                 }
                                             });
    }

    public OrganizationDto getOrganizationByNameAsAdmin(String organizationName) throws Exception {
        String apiUrl = format("%sfind?name=%s", getApiUrl(), organizationName);
        return requestFactory.fromUrl(apiUrl)
                             .setAuthorizationHeader(adminTestUser.getAuthToken())
                             .request()
                             .asDto(OrganizationDto.class);
    }

    public OrganizationDto getOrganizationByName(String organizationName, String authToken) throws Exception {
        String apiUrl = format("%sfind?name=%s", getApiUrl(), organizationName);
        return requestFactory.fromUrl(apiUrl)
                             .setAuthorizationHeader(authToken)
                             .request()
                             .asDto(OrganizationDto.class);
    }

    public void addOrganizationMemberAsAdmin(String organizationId, String userId) throws Exception {
        addOrganizationMemberAsAdmin(organizationId, userId, asList("createWorkspaces"));
    }

    public void addOrganizationAdmin(String organizationId, String userId) throws Exception {
        addOrganizationMemberAsAdmin(organizationId,
                                     userId,
                                     asList("update", "setPermissions", "manageResources", "manageWorkspaces", "createWorkspaces", "delete",
                                     "manageSuborganizations")
        );
    }

    public void addOrganizationMemberAsAdmin(String organizationId, String userId, List<String> actions) throws Exception {
        String apiUrl = apiEndpoint + "permissions";
        PermissionsDto data = newDto(PermissionsDto.class)
                .withDomainId("organization")
                .withInstanceId(organizationId)
                .withUserId(userId)
                .withActions(actions);

        requestFactory.fromUrl(apiUrl)
                      .setAuthorizationHeader(adminTestUser.getAuthToken())
                      .setBody(data)
                      .usePostMethod()
                      .request();
    }
}
