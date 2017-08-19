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
package com.codenvy.user.interceptor;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.codenvy.service.password.RecoveryStorage;
import com.codenvy.user.CreationNotificationSender;
import java.lang.reflect.Method;
import javax.ws.rs.core.Response;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link UserCreatorInterceptor}
 *
 * @author Sergii Leschenko
 * @author Anatoliy Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CreateUserInterceptorTest {
  @Mock private MethodInvocation invocation;
  @Mock private RecoveryStorage recoveryStorage;
  @Mock private Response response;
  @Mock private UserService userService;
  @Mock CreationNotificationSender notificationSender;

  @InjectMocks private CreateUserInterceptor interceptor;

  private String recipient = "test@user.com";

  @Test(expectedExceptions = ConflictException.class)
  public void shouldNotSendEmailIfInvocationThrowsException() throws Throwable {
    when(invocation.proceed()).thenThrow(new ConflictException("conflict"));

    interceptor.invoke(invocation);

    verifyZeroInteractions(notificationSender);
  }

  @Test
  public void shouldSendEmailWhenUserWasCreatedByUserServiceWithToken() throws Throwable {
    // preparing user creator's method
    final Method method =
        UserService.class.getMethod("create", UserDto.class, String.class, Boolean.class);
    when(invocation.getMethod()).thenReturn(method);

    final Object[] invocationArgs = new Object[method.getParameterCount()];
    invocationArgs[1] = "token123";
    when(invocation.getArguments()).thenReturn(invocationArgs);

    when(invocation.proceed()).thenReturn(response);
    when(response.getEntity())
        .thenReturn(DtoFactory.newDto(UserDto.class).withEmail(recipient).withName("user123"));

    interceptor.invoke(invocation);

    verify(notificationSender).sendNotification(eq("user123"), eq(recipient), eq(false));
  }

  @Test
  public void shouldSendEmailWhenUserWasCreatedByUserServiceWithDescriptor() throws Throwable {
    // preparing user creator's method
    final Method method =
        UserService.class.getMethod("create", UserDto.class, String.class, Boolean.class);
    when(invocation.getMethod()).thenReturn(method);

    final Object[] invocationArgs = new Object[method.getParameterCount()];
    when(invocation.getArguments()).thenReturn(invocationArgs);

    when(invocation.proceed()).thenReturn(response);
    when(response.getEntity())
        .thenReturn(DtoFactory.newDto(UserDto.class).withEmail(recipient).withName("user123"));

    interceptor.invoke(invocation);

    verify(notificationSender).sendNotification(eq("user123"), eq(recipient), eq(true));
  }
}
