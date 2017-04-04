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
package com.codenvy.user.interceptor;

import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.user.CreationNotificationSender;

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

import java.lang.reflect.Method;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link UserCreatorInterceptor}
 *
 * @author Sergii Leschenko
 * @author Anatoliy Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class UserCreatorInterceptorTest {
    @Mock
    private User             user;
    @Mock
    private MethodInvocation invocation;
    @Mock
    private UserCreator      userCreator;
    @Mock
    private UserManager      userManager;
    @Mock
    CreationNotificationSender notificationSender;

    @InjectMocks
    private UserCreatorInterceptor interceptor;

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
        final Method method = UserCreator.class.getMethod("createUser", String.class, String.class, String.class, String.class);
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
        final Method method = UserCreator.class.getMethod("createUser", String.class, String.class, String.class, String.class);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.proceed()).thenReturn(user);

        when(user.getEmail()).thenReturn(recipient);
        when(user.getName()).thenReturn("user123");

        when(invocation.getArguments()).thenReturn(new Object[] {recipient});

        interceptor.invoke(invocation);

        verify(notificationSender, never()).sendNotification(any(), anyString(), anyBoolean());
    }
}
