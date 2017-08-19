/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.auth.sso.client;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.codenvy.auth.sso.server.SsoService;
import com.codenvy.auth.sso.shared.dto.SubjectDto;
import javax.ws.rs.core.UriBuilder;
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

/**
 * Tests for {@link HttpSsoServerClient}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class HttpSsoServerClientTest {
  private static final String API_ENDPOINT = "http://localhost:8000/api";
  public static final String CLIENT_URL = "http://test.client.com";

  @Mock private HttpJsonRequestFactory requestFactory;
  @Mock private HttpJsonResponse response;
  private HttpJsonRequest request;

  HttpSsoServerClient ssoClient;

  @BeforeMethod
  public void setUp() throws Exception {
    request =
        mock(
            HttpJsonRequest.class,
            (Answer)
                invocation -> {
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

    final Subject subject = ssoClient.getSubject("token123", CLIENT_URL);

    assertEquals(subject.getUserId(), subject.getUserId());
    assertEquals(subject.getUserName(), subject.getUserName());
    assertEquals(subject.getToken(), subject.getToken());
    assertEquals(subject.isTemporary(), subject.isTemporary());
    verify(requestFactory)
        .fromUrl(
            eq(
                UriBuilder.fromUri(API_ENDPOINT)
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
  public void shouldReturnsNullWhenSomeExceptionOccurs() throws Exception {
    final SubjectDto subjectDto = createUserDto();
    when(response.asDto(anyObject())).thenReturn(subjectDto);
    when(request.request()).thenThrow(new NotFoundException("not found"));

    final Subject subject = ssoClient.getSubject("token123", CLIENT_URL);

    assertNull(subject);
    verify(requestFactory)
        .fromUrl(
            eq(
                UriBuilder.fromUri(API_ENDPOINT)
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
  public void shouldUnregisterClient() throws Exception {
    ssoClient.unregisterClient("token123", CLIENT_URL);

    verify(requestFactory)
        .fromUrl(
            eq(
                UriBuilder.fromUri(API_ENDPOINT)
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
        .withToken("token123");
  }
}
