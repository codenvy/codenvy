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
package com.codenvy.auth.sso.client;

import com.codenvy.auth.sso.server.SsoService;
import com.codenvy.auth.sso.shared.dto.SubjectDto;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.UriBuilder;
import java.util.Collections;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link HttpSsoServerClient}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class HttpSsoServerClientTest {
    private static final String API_ENDPOINT = "http://localhost:8000/api";
    public static final  String CLIENT_URL   = "http://test.client.com";

    @Mock
    private HttpJsonRequestFactory requestFactory;
    @Mock
    private HttpJsonResponse       response;
    private HttpJsonRequest        request;

    HttpSsoServerClient ssoClient;

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

        ssoClient = new HttpSsoServerClient(API_ENDPOINT, requestFactory);
    }

    @Test
    public void shouldRequestUser() throws Exception {
        final SubjectDto subjectDto = createUserDto();
        when(response.asDto(anyObject())).thenReturn(subjectDto);

        final Subject subject = ssoClient.getSubject("token123", CLIENT_URL, null, null);

        assertEquals(subject.getUserId(), subject.getUserId());
        assertEquals(subject.getUserName(), subject.getUserName());
        assertEquals(subject.getToken(), subject.getToken());
        assertEquals(subject.isTemporary(), subject.isTemporary());
        assertTrue(subject.isMemberOf("superUser"));
        assertFalse(subject.isMemberOf("user"));
        verify(requestFactory).fromUrl(eq(UriBuilder.fromUri(API_ENDPOINT)
                                                    .path(SsoService.class)
                                                    .path(SsoService.class, "getCurrentPrincipal")
                                                    .build("token123")
                                                    .toString()));
        verify(request).addQueryParam(eq("clienturl"), eq(CLIENT_URL));
        verify(request).useGetMethod();
        verify(request).request();
        verifyNoMoreInteractions(request);
    }

    @Test
    public void shouldRequestUserWithWorkspaceAndAccountIds() throws Exception {
        final SubjectDto subjectDto = createUserDto();
        when(response.asDto(anyObject())).thenReturn(subjectDto);

        final Subject subject = ssoClient.getSubject("token123", CLIENT_URL, "workspace123", "account123");

        assertEquals(subject.getUserId(), subject.getUserId());
        assertEquals(subject.getUserName(), subject.getUserName());
        assertEquals(subject.getToken(), subject.getToken());
        assertEquals(subject.isTemporary(), subject.isTemporary());
        assertTrue(subject.isMemberOf("superUser"));
        assertFalse(subject.isMemberOf("user"));
        verify(requestFactory).fromUrl(eq(UriBuilder.fromUri(API_ENDPOINT)
                                                    .path(SsoService.class)
                                                    .path(SsoService.class, "getCurrentPrincipal")
                                                    .build("token123")
                                                    .toString()));
        verify(request).addQueryParam(eq("workspaceid"), eq("workspace123"));
        verify(request).addQueryParam(eq("accountid"), eq("account123"));
        verify(request).addQueryParam(eq("clienturl"), eq(CLIENT_URL));
        verify(request).useGetMethod();
        verify(request).request();
        verifyNoMoreInteractions(request);
    }

    @Test
    public void shouldRequestUserWithWorkspaceId() throws Exception {
        final SubjectDto subjectDto = createUserDto();
        when(response.asDto(anyObject())).thenReturn(subjectDto);

        final Subject subject = ssoClient.getSubject("token123", CLIENT_URL, "workspace123", null);

        assertEquals(subject.getUserId(), subject.getUserId());
        assertEquals(subject.getUserName(), subject.getUserName());
        assertEquals(subject.getToken(), subject.getToken());
        assertEquals(subject.isTemporary(), subject.isTemporary());
        assertTrue(subject.isMemberOf("superUser"));
        assertFalse(subject.isMemberOf("user"));
        verify(requestFactory).fromUrl(eq(UriBuilder.fromUri(API_ENDPOINT)
                                                    .path(SsoService.class)
                                                    .path(SsoService.class, "getCurrentPrincipal")
                                                    .build("token123")
                                                    .toString()));
        verify(request).addQueryParam(eq("workspaceid"), eq("workspace123"));
        verify(request, never()).addQueryParam(eq("accountid"), anyString());
        verify(request).addQueryParam(eq("clienturl"), eq(CLIENT_URL));
        verify(request).useGetMethod();
        verify(request).request();
        verifyNoMoreInteractions(request);
    }

    @Test
    public void shouldRequestUserWithAccountId() throws Exception {
        final SubjectDto subjectDto = createUserDto();
        when(response.asDto(anyObject())).thenReturn(subjectDto);

        final Subject subject = ssoClient.getSubject("token123", CLIENT_URL, null, "account123");

        assertEquals(subject.getUserId(), subject.getUserId());
        assertEquals(subject.getUserName(), subject.getUserName());
        assertEquals(subject.getToken(), subject.getToken());
        assertEquals(subject.isTemporary(), subject.isTemporary());
        assertTrue(subject.isMemberOf("superUser"));
        assertFalse(subject.isMemberOf("user"));
        verify(requestFactory).fromUrl(eq(UriBuilder.fromUri(API_ENDPOINT)
                                                    .path(SsoService.class)
                                                    .path(SsoService.class, "getCurrentPrincipal")
                                                    .build("token123")
                                                    .toString()));
        verify(request, never()).addQueryParam(eq("workspaceid"), anyString());
        verify(request).addQueryParam(eq("clienturl"), eq(CLIENT_URL));
        verify(request).addQueryParam(eq("accountid"), eq("account123"));
        verify(request).useGetMethod();
        verify(request).request();
        verifyNoMoreInteractions(request);
    }

    @Test
    public void shouldReturnsNullWhenSomeExceptionOccurs() throws Exception {
        final SubjectDto subjectDto = createUserDto();
        when(response.asDto(anyObject())).thenReturn(subjectDto);
        when(request.request()).thenThrow(new NotFoundException("not found"));

        final Subject subject = ssoClient.getSubject("token123", CLIENT_URL, "workspace123", "account123");

        assertNull(subject);
        verify(requestFactory).fromUrl(eq(UriBuilder.fromUri(API_ENDPOINT)
                                                    .path(SsoService.class)
                                                    .path(SsoService.class, "getCurrentPrincipal")
                                                    .build("token123")
                                                    .toString()));
        verify(request).addQueryParam(eq("workspaceid"), eq("workspace123"));
        verify(request).addQueryParam(eq("accountid"), eq("account123"));
        verify(request).addQueryParam(eq("clienturl"), eq(CLIENT_URL));
        verify(request).useGetMethod();
        verify(request).request();
        verifyNoMoreInteractions(request);
    }

    @Test
    public void shouldUnregisterClient() throws Exception {
        ssoClient.unregisterClient("token123", CLIENT_URL);

        verify(requestFactory).fromUrl(eq(UriBuilder.fromUri(API_ENDPOINT)
                                                    .path(SsoService.class)
                                                    .path(SsoService.class, "unregisterToken")
                                                    .build("token123")
                                                    .toString()));
        verify(request).addQueryParam(eq("clienturl"), eq(CLIENT_URL));
        verify(request).useDeleteMethod();
        verify(request).request();
        verifyNoMoreInteractions(request);
    }

    private SubjectDto createUserDto() {
        return DtoFactory.newDto(SubjectDto.class)
                         .withId("user123")
                         .withName("userok")
                         .withToken("token123")
                         .withTemporary(false)
                         .withRoles(Collections.singletonList("superUser"));
    }
}
