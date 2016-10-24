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
import com.google.inject.name.Names;
import com.google.inject.spi.ConstructorBinding;

import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.environment.server.MachineProcessManager;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Listeners(MockitoTestNGListener.class)
public class MachineTokenInterceptorTest {
    private static final String USER_ID   = "user12";
    private static final String USER_NAME = "username";

    @Mock
    private MachineTokenRegistry  tokenRegistry;
    @Mock
    private WorkspaceImpl         workspaceImpl;
    @Mock
    private WorkspaceFilesCleaner workspaceFilesCleaner;

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
                bind(MachineProcessManager.class).toInstance(mock(MachineProcessManager.class));
                bind(UserManager.class).toInstance(mock(UserManager.class));
                bind(AccountDao.class).toInstance(mock(AccountDao.class));
                bindConstant().annotatedWith(Names.named("workspace.runtime.auto_restore")).to(false);
                bindConstant().annotatedWith(Names.named("workspace.runtime.auto_snapshot")).to(false);
                bind(WorkspaceManager.class);
                bind(SnapshotDao.class).toInstance(mock(SnapshotDao.class));
                bind(WorkspaceFilesCleaner.class).toInstance(workspaceFilesCleaner);

                bind(MachineTokenRegistry.class).toInstance(tokenRegistry);

                //Main injection
                install(new InterceptorModule());

//                 To prevent real methods of manager calling
                bindInterceptor(subclassesOf(WorkspaceManager.class), names("stopWorkspace"), invocation -> null);
                bindInterceptor(subclassesOf(WorkspaceManager.class), names("startWorkspace"),
                                invocation -> workspaceImpl);
            }
        };

        injector = Guice.createInjector(module);
        workspaceManager = injector.getInstance(WorkspaceManager.class);
        EnvironmentContext.setCurrent(new EnvironmentContext() {
            @Override
            public Subject getSubject() {
                return new SubjectImpl(USER_NAME, USER_ID, "token", false);
            }
        });
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
    public void shouldGenerateTokenOnWorkspaceStart() throws Throwable {
        final String workspaceId = "testWs123";
        when(workspaceImpl.getId()).thenReturn(workspaceId);

        workspaceManager.startWorkspace(workspaceId, null, null);

        verify(tokenRegistry).generateToken(eq(USER_ID), eq(workspaceId));
    }
}
