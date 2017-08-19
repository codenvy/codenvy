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
package com.codenvy.api.user.server;

import static com.codenvy.api.user.server.UserServicePermissionsFilter.MANAGE_USERS_ACTION;
import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.codenvy.api.permission.server.SystemDomain;
import com.jayway.restassured.response.Response;
import java.lang.reflect.Method;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link AdminUserServicePermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class AdminUserServicePermissionsFilterTest {
  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @Mock WorkspaceManager workspaceManager;
  @Mock UserManager userManager;

  @SuppressWarnings("unused")
  @InjectMocks
  AdminUserServicePermissionsFilter permissionsFilter;

  @Mock private static Subject subject;

  @Mock AdminUserService service;

  @Test
  public void shouldCheckPermissionsByUsersFetching() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/admin/user");

    assertEquals(response.getStatusCode(), 204);
    verify(service).getAll(anyInt(), anyInt());
    verify(subject).checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION);
  }

  @Test(
    expectedExceptions = ForbiddenException.class,
    expectedExceptionsMessageRegExp = "User is not authorized to perform this operation"
  )
  public void shouldThrowForbiddenExceptionWhenRequestedUnknownMethod() throws Exception {
    final GenericResourceMethod mock = mock(GenericResourceMethod.class);
    Method getServiceDescriptor = AdminUserService.class.getMethod("getServiceDescriptor");
    when(mock.getMethod()).thenReturn(getServiceDescriptor);

    permissionsFilter.filter(mock, new Object[] {});
  }

  @Test
  public void shouldThrowForbiddenExceptionWhenUserDoesNotHavePermissionsForGetAllUsers()
      throws Exception {
    doThrow(new ForbiddenException("The user does not have permission to readUsers"))
        .when(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION);

    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/admin/user");

    assertEquals(response.getStatusCode(), 403);
    assertEquals(unwrapError(response), "The user does not have permission to readUsers");

    verifyZeroInteractions(service);
  }

  @Test
  public void allowsToSearchUsersByEmailOrNameFragments() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/admin/user/find?name=admin");

    assertEquals(response.getStatusCode(), 204);
    verify(service).find(anyString(), anyString(), anyInt(), anyInt());
    verify(subject).checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION);
  }

  @Test
  public void throwsForbiddenExceptionWhenUserDoesNotHavePermissionsForSearchingUsers()
      throws Exception {
    doThrow(new ForbiddenException("The user does not have permission to readUsers"))
        .when(subject)
        .checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_USERS_ACTION);

    Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/admin/user/find");

    assertEquals(response.getStatusCode(), 403);
    assertEquals(unwrapError(response), "The user does not have permission to readUsers");
    verifyZeroInteractions(service);
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
