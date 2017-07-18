/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.api.license.server.filter;

import com.codenvy.api.license.server.SystemLicenseService;
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
        @Override
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }
}
