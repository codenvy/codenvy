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
package com.codenvy.resource.api;

import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.resource.api.free.FreeResourcesLimitService;
import com.codenvy.resource.api.free.FreeResourcesLimitServicePermissionsFilter;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codenvy.organization.spi.impl.OrganizationImpl.ORGANIZATIONAL_ACCOUNT;
import static com.jayway.restassured.RestAssured.given;
import static org.eclipse.che.api.user.server.model.impl.UserImpl.PERSONAL_ACCOUNT;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link ResourceServicePermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class, EverrestJetty.class})
public class ResourceServicePermissionsFilterTest {
    @SuppressWarnings("unused")
    private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();
    @SuppressWarnings("unused")
    private static final EnvironmentFilter  FILTER = new EnvironmentFilter();

    @Mock
    private AccountManager accountManager;

    @Mock
    private AccountImpl account;

    @Mock
    private ResourceService service;

    @Mock
    private FreeResourcesLimitService freeResourcesLimitService;

    @Mock
    private static Subject subject;


    @Spy
    @InjectMocks
    private ResourceServicePermissionsFilter filter;

    @BeforeMethod
    public void setUp() throws Exception {
        when(accountManager.getById(any())).thenReturn(account);

        doReturn(true).when(filter).canSeeResources(any(), anyString());
    }

    @Test
    public void shouldTestThatAllPublicMethodsAreCoveredByPermissionsFilter() throws Exception {
        //given
        final List<String> collect = Stream.of(ResourceService.class.getDeclaredMethods())
                                           .filter(method -> Modifier.isPublic(method.getModifiers()))
                                           .map(Method::getName)
                                           .collect(Collectors.toList());

        //then
        assertEquals(collect.size(), 3);
        assertTrue(collect.contains(ResourceServicePermissionsFilter.GET_TOTAL_RESOURCES_METHOD));
        assertTrue(collect.contains(ResourceServicePermissionsFilter.GET_AVAILABLE_RESOURCES_METHOD));
        assertTrue(collect.contains(ResourceServicePermissionsFilter.GET_USED_RESOURCES_METHOD));
    }

    @Test
    public void shouldNotProceedFreeResourcesPath() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .expect()
               .statusCode(204)
               .when()
               .get(SECURE_PATH + "/resource/free/account123");

        verifyZeroInteractions(filter);
        verify(freeResourcesLimitService).getFreeResourcesLimit(anyString());
    }

    @Test
    public void shouldCheckPermissionsOnGettingTotalResources() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .expect()
               .statusCode(200)
               .when()
               .get(SECURE_PATH + "/resource/account123");

        verify(filter).canSeeResources(eq(subject), eq("account123"));
        verify(service).getTotalResources(eq("account123"));
    }

    @Test
    public void shouldCheckPermissionsOnGettingAvailableResources() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .expect()
               .statusCode(200)
               .when()
               .get(SECURE_PATH + "/resource/account123/available");

        verify(filter).canSeeResources(eq(subject), eq("account123"));
        verify(service).getAvailableResources(eq("account123"));
    }

    @Test
    public void shouldCheckPermissionsOnGettingUsedResources() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .expect()
               .statusCode(200)
               .when()
               .get(SECURE_PATH + "/resource/account123/used");

        verify(filter).canSeeResources(eq(subject), eq("account123"));
        verify(service).getUsedResources(eq("account123"));
    }

    @Test(dataProvider = "coveredPaths")
    public void shouldDenyRequestWhenUserDoesNotHasPermissionsToSeeResources(String path) throws Exception {
        doReturn(false).when(filter).canSeeResources(any(), anyString());

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .expect()
               .statusCode(403)
               .when()
               .get(SECURE_PATH + path);

        verify(filter).canSeeResources(eq(subject), eq("account123"));
    }

    @Test
    public void shouldAllowToSeeResourcesIfCurrentUserIdEqualsToRequestedPersonalAccountId() throws Exception {
        when(filter.canSeeResources(any(), anyString())).thenCallRealMethod();
        when(account.getType()).thenReturn(PERSONAL_ACCOUNT);
        when(account.getId()).thenReturn("user123");
        when(subject.getUserId()).thenReturn("user123");

        boolean canSeeResources = filter.canSeeResources(subject, "user123");

        assertTrue(canSeeResources);
        verify(accountManager).getById(eq("user123"));
    }

    @Test
    public void shouldNotAllowToSeeResourcesIfCurrentUserIdDoesNotEqualToRequestedPersonalAccountId() throws Exception {
        when(filter.canSeeResources(any(), anyString())).thenCallRealMethod();
        when(account.getType()).thenReturn(PERSONAL_ACCOUNT);
        when(account.getId()).thenReturn("user123");
        when(subject.getUserId()).thenReturn("user234");

        boolean canSeeResources = filter.canSeeResources(subject, "user123");

        assertFalse(canSeeResources);
        verify(accountManager).getById(eq("user123"));
    }

    @Test
    public void shouldNotAllowToSeeResourcesIfCurrentUserDoesNotHaveAnyPermissionsForOrganization() throws Exception {
        when(filter.canSeeResources(any(), anyString())).thenCallRealMethod();
        when(account.getType()).thenReturn(ORGANIZATIONAL_ACCOUNT);
        when(account.getId()).thenReturn("organization123");

        boolean canSeeResources = filter.canSeeResources(subject, "organization123");

        assertFalse(canSeeResources);
        verify(accountManager).getById(eq("organization123"));
        verify(subject).hasPermission(OrganizationDomain.DOMAIN_ID, "organization123", OrganizationDomain.CREATE_WORKSPACES);
        verify(subject).hasPermission(OrganizationDomain.DOMAIN_ID, "organization123", OrganizationDomain.MANAGE_RESOURCES);
        verify(subject).hasPermission(OrganizationDomain.DOMAIN_ID, "organization123", OrganizationDomain.MANAGE_WORKSPACES);
    }

    @Test(dataProvider = "requiredPermissions")
    public void shouldAllowToSeeResourcesIfCurrentUserHasAtLeastOneRequiredPermissionForOrganization(String requiredPermissions)
            throws Exception {

        when(filter.canSeeResources(any(), anyString())).thenCallRealMethod();
        when(account.getType()).thenReturn(ORGANIZATIONAL_ACCOUNT);
        when(account.getId()).thenReturn("organization123");
        when(subject.hasPermission(OrganizationDomain.DOMAIN_ID, "organization123", requiredPermissions)).thenReturn(true);

        boolean canSeeResources = filter.canSeeResources(subject, "organization123");

        assertTrue(canSeeResources);
        verify(accountManager).getById(eq("organization123"));
    }

    @DataProvider(name = "requiredPermissions")
    public Object[][] requiredPermissionsProvider() {
        return new Object[][] {
                {OrganizationDomain.MANAGE_RESOURCES},
                {OrganizationDomain.CREATE_WORKSPACES},
                {OrganizationDomain.MANAGE_WORKSPACES},
        };
    }

    @DataProvider(name = "coveredPaths")
    public Object[][] pathsProvider() {
        return new Object[][] {
                {"/resource/account123"},
                {"/resource/account123/available"},
                {"/resource/account123/used"},
        };
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }
}
