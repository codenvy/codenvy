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
package com.codenvy.api.workspace.server.filters;

import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.workspace.server.RecipeScriptDownloadService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.codenvy.api.workspace.server.WorkspaceDomain.DOMAIN_ID;
import static com.codenvy.api.workspace.server.WorkspaceDomain.USE;
import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link RecipeScriptDownloadPermissionFilter}
 *
 * @author Mihail Kuznyetsov.
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class RecipeScriptDownloadPermissionFilterTest {
    @SuppressWarnings("unused")
    private static final EnvironmentFilter FILTER = new EnvironmentFilter();

    @Mock
    private static Subject subject;
    @Mock
    RecipeScriptDownloadService          service;
    @InjectMocks
    RecipeScriptDownloadPermissionFilter filter;

    @Test
    public void shouldCheckPermission() throws Exception {
        when(subject.hasPermission(eq(DOMAIN_ID), eq("machine123"), eq(USE))).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/recipe/script/workspace123/machine123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).getRecipeScript(eq("workspace123"), eq("machine123"));
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", USE);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void shouldThrowExceptionWhenCallingUnlistedMethod() throws Exception {
        GenericResourceMethod genericMethodResource = Mockito.mock(GenericResourceMethod.class);
        when(genericMethodResource.getMethod()).thenReturn(this.getClass().getDeclaredMethod("shouldThrowExceptionWhenCallingUnlistedMethod"));
        Object[] argument = new Object[0];
        filter.filter(genericMethodResource, argument);
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }

}
