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
package com.codenvy.organization.api.permissions;

import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.api.resource.OrganizationResourcesDistributionService;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.shared.dto.ResourceDto;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CheJsonProvider;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codenvy.organization.api.permissions.OrganizationPermissionsFilter.MANAGE_ORGANIZATIONS_ACTION;
import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.emptyList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link OrganizationResourceDistributionServicePermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class OrganizationResourceDistributionServicePermissionsFilterTest {
    @SuppressWarnings("unused")
    private static final ApiExceptionMapper MAPPER        = new ApiExceptionMapper();
    @SuppressWarnings("unused")
    private static final EnvironmentFilter  FILTER        = new EnvironmentFilter();
    @SuppressWarnings("unused")
    private static final CheJsonProvider    JSON_PROVIDER = new CheJsonProvider(new HashSet<>());

    private static final String SUBORGANIZATION     = "org123";
    private static final String PARENT_ORGANIZATION = "parentOrg123";

    @Mock
    private OrganizationResourcesDistributionService service;

    @Mock
    private OrganizationManager manager;

    @Mock
    private static Subject subject;

    @InjectMocks
    private OrganizationResourceDistributionServicePermissionsFilter permissionsFilter;

    @BeforeMethod
    public void setUp() throws Exception {
        when(manager.getById(SUBORGANIZATION)).thenReturn(new OrganizationImpl(SUBORGANIZATION, "testOrg", PARENT_ORGANIZATION));
        when(manager.getById(PARENT_ORGANIZATION)).thenReturn(new OrganizationImpl(PARENT_ORGANIZATION, "parentOrg", null));

        when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(true);
    }

    @Test
    public void shouldTestThatAllPublicMethodsAreCoveredByPermissionsFilter() throws Exception {
        //given
        final List<String> collect = Stream.of(OrganizationResourcesDistributionService.class.getDeclaredMethods())
                                           .filter(method -> Modifier.isPublic(method.getModifiers()))
                                           .map(Method::getName)
                                           .collect(Collectors.toList());

        //then
        assertEquals(collect.size(), 3);
        assertTrue(collect.contains(OrganizationResourceDistributionServicePermissionsFilter.DISTRIBUTE_RESOURCES_METHOD));
        assertTrue(collect.contains(OrganizationResourceDistributionServicePermissionsFilter.GET_DISTRIBUTED_RESOURCES_METHOD));
        assertTrue(collect.contains(OrganizationResourceDistributionServicePermissionsFilter.RESET_DISTRIBUTED_RESOURCES));
    }

    @Test
    public void shouldCheckManageResourcesPermissionsOnDistributingResourcesForSuborganization() throws Exception {
        List<ResourceDto> resources = Collections.singletonList(DtoFactory.newDto(ResourceDto.class)
                                                                          .withType("test")
                                                                          .withAmount(123)
                                                                          .withUnit("unit"));
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType(MediaType.APPLICATION_JSON)
               .body(resources)
               .expect()
               .statusCode(204)
               .when()
               .post(SECURE_PATH + "/organization/resource/" + SUBORGANIZATION);

        verify(service).distribute(SUBORGANIZATION, resources);
        verify(subject).hasPermission(OrganizationDomain.DOMAIN_ID, PARENT_ORGANIZATION, OrganizationDomain.MANAGE_RESOURCES);
    }

    @Test
    public void shouldNotCheckPermissionsOnDistributingResourcesForRootOrganization() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType(MediaType.APPLICATION_JSON)
               .body(emptyList())
               .expect()
               .statusCode(204)
               .when()
               .post(SECURE_PATH + "/organization/resource/" + PARENT_ORGANIZATION);

        verify(service).distribute(PARENT_ORGANIZATION, emptyList());
        verify(subject, never()).hasPermission(anyString(), anyString(), anyString());
    }

    @Test
    public void shouldCheckManageResourcesPermissionsOnResettingDistributedResourcesForSuborganization() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .expect()
               .statusCode(204)
               .when()
               .delete(SECURE_PATH + "/organization/resource/" + SUBORGANIZATION);

        verify(service).reset(SUBORGANIZATION);
        verify(subject).hasPermission(OrganizationDomain.DOMAIN_ID, PARENT_ORGANIZATION, OrganizationDomain.MANAGE_RESOURCES);
    }

    @Test
    public void shouldNotCheckPermissionsOnResettingDistributedResourcesForRootOrganization() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .expect()
               .statusCode(204)
               .when()
               .delete(SECURE_PATH + "/organization/resource/" + PARENT_ORGANIZATION);

        verify(service).reset(PARENT_ORGANIZATION);
        verify(subject, never()).hasPermission(anyString(), anyString(), anyString());
    }

    @Test
    public void shouldCheckManageResourcesPermissionsOnGettingDistributedResourcesWhenUserDoesNotHaveManageOrganizationsPermission()
            throws Exception {
        when(subject.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_ORGANIZATIONS_ACTION)).thenReturn(false);

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .expect()
               .statusCode(204)
               .when()
               .get(SECURE_PATH + "/organization/resource/" + PARENT_ORGANIZATION);

        verify(service).getDistributedResources(eq(PARENT_ORGANIZATION), anyInt(), anyLong());
        verify(subject).hasPermission(SystemDomain.DOMAIN_ID, null, OrganizationPermissionsFilter.MANAGE_ORGANIZATIONS_ACTION);
    }

    @Test
    public void shouldNotCheckManageResourcesPermissionsOnGettingDistributedResourcesWhenUserHasManageOrganizationsPermission()
            throws Exception {
        when(subject.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_ORGANIZATIONS_ACTION)).thenReturn(true);

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .expect()
               .statusCode(204)
               .when()
               .get(SECURE_PATH + "/organization/resource/" + PARENT_ORGANIZATION);

        verify(service).getDistributedResources(eq(PARENT_ORGANIZATION), anyInt(), anyLong());
        verify(subject).hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_ORGANIZATIONS_ACTION);
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "The user does not have permission to perform this operation")
    public void shouldThrowForbiddenExceptionWhenRequestedUnknownMethod() throws Exception {
        final GenericResourceMethod mock = mock(GenericResourceMethod.class);
        Method unknownMethod = OrganizationResourcesDistributionService.class.getMethod("getServiceDescriptor");
        when(mock.getMethod()).thenReturn(unknownMethod);

        permissionsFilter.filter(mock, new Object[] {});
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        @Override
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }
}
