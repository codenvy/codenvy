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
package com.codenvy.machine.authentication.server.interceptor;

import com.codenvy.machine.authentication.server.MachineTokenRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.spi.ConstructorBinding;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;


import java.lang.reflect.Method;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Listeners(MockitoTestNGListener.class)
public class MachineTokenInterceptorTest {

    @Mock
    private MachineTokenRegistry tokenRegistry;
    @Mock
    private WorkspaceImpl        workspaceImpl;

    private WorkspaceManager workspaceManager;

    private Injector injector;

    @BeforeMethod
    public void setUp() throws Throwable {

        Module module = new AbstractModule() {
            public void configure() {
                //Bind manager and his dep-s. To bind interceptor, guice must create intercepted class by himself.
                bind(WorkspaceDao.class).toInstance(mock(WorkspaceDao.class));
                bind(WorkspaceRuntimes.class).toInstance(mock(WorkspaceRuntimes.class));
                bind(EventService.class).toInstance(mock(EventService.class));
                bind(MachineManager.class).toInstance(mock(MachineManager.class));
                bind(UserManager.class).toInstance(mock(UserManager.class));
                bind(WorkspaceManager.class);

                bind(MachineTokenRegistry.class).toInstance(tokenRegistry);

                //Main injection
                install(new InterceptorModule());

                // To prevent real methods of manager calling
                bindInterceptor(subclassesOf(WorkspaceManager.class), names("stopWorkspace"), invocation -> null);
                bindInterceptor(subclassesOf(WorkspaceManager.class), names("startWorkspace"),
                                invocation -> workspaceImpl);
                bindInterceptor(subclassesOf(WorkspaceManager.class), names("recoverWorkspace"),
                                invocation -> workspaceImpl);
            }
        };

        injector = Guice.createInjector(module);
        workspaceManager = injector.getInstance(WorkspaceManager.class);
    }

    @Test
    public void checkAllInterceptedMethodsArePresent() throws Throwable {
        ConstructorBinding<?> interceptedBinding
                = (ConstructorBinding<?>)injector.getBinding(WorkspaceManager.class);

        for (Method method : interceptedBinding.getMethodInterceptors().keySet()) {
            workspaceManager.getClass().getMethod(method.getName(), method.getParameterTypes());
        }
    }

    @Test
    public void shouldRemoveTokenOnWorkspaceStop() throws Throwable {
        final String workspaceId = "testWs123";

        workspaceManager.stopWorkspace(workspaceId);

        verify(tokenRegistry).removeTokens(eq(workspaceId));
    }


    @Test
    public void shouldGenerateTokenOnWorkspaceStart() throws Throwable {
        final String workspaceId = "testWs123";
        when(workspaceImpl.getId()).thenReturn(workspaceId);

        workspaceManager.startWorkspace(workspaceId, null, null);

        verify(tokenRegistry).generateToken(anyString(), eq(workspaceId));
    }

    @Test
    public void shouldGenerateTokenOnWorkspaceRecover() throws Throwable {
        final String workspaceId = "testWs123";
        when(workspaceImpl.getId()).thenReturn(workspaceId);

        workspaceManager.recoverWorkspace(workspaceId, null, null);

        verify(tokenRegistry).generateToken(anyString(), eq(workspaceId));
    }
}
