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
