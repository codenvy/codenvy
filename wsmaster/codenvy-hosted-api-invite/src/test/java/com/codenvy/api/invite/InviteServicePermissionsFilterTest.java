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
package com.codenvy.api.invite;

import com.codenvy.shared.invite.dto.InviteDto;

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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codenvy.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;
import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test for {@link InviteServicePermissionsFilter}
 *
 * @author Sergii Leshchenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class InviteServicePermissionsFilterTest {
    @SuppressWarnings("unused")
    private static final CheJsonProvider    jsonProvider = new CheJsonProvider(Collections.emptySet());
    @SuppressWarnings("unused")
    private static final ApiExceptionMapper MAPPER       = new ApiExceptionMapper();
    @SuppressWarnings("unused")
    private static final EnvironmentFilter  FILTER       = new EnvironmentFilter();

    private static final String USER_ID = "user123";

    @Mock
    private static Subject subject;

    @Mock
    private InviteService service;

    @InjectMocks
    private InviteServicePermissionsFilter permissionsFilter;

    @BeforeMethod
    public void setUp() throws Exception {
        when(subject.getUserId()).thenReturn(USER_ID);

        when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(true);
    }

    @Test
    public void shouldTestThatAllPublicMethodsAreCoveredByPermissionsFilter() throws Exception {
        //given
        final List<String> collect = Stream.of(InviteService.class.getDeclaredMethods())
                                           .filter(method -> Modifier.isPublic(method.getModifiers()))
                                           .map(Method::getName)
                                           .collect(Collectors.toList());

        //then
        assertEquals(collect.size(), 3);
        assertTrue(collect.contains(InviteServicePermissionsFilter.GET_INVITES_METHOD));
        assertTrue(collect.contains(InviteServicePermissionsFilter.INVITE_METHOD));
        assertTrue(collect.contains(InviteServicePermissionsFilter.REMOVE_METHOD));
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "User is not authorized to perform specified operation")
    public void shouldThrowForbiddenExceptionWhenRequestedUnknownMethod() throws Exception {
        final GenericResourceMethod mock = mock(GenericResourceMethod.class);
        Method injectLinks = InviteService.class.getMethod("getServiceDescriptor");
        when(mock.getMethod()).thenReturn(injectLinks);

        permissionsFilter.filter(mock, new Object[] {});
    }

    @Test
    public void shouldCheckPermissionsOnInviteStoring() throws Exception {
        InviteDto toStore = DtoFactory.newDto(InviteDto.class)
                                      .withEmail("userok@test.com")
                                      .withDomainId("test")
                                      .withInstanceId("test123")
                                      .withActions(Collections.singletonList("read"));

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType("application/json")
               .body(toStore)
               .when()
               .expect().statusCode(204)
               .post(SECURE_PATH + "/invite");

        verify(service).invite(toStore);
        verify(subject).hasPermission("test", "test123", SET_PERMISSIONS);
    }

    @Test
    public void shouldRespond403WhenUserDoesNotHaveCorrespondingPermissionOnInviteStoring() throws Exception {
        when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .contentType("application/json")
               .body(DtoFactory.newDto(InviteDto.class)
                               .withEmail("userok@test.com")
                               .withDomainId("test")
                               .withInstanceId("test123")
                               .withActions(Collections.singletonList("read")))
               .when()
               .expect().statusCode(403)
               .post(SECURE_PATH + "/invite");

        verify(service, never()).invite(any());
        verify(subject).hasPermission("test", "test123", SET_PERMISSIONS);
    }

    @Test
    public void shouldCheckPermissionsOnGettingInvites() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .expect().statusCode(204)
               .get(SECURE_PATH + "/invite/test?instance=test123&skipCount=1&maxItems=2");

        verify(service).getInvites("test", "test123", 1, 2);
        verify(subject).hasPermission("test", "test123", SET_PERMISSIONS);
    }

    @Test
    public void shouldRespond403WhenUserDoesNotHaveCorrespondingPermissionOnInvitesGetting() throws Exception {
        when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .expect().statusCode(403)
               .get(SECURE_PATH + "/invite/test?instance=test123&skipCount=1&maxItems=2");

        verify(service, never()).getInvites(anyString(), anyString(), anyLong(), anyInt());
        verify(subject).hasPermission("test", "test123", SET_PERMISSIONS);
    }

    @Test
    public void shouldCheckPermissionsOnInviteRemoving() throws Exception {
        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .expect().statusCode(204)
               .delete(SECURE_PATH + "/invite/test?instance=test123&email=user@test.com");

        verify(service).remove("test", "test123", "user@test.com");
        verify(subject).hasPermission("test", "test123", SET_PERMISSIONS);
    }

    @Test
    public void shouldRespond403WhenUserDoesNotHaveCorrespondingPermissionOnInvitesRemoving() throws Exception {
        when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);

        given().auth()
               .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
               .when()
               .expect().statusCode(403)
               .delete(SECURE_PATH + "/invite/test?instance=test123&email=user@test.com");

        verify(service, never()).remove(anyString(), anyString(), anyString());
        verify(subject).hasPermission("test", "test123", SET_PERMISSIONS);
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        @Override
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }
}
