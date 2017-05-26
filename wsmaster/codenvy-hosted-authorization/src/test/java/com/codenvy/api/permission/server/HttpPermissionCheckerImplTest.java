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
package com.codenvy.api.permission.server;

import com.codenvy.api.permission.shared.dto.PermissionsDto;

import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.UriBuilder;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link HttpPermissionCheckerImpl}.
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class HttpPermissionCheckerImplTest {
    private static final String API_ENDPOINT = "http://localhost:8000/api";

    @Mock
    private HttpJsonRequestFactory requestFactory;
    @Mock
    private HttpJsonResponse       response;
    private HttpJsonRequest        request;

    private HttpPermissionCheckerImpl httpPermissionChecker;

    @BeforeMethod
    public void setUp() throws Exception {
        request = mock(HttpJsonRequest.class, (Answer)invocation -> {
            if (invocation.getMethod().getReturnType().isInstance(invocation.getMock())) {
                return invocation.getMock();
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });
        when(request.request()).thenReturn(response);
        when(requestFactory.fromUrl(anyString())).thenReturn(request);

        httpPermissionChecker = new HttpPermissionCheckerImpl(API_ENDPOINT, requestFactory);
    }

    @Test
    public void shouldCheckPermissionsByHttpRequestToPermissionsService() throws Exception {
        when(response.asDto(anyObject())).thenReturn(DtoFactory.newDto(PermissionsDto.class)
                                                               .withUserId("user123")
                                                               .withDomainId("domain123")
                                                               .withInstanceId("instance123")
                                                               .withActions(asList("read", "test")));

        final boolean hasPermission = httpPermissionChecker.hasPermission("user123", "domain123", "instance123", "test");

        assertEquals(hasPermission, true);
        verify(requestFactory).fromUrl(eq(UriBuilder.fromUri(API_ENDPOINT)
                                                    .path(PermissionsService.class)
                                                    .path(PermissionsService.class, "getCurrentUsersPermissions")
                                                    .queryParam("instance", "instance123")
                                                    .build("domain123")
                                                    .toString()));
        verify(request).useGetMethod();
        verify(request).request();
        verifyNoMoreInteractions(request);
    }
}
