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
package com.codenvy.api.user.server;

import com.codenvy.api.license.server.SystemLicenseManager;
import com.codenvy.api.permission.server.SystemDomain;
import com.jayway.restassured.response.Response;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static com.codenvy.api.license.shared.model.Constants.UNABLE_TO_ADD_ACCOUNT_BECAUSE_OF_LICENSE;
import static com.codenvy.api.user.server.UserServicePermissionsFilter.MANAGE_USERS_ACTION;
import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for {@link UserServicePermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class UserServicePermissionsFilterTest {
    @SuppressWarnings("unused")
    private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();
    @SuppressWarnings("unused")
    private static final EnvironmentFilter  FILTER = new EnvironmentFilter();
    public static final String TOKEN = "token123";
    public static final String USER_ID = "userok";

    @Mock
    WorkspaceManager     workspaceManager;
    @Mock
    UserManager          userManager;
    @Mock
    SystemLicenseManager licenseManager;

    UserServicePermissionsFilter permissionsFilter;

    @Mock
    private static Subject subject;

    @Mock
    UserService service;

    @BeforeMethod
    public void setUp() throws ServerException {
        permissionsFilter = new UserServicePermissionsFilter(true, licenseManager);
        when(subject.getUserId()).thenReturn(USER_ID);
    }

    @Test
    public void shouldNotCheckPermissionsOnUserCreationFromToken() throws Exception {
        when(licenseManager.canUserBeAdded()).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/user?token=" + TOKEN);

        assertEquals(response.getStatusCode(), 204);
        verify(service).create(eq(null), eq(TOKEN), anyBoolean());
        verifyZeroInteractions(subject);
    }

    @Test
    public void shouldThrowAUserExceptionWhenCreatingUserIsBeyondLicense() throws Exception {
        when(licenseManager.canUserBeAdded()).thenReturn(false);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/user?token=" + TOKEN);

        assertEquals(response.getStatusCode(), 403);
        assertEquals(response.getBody().prettyPrint(), "{\n"
                                                       + "    \"message\": \"" + UNABLE_TO_ADD_ACCOUNT_BECAUSE_OF_LICENSE + "\"\n"
                                                       + "}");
        verify(service, never()).create(any(), any(), any());
        verify(subject, never()).checkPermission(any(), any(), any());
    }

    @Test
    public void shouldThrowAnAdminUserExceptionWhenCreatingUserIsBeyondLicense() throws Exception {
        when(licenseManager.canUserBeAdded()).thenReturn(false);
        when(subject.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION)).thenReturn(true);
        long allowedUsersNumber = 10;
        when(licenseManager.getAllowedUserNumber()).thenReturn(allowedUsersNumber);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/user?token=" + TOKEN);

        assertEquals(response.getStatusCode(), 403);
        assertEquals(response.getBody().prettyPrint(), format("{\n"
                                                       + "    \"message\": \"The user cannot be added. You have %s users in Codenvy which is the maximum allowed by your current license.\"\n"
                                                       + "}", allowedUsersNumber));
        verify(service, never()).create(any(), any(), any());
        verify(subject, never()).checkPermission(any(), any(), any());
    }


    @Test
    public void shouldThrowAnExceptionWhenCheckUserLicenseError() throws Exception {
        doThrow(new ServerException("read users error")).when(licenseManager).canUserBeAdded();

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/user?token=" + TOKEN);

        assertEquals(response.getStatusCode(), 500);
    }

    @Test
    public void shouldCheckPermissionsOnUserCreationFromEntity() throws Exception {
        when(licenseManager.canUserBeAdded()).thenReturn(true);

        final UserDto userToCreate = DtoFactory.newDto(UserDto.class)
                                               .withId("user123")
                                               .withEmail("test @test.com")
                                               .withPassword("***");

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                             .contentType("application/json")
                                         .body(userToCreate)
                                         .when()
                                         .post(SECURE_PATH + "/user");

        assertEquals(response.getStatusCode(), 204);
        verify(service).create(any(), eq(null), anyBoolean());
        verify(subject).checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION);
    }

    @Test
    public void shouldCheckPermissionsOnUserRemoving() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .delete(SECURE_PATH + "/user/user123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).remove(eq("user123"));
        verify(subject).checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION);
    }

    @Test
    public void shouldNotCheckPermissionsOnUserSelfRemoving() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .delete(SECURE_PATH + "/user/" + USER_ID);

        assertEquals(response.getStatusCode(), 204);
        verify(service).remove(eq(USER_ID));
        verify(subject, never()).checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION);
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "User is not authorized to perform this operation")
    public void shouldThrowForbiddenExceptionWhenRequestedUnknownMethod() throws Exception {
        final GenericResourceMethod mock = mock(GenericResourceMethod.class);
        Method injectLinks = AdminUserService.class.getMethod("getServiceDescriptor");
        when(mock.getMethod()).thenReturn(injectLinks);

        permissionsFilter.filter(mock, new Object[] {});
    }

    @Test(dataProvider = "publicMethods")
    public void shouldNotCheckPermissionsForPublicMethods(String methodName) throws Exception {
        final Method method = Stream.of(UserService.class.getMethods())
                                    .filter(userServiceMethod -> userServiceMethod.getName().equals(methodName))
                                    .findAny()
                                    .orElseGet(null);
        assertNotNull(method);

        final GenericResourceMethod mock = mock(GenericResourceMethod.class);
        when(mock.getMethod()).thenReturn(method);

        permissionsFilter.filter(mock, new Object[] {});

        verifyNoMoreInteractions(subject);
    }

    @DataProvider(name = "publicMethods")
    private Object[][] pathsProvider() {
        return new Object[][] {
                {"getCurrent"},
                {"updatePassword"},
                {"getById"},
                {"find"},
                {"getSettings"}
        };
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }
}
