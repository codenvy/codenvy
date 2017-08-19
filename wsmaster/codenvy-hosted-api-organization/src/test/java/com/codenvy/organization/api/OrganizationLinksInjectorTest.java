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
package com.codenvy.organization.api;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.codenvy.organization.shared.Constants;
import com.codenvy.organization.shared.dto.OrganizationDto;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link OrganizationLinksInjector}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationLinksInjectorTest {
  private static final String URI_BASE = "http://localhost:8080";

  @Mock ServiceContext context;

  OrganizationLinksInjector organizationLinksInjector = new OrganizationLinksInjector();

  @BeforeMethod
  public void setUp() {
    final UriBuilder uriBuilder = new UriBuilderImpl();
    uriBuilder.uri(URI_BASE);

    when(context.getBaseUriBuilder()).thenReturn(uriBuilder);
  }

  @Test
  public void shouldInjectLinks() {
    final OrganizationDto organization = DtoFactory.newDto(OrganizationDto.class).withId("org123");

    final OrganizationDto withLinks = organizationLinksInjector.injectLinks(organization, context);

    assertEquals(withLinks.getLinks().size(), 2);
    assertNotNull(withLinks.getLink(Constants.LINK_REL_SELF));
    assertNotNull(withLinks.getLink(Constants.LINK_REL_SUBORGANIZATIONS));
  }
}
