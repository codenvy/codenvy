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

import com.codenvy.api.permission.server.PermissionsService;
import com.codenvy.api.permission.shared.dto.PermissionsDto;
import com.codenvy.api.workspace.server.recipe.RecipeDomain;
import com.codenvy.api.workspace.server.stack.StackDomain;
import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link AclSetPermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class, EverrestJetty.class})
public class AclSetPermissionsFilterTest {

    @Mock
    PermissionsService permissionsService;

    @InjectMocks
    AclSetPermissionsFilter permissionsFilter;

    @Test
    public void shouldRespond403IfUserTryToUsePublicSearchActionForRecipe() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(DtoFactory.newDto(PermissionsDto.class)
                                                         .withDomain(RecipeDomain.DOMAIN_ID)
                                                         .withInstance("recipe123")
                                                         .withUser("*")
                                                         .withActions(Collections.singletonList(RecipeDomain.SEARCH)))
                                         .when()
                                         .post(SECURE_PATH + "/permissions");

        assertEquals(response.getStatusCode(), 403);
        assertEquals(unwrapError(response), "Public permissions support only 'read' action");
        verifyZeroInteractions(permissionsService);
    }

    @Test
    public void shouldBeAbleToStorePublicReadActionForRecipe() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(DtoFactory.newDto(PermissionsDto.class)
                                                         .withDomain(RecipeDomain.DOMAIN_ID)
                                                         .withInstance("recipe123")
                                                         .withUser("*")
                                                         .withActions(Collections.singletonList(RecipeDomain.READ)))
                                         .when()
                                         .post(SECURE_PATH + "/permissions");

        assertEquals(response.getStatusCode(), 204);
        verify(permissionsService).storePermissions(any());
    }

    @Test
    public void shouldRespond403IfUserTryToStorePublicSearchActionForStack() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(DtoFactory.newDto(PermissionsDto.class)
                                                         .withDomain(StackDomain.DOMAIN_ID)
                                                         .withInstance("stack123")
                                                         .withUser("*")
                                                         .withActions(Collections.singletonList(StackDomain.SEARCH)))
                                         .when()
                                         .post(SECURE_PATH + "/permissions");

        assertEquals(response.getStatusCode(), 403);
        assertEquals(unwrapError(response), "Public permissions support only 'read' action");
        verifyZeroInteractions(permissionsService);
    }

    @Test
    public void shouldBeAbleToStorePublicReadActionForStack() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(DtoFactory.newDto(PermissionsDto.class)
                                                         .withDomain(StackDomain.DOMAIN_ID)
                                                         .withInstance("recipe123")
                                                         .withUser("*")
                                                         .withActions(Collections.singletonList(StackDomain.READ)))
                                         .when()
                                         .post(SECURE_PATH + "/permissions");

        assertEquals(response.getStatusCode(), 204);
        verify(permissionsService).storePermissions(any());
    }

    @Test
    public void shouldBeAbleToStorePublicSearchActionForOtherDomain() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(DtoFactory.newDto(PermissionsDto.class)
                                                         .withDomain("test")
                                                         .withInstance("test123123")
                                                         .withUser("*")
                                                         .withActions(Arrays.asList(StackDomain.READ, StackDomain.SEARCH)))
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
}
