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
package com.codenvy.api.permission.server.filter;

import com.codenvy.api.permission.server.PermissionsService;
import com.codenvy.api.permission.shared.dto.PermissionsDto;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.codenvy.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;
import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link SetPermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class, EverrestJetty.class})
public class SetPermissionsFilterTest {
    @SuppressWarnings("unused")
    private static final EnvironmentFilter FILTER = new EnvironmentFilter();

    @Mock
    static Subject subject;

    @Mock
    PermissionsService permissionsService;

    @InjectMocks
    SetPermissionsFilter permissionsFilter;

    @BeforeMethod
    public void setUp() {
        when(subject.getUserId()).thenReturn("user123");
    }

    @Test
    public void shouldRespond403IfUserDoesNotHaveAnyPermissionsForInstance() throws Exception {
        when(subject.hasPermission("test", "test123", SET_PERMISSIONS)).thenReturn(false);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(DtoFactory.newDto(PermissionsDto.class)
                                                         .withDomainId("test")
                                                         .withInstanceId("test123")
                                                         .withUserId("user123")
                                                         .withActions(Collections.singletonList("read")))
                                         .when()
                                         .post(SECURE_PATH + "/permissions");

        assertEquals(response.getStatusCode(), 403);
        assertEquals(unwrapError(response), "User can't edit permissions for this instance");
        verifyZeroInteractions(permissionsService);
    }

    @Test
    public void shouldDoChainIfUserHasAnyPermissionsForInstance() throws Exception {
        when(subject.hasPermission("test", "test123", SET_PERMISSIONS)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(DtoFactory.newDto(PermissionsDto.class)
                                                         .withDomainId("test")
                                                         .withInstanceId("test123")
                                                         .withUserId("user123")
                                                         .withActions(Collections.singletonList("read")))
                                         .when()
                                         .post(SECURE_PATH + "/permissions");

        assertEquals(response.getStatusCode(), 204);
        verify(permissionsService).storePermissions(any());
    }

    private static String unwrapError(Response response) {
        return unwrapDto(response, ServiceError.class).getMessage();
    }

    private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }
}
