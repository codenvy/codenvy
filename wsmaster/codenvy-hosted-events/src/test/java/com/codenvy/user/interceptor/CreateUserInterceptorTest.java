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

import com.codenvy.service.password.RecoveryStorage;
import com.codenvy.user.CreationNotificationSender;

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

import javax.ws.rs.core.Response;
import java.lang.reflect.Method;

import static org.mockito.Matchers.eq;
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
public class CreateUserInterceptorTest {
    @Mock
    private MethodInvocation invocation;
    @Mock
    private RecoveryStorage  recoveryStorage;
    @Mock
    private Response         response;
    @Mock
    private UserService      userService;
    @Mock
    CreationNotificationSender notificationSender;

    @InjectMocks
    private CreateUserInterceptor interceptor;

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
        final Method method = UserService.class.getMethod("create", UserDto.class, String.class, Boolean.class);
        when(invocation.getMethod()).thenReturn(method);

        final Object[] invocationArgs = new Object[method.getParameterCount()];
        invocationArgs[1] = "token123";
        when(invocation.getArguments()).thenReturn(invocationArgs);

        when(invocation.proceed()).thenReturn(response);
        when(response.getEntity()).thenReturn(DtoFactory.newDto(UserDto.class)
                                                        .withEmail(recipient)
                                                        .withName("user123"));

        interceptor.invoke(invocation);

        verify(notificationSender).sendNotification(eq("user123"), eq(recipient), eq(false));
    }

    @Test
    public void shouldSendEmailWhenUserWasCreatedByUserServiceWithDescriptor() throws Throwable {
        // preparing user creator's method
        final Method method = UserService.class.getMethod("create", UserDto.class, String.class, Boolean.class);
        when(invocation.getMethod()).thenReturn(method);

        final Object[] invocationArgs = new Object[method.getParameterCount()];
        when(invocation.getArguments()).thenReturn(invocationArgs);

        when(invocation.proceed()).thenReturn(response);
        when(response.getEntity()).thenReturn(DtoFactory.newDto(UserDto.class)
                                                        .withEmail(recipient)
                                                        .withName("user123"));

        interceptor.invoke(invocation);

        verify(notificationSender).sendNotification(eq("user123"), eq(recipient), eq(true));
    }
}
