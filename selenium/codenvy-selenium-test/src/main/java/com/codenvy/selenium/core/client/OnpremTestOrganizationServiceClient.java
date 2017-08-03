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

    @Inject
    public OnpremTestOrganizationServiceClient(TestApiEndpointUrlProvider apiEndpointUrlProvider,
                                               HttpJsonRequestFactory requestFactory) {
        this.apiEndpoint = apiEndpointUrlProvider.get().toString();
        this.requestFactory = requestFactory;
    }

    public List<OrganizationDto> getOrganizations(String authToken) throws Exception {
        return getOrganizations(null, authToken);
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

    private String getApiUrl() {return apiEndpoint + "/organization";}

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

    public OrganizationDto createOrganization(String name, String authToken) throws Exception {
        return createOrganization(name, null, authToken);
    }

    public void deleteOrganizationById(String id, String authToken) throws Exception {
        String apiUrl = format("%s/%s", getApiUrl(), id);

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

    public void deleteOrganizationByName(String name, String authToken) throws Exception {
        OrganizationDto organization = getOrganizationByName(name, authToken);

        if (organization != null) {
            deleteOrganizationById(organization.getId(), authToken);
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

    public OrganizationDto getOrganizationByName(String organizationName, String authToken) throws Exception {
        String apiUrl = format("%s/find?name=%s", getApiUrl(), organizationName);
        return requestFactory.fromUrl(apiUrl)
                             .setAuthorizationHeader(authToken)
                             .request()
                             .asDto(OrganizationDto.class);
    }

    public void addOrganizationMember(String organizationId, String userId, String authToken) throws Exception {
        addOrganizationMember(organizationId, userId, asList("createWorkspaces"), authToken);
    }

    public void addOrganizationAdmin(String organizationId, String userId, String authToken) throws Exception {
        addOrganizationMember(organizationId,
                              userId,
                              asList("update", "setPermissions", "manageResources", "manageWorkspaces", "createWorkspaces", "delete",
                                     "manageSuborganizations"),
                              authToken);
    }

    public void addOrganizationMember(String organizationId, String userId, List<String> actions, String authToken) throws Exception {
        String apiUrl = apiEndpoint + "/permissions";
        PermissionsDto data = newDto(PermissionsDto.class)
                .withDomainId("organization")
                .withInstanceId(organizationId)
                .withUserId(userId)
                .withActions(actions);

        requestFactory.fromUrl(apiUrl)
                      .setAuthorizationHeader(authToken)
                      .setBody(data)
                      .usePostMethod()
                      .request();
    }
}
