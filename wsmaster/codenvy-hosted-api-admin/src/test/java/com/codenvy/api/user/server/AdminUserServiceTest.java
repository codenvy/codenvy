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

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.util.List;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.user.server.UserLinksInjector;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.impl.EnvironmentContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link AdminUserService}
 *
 * @author Anatoliy Bazko
 */
@Listeners(value = {MockitoTestNGListener.class, EverrestJetty.class})
public class AdminUserServiceTest {
  @SuppressWarnings("unused")
  ApiExceptionMapper apiExceptionMapper;

  @Mock UserManager userManager;
  @Mock UriInfo uriInfo;
  @Mock EnvironmentContext environmentContext;
  @Mock SecurityContext securityContext;
  @Mock UserLinksInjector linksInjector;

  @InjectMocks AdminUserService userService;

  @BeforeMethod
  public void init() {
    when(linksInjector.injectLinks(any(), any())).thenAnswer(inv -> inv.getArguments()[0]);
  }

  @Test
  public void shouldReturnAllUsers() throws Exception {
    UserImpl testUser = new UserImpl("test_id", "test@email", "name");
    when(userManager.getAll(anyInt(), anyInt()))
        .thenReturn(new Page<>(singletonList(testUser), 0, 1, 1));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/admin/user?maxItems=3&skipCount=4");

    assertEquals(response.getStatusCode(), OK.getStatusCode());
    verify(userManager).getAll(3, 4);

    List<UserDto> users =
        DtoFactory.getInstance().createListDtoFromJson(response.getBody().print(), UserDto.class);
    assertEquals(users.size(), 1);
    final UserDto fetchedUser = users.get(0);
    assertEquals(fetchedUser.getId(), testUser.getId());
    assertEquals(fetchedUser.getEmail(), testUser.getEmail());
  }

  @Test
  public void shouldThrowServerErrorIfDaoThrowException() throws Exception {
    when(userManager.getAll(anyInt(), anyInt())).thenThrow(new ServerException("some error"));
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/admin/user");

    assertEquals(response.getStatusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
    final ServiceError serviceError =
        DtoFactory.getInstance().createDtoFromJson(response.body().print(), ServiceError.class);
    assertEquals(serviceError.getMessage(), "some error");
  }

  @Test
  public void throwsBadRequestExceptionWhenMissedEmailAndNameParts() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .expect()
            .statusCode(400)
            .get(SECURE_PATH + "/admin/user/find");

    final String actual =
        DtoFactory.getInstance()
            .createDtoFromJson(response.body().print(), ServiceError.class)
            .getMessage();
    assertEquals(actual, "Missed user's e-mail/name part");
  }

  @Test
  public void throwsBadRequestExceptionWhenProvidedTwoSearchParams() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .expect()
            .statusCode(400)
            .get(SECURE_PATH + "/admin/user/find?emailPart=admin@&namePart=admin");

    final String actual =
        DtoFactory.getInstance()
            .createDtoFromJson(response.body().print(), ServiceError.class)
            .getMessage();
    assertEquals(actual, "Expected either user's e-mail or name part, while both values received");
  }

  @Test
  public void throwsServerExceptionWhenAnyInternalServerErrorOccur() throws Exception {
    final String errMsg = "server error";
    when(userManager.getByEmailPart(anyString(), anyInt(), anyInt()))
        .thenThrow(new ServerException(errMsg));
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .expect()
            .statusCode(500)
            .get(SECURE_PATH + "/admin/user/find?emailPart=admin@");

    final String actual =
        DtoFactory.getInstance()
            .createDtoFromJson(response.body().print(), ServiceError.class)
            .getMessage();
    assertEquals(actual, errMsg);
  }

  @Test
  public void findsUserByEmailPart() throws Exception {
    final Page<User> users =
        new Page<>(singletonList(new UserImpl("1", "admin@codenvy.com", "admin")), 1, 1, 1);
    doReturn(users).when(userManager).getByEmailPart("admin@", 30, 0);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/admin/user/find?emailPart=admin@");

    final List<User> result =
        unwrapDtoList(response, UserDto.class)
            .stream()
            .map(u -> new UserImpl(u.getId(), u.getEmail(), u.getName()))
            .collect(toList());
    assertEquals(result, users.getItems());
  }

  @Test
  public void findsUserByNamePart() throws Exception {
    final Page<User> users =
        new Page<>(singletonList(new UserImpl("1", "test@test.com", "test")), 1, 1, 1);
    doReturn(users).when(userManager).getByNamePart("test", 30, 0);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/admin/user/find?namePart=test");

    final List<User> result =
        unwrapDtoList(response, UserDto.class)
            .stream()
            .map(u -> new UserImpl(u.getId(), u.getEmail(), u.getName()))
            .collect(toList());
    assertEquals(result, users.getItems());
  }

  private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance()
        .createListDtoFromJson(response.body().print(), dtoClass)
        .stream()
        .collect(toList());
  }
}
