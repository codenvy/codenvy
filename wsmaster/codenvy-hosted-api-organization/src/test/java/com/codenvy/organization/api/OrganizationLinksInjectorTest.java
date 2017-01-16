/*
 *  [2012] - [2017] Codenvy, S.A.
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
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.UriBuilder;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for {@link OrganizationLinksInjector}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationLinksInjectorTest {
    private static final String URI_BASE = "http://localhost:8080";

    @Mock
    ServiceContext context;

    OrganizationLinksInjector organizationLinksInjector = new OrganizationLinksInjector();

    @BeforeMethod
    public void setUp() {
        final UriBuilder uriBuilder = new UriBuilderImpl();
        uriBuilder.uri(URI_BASE);

        when(context.getBaseUriBuilder()).thenReturn(uriBuilder);
    }

    @Test
    public void shouldInjectLinks() {
        final OrganizationDto organization = DtoFactory.newDto(OrganizationDto.class)
                                                       .withId("org123");

        final OrganizationDto withLinks = organizationLinksInjector.injectLinks(organization, context);

        assertEquals(withLinks.getLinks().size(), 2);
        assertNotNull(withLinks.getLink(Constants.LINK_REL_SELF));
        assertNotNull(withLinks.getLink(Constants.LINK_REL_SUBORGANIZATIONS));
    }
}
