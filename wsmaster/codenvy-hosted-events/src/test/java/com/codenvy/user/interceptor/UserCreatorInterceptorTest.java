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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.user.CreationNotificationSender;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
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
public class UserCreatorInterceptorTest {
  @Mock private User user;
  @Mock private MethodInvocation invocation;
  @Mock private UserCreator userCreator;
  @Mock private UserManager userManager;
  @Mock CreationNotificationSender notificationSender;

  @InjectMocks private UserCreatorInterceptor interceptor;

  private String recipient = "test@user.com";

  @Test(expectedExceptions = ConflictException.class)
  public void shouldNotSendEmailIfInvocationThrowsException() throws Throwable {
    when(invocation.proceed()).thenThrow(new ConflictException("conflict"));
    when(invocation.getArguments()).thenReturn(new Object[] {null, "token123"});

    interceptor.invoke(invocation);

    verifyZeroInteractions(notificationSender);
  }

  @Test
  public void shouldSendEmailWhenUserWasCreated() throws Throwable {
    // preparing user creator's method
    final Method method =
        UserCreator.class.getMethod(
            "createUser", String.class, String.class, String.class, String.class);
    when(invocation.getMethod()).thenReturn(method);
    when(invocation.proceed()).thenReturn(user);

    when(user.getEmail()).thenReturn(recipient);
    when(user.getName()).thenReturn("user123");
    when(userManager.getByEmail(recipient)).thenThrow(new NotFoundException(""));

    when(invocation.getArguments()).thenReturn(new Object[] {recipient});

    interceptor.invoke(invocation);

    verify(notificationSender).sendNotification(eq("user123"), eq(recipient), eq(false));
  }

  @Test
  public void shouldNotSendEmailWhenUserCreatorProvideExistingUser() throws Throwable {
    // preparing user creator's method
    final Method method =
        UserCreator.class.getMethod(
            "createUser", String.class, String.class, String.class, String.class);
    when(invocation.getMethod()).thenReturn(method);
    when(invocation.proceed()).thenReturn(user);

    when(user.getEmail()).thenReturn(recipient);
    when(user.getName()).thenReturn("user123");

    when(invocation.getArguments()).thenReturn(new Object[] {recipient});

    interceptor.invoke(invocation);

    verify(notificationSender, never()).sendNotification(any(), anyString(), anyBoolean());
  }
}
