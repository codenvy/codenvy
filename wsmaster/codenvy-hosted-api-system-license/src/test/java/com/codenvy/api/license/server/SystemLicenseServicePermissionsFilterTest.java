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
package com.codenvy.api.license.server;

import com.codenvy.api.permission.server.SystemDomain;
import com.jayway.restassured.response.Response;

import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.codenvy.api.permission.server.SystemDomain.MANAGE_SYSTEM_ACTION;
import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 *
 * Tests for {@link SystemLicenseServicePermissionsFilter}
 *
 * @author Alexander Andrienko
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class SystemLicenseServicePermissionsFilterTest {
    @SuppressWarnings("unused")
    private static final EnvironmentFilter FILTER = new EnvironmentFilter();

    @SuppressWarnings("unused")
    @InjectMocks
    private SystemLicenseServicePermissionsFilter permissionsFilter;

    @Mock
    private static Subject subject;

    @Mock
    private SystemLicenseService systemLicenseService;

    @Test
    public void shouldNotCheckPermissionsOnLicenseChecking() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/license/system/legality");

        assertEquals(response.getStatusCode(), 204);
        verify(systemLicenseService).isSystemUsageLegal();
        verifyZeroInteractions(subject);
    }

    @Test
    public void shouldNotCheckPermissionsOnLicenseNodeChecking() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/license/system/legality/node?nodeNumber=1");

        assertEquals(response.getStatusCode(), 204);
        verify(systemLicenseService).isMachineNodesUsageLegal(1);
        verifyZeroInteractions(subject);
    }

    @Test
    public void shouldCheckManageSystemPermissionsOnRequestingAnyMethodsFromLicenseServiceExceptLicenseChecking() throws Exception {
        EnvironmentContext.getCurrent().setSubject(subject);
        when(subject.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)).thenReturn(true);
        final GenericResourceMethod GenericResourceMethod = mock(GenericResourceMethod.class);

        final Method[] imMethods = SystemLicenseService.class.getDeclaredMethods();

        int publicMethods = 0;
        for (Method imMethod : imMethods) {
            String methodName = imMethod.getName();
            if (Modifier.isPublic(imMethod.getModifiers()) &&
                !"isSystemUsageLegal".equals(methodName) &&
                !"isMachineNodesUsageLegal".equals(methodName)) {
                when(GenericResourceMethod.getMethod()).thenReturn(imMethod);
                permissionsFilter.filter(GenericResourceMethod, new Object[] {});
                publicMethods++;
            }
        }

        //all methods should be covered with permissions
        verify(subject, times(publicMethods)).checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION);
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }
}
