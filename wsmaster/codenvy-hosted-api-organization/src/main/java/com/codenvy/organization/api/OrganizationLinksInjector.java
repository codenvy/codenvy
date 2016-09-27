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

import com.codenvy.organization.shared.Constants;
import com.codenvy.organization.shared.dto.OrganizationDto;

import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;

import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Helps to inject {@link OrganizationService} related links.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class OrganizationLinksInjector {
    public OrganizationDto injectLinks(OrganizationDto organizationDto, ServiceContext serviceContext) {
        final UriBuilder uriBuilder = serviceContext.getBaseUriBuilder();
        final List<Link> links = new ArrayList<>(2);
        links.add(LinksHelper.createLink(HttpMethod.GET,
                                         uriBuilder.clone()
                                                   .path(OrganizationService.class)
                                                   .path(OrganizationService.class, "getById")
                                                   .build(organizationDto.getId())
                                                   .toString(),
                                         null,
                                         APPLICATION_JSON,
                                         Constants.LINK_REL_SELF));
        links.add(LinksHelper.createLink(HttpMethod.GET,
                                         uriBuilder.clone()
                                                   .path(OrganizationService.class)
                                                   .path(OrganizationService.class, "getByParent")
                                                   .build(organizationDto.getId())
                                                   .toString(),
                                         null,
                                         APPLICATION_JSON,
                                         Constants.LINK_REL_SUBORGANIZATIONS));
        return organizationDto.withLinks(links);
    }
}
