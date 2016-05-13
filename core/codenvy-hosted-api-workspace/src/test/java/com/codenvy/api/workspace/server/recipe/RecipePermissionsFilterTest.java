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
package com.codenvy.api.workspace.server.recipe;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.machine.server.recipe.RecipeService;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericMethodResource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static com.codenvy.api.workspace.server.recipe.RecipeDomain.DELETE;
import static com.codenvy.api.workspace.server.recipe.RecipeDomain.READ;
import static com.codenvy.api.workspace.server.recipe.RecipeDomain.UPDATE;
import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link RecipePermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class RecipePermissionsFilterTest {
    @SuppressWarnings("unused")
    private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();
    @SuppressWarnings("unused")
    private static final EnvironmentFilter  FILTER = new EnvironmentFilter();

    @SuppressWarnings("unused")
    @InjectMocks
    RecipePermissionsFilter permissionsFilter;

    @Mock
    private static Subject subject;

    @Mock
    RecipeService service;

    @Test
    public void shouldNotCheckPermissionsOnStackCreating() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 204);
        verifyZeroInteractions(subject);
    }

    @Test
    public void shouldCheckPermissionsOnStackReading() throws Exception {
        when(subject.hasPermission("recipe", "recipe123", READ)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/recipe/recipe123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).getRecipe("recipe123");
        verify(subject).hasPermission(eq("recipe"), eq("recipe123"), eq(READ));
    }

    @Test
    public void shouldCheckPermissionsOnStackScriptReading() throws Exception {
        when(subject.hasPermission("recipe", "recipe123", READ)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/recipe/recipe123/script");

        assertEquals(response.getStatusCode(), 204);
        verify(service).getRecipeScript("recipe123");
        verify(subject).hasPermission(eq("recipe"), eq("recipe123"), eq(READ));
    }

    @Test
    public void shouldCheckPermissionsOnStackUpdating() throws Exception {
        when(subject.hasPermission("recipe", "recipe123", UPDATE)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .body(DtoFactory.newDto(RecipeUpdate.class)
                                                         .withId("recipe123"))
                                         .when()
                                         .put(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 204);
        verify(service).updateRecipe(any());
        verify(subject).hasPermission(eq("recipe"), eq("recipe123"), eq(UPDATE));
    }

    @Test
    public void shouldCheckPermissionsOnStackRemoving() throws Exception {
        when(subject.hasPermission("recipe", "recipe123", DELETE)).thenReturn(true);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .delete(SECURE_PATH + "/recipe/recipe123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).removeRecipe(eq("recipe123"));
        verify(subject).hasPermission(eq("recipe"), eq("recipe123"), eq(DELETE));
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenUserDoesNotHavePermissionsForPerformOperationOnStackRemoving() throws Exception {
        when(subject.hasPermission("recipe", "recipe123", DELETE)).thenReturn(false);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .delete(SECURE_PATH + "/recipe/recipe123");

        assertEquals(response.getStatusCode(), 403);
        assertEquals(unwrapError(response), "The user does not have permission to " + DELETE + " recipe with id 'recipe123'");
        verifyZeroInteractions(service);
    }

    @Test
    public void shouldNotCheckPermissionsOnStacksSearching() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .get(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 200);
        verify(service).searchRecipes(anyListOf(String.class), anyString(), anyInt(), anyInt());
        verifyZeroInteractions(subject);
    }

    @Test
    public void shouldNotCheckPermissionsOnRecipeCreating() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/json")
                                         .when()
                                         .post(SECURE_PATH + "/recipe");

        assertEquals(response.getStatusCode(), 204);
        verify(service).createRecipe(any());
        verifyZeroInteractions(subject);
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "The user does not have permission to perform this operation")
    public void shouldThrowForbiddenExceptionWhenRequestedUnknownMethod() throws Exception {
        final GenericMethodResource mock = mock(GenericMethodResource.class);
        Method injectLinks = WorkspaceService.class.getMethod("getServiceDescriptor");
        when(mock.getMethod()).thenReturn(injectLinks);

        permissionsFilter.filter(mock, new Object[] {});
    }

    @Test(dataProvider = "coveredPaths")
    public void shouldThrowForbiddenExceptionWhenUserDoesNotHavePermissionsForPerformOperation(String path,
                                                                                               String method,
                                                                                               String action) throws Exception {
        when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);

        Response response = request(given().auth()
                                           .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                           .when(),
                                    SECURE_PATH + path,
                                    method);

        assertEquals(response.getStatusCode(), 403);
        assertEquals(unwrapError(response), "The user does not have permission to " + action + " recipe with id 'recipe123'");
        verifyZeroInteractions(service);
    }

    @DataProvider(name = "coveredPaths")
    public Object[][] pathsProvider() {
        return new Object[][] {
                {"/recipe/recipe123", "get", READ},
                {"/recipe/recipe123", "delete", DELETE},
                {"/recipe/recipe123/script", "get", READ},
        };
    }

    private Response request(RequestSpecification request, String path, String method) {
        switch (method) {
            case "post":
                return request.post(path);
            case "get":
                return request.get(path);
            case "delete":
                return request.delete(path);
            case "put":
                return request.put(path);
        }
        throw new RuntimeException("Unsupported method");
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
