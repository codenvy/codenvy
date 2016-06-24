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
package com.codenvy.api.user.server;

import com.codenvy.api.user.server.dao.AdminUserDao;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.impl.EnvironmentContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link AdminUserService}
 *
 * @author Anatoliy Bazko
 */
@Listeners(value = {MockitoTestNGListener.class, EverrestJetty.class})
public class AdminUserServiceTest {
    @SuppressWarnings("unused")
    ApiExceptionMapper apiExceptionMapper;

    @Mock
    AdminUserDao       userDao;
    @Mock
    UriInfo            uriInfo;
    @Mock
    EnvironmentContext environmentContext;
    @Mock
    SecurityContext    securityContext;

    @InjectMocks
    AdminUserService userService;

    @Test
    public void shouldReturnAllUsers() throws Exception {
        User testUser = new User().withId("test_id").withEmail("test@email");
        when(userDao.getAll(anyInt(), anyInt())).thenReturn(new Page<>(singletonList(testUser), 0, 1, 1));

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/admin/user?maxItems=3&skipCount=4");

        assertEquals(response.getStatusCode(), OK.getStatusCode());
        verify(userDao).getAll(3, 4);

        List<UserDescriptor> users = DtoFactory.getInstance().createListDtoFromJson(response.getBody().print(), UserDescriptor.class);
        assertEquals(users.size(), 1);
        final UserDescriptor fetchedUser = users.get(0);
        assertEquals(fetchedUser.getId(), testUser.getId());
        assertEquals(fetchedUser.getEmail(), testUser.getEmail());
    }

    @Test
    public void shouldThrowServerErrorIfDaoThrowException() throws Exception {
        when(userDao.getAll(anyInt(), anyInt())).thenThrow(new ServerException("some error"));
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/admin/user");

        assertEquals(response.getStatusCode(), INTERNAL_SERVER_ERROR.getStatusCode());
        final ServiceError serviceError = DtoFactory.getInstance().createDtoFromJson(response.body().print(), ServiceError.class);
        assertEquals(serviceError.getMessage(), "some error");
    }
}
