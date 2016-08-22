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
package com.codenvy.api.user.server;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.name.Names;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.inject.lifecycle.InitModule;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.annotation.PostConstruct;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AdminUserCreator}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class AdminUserCreatorTest {

    private static final String NAME     = "admin";
    private static final String PASSWORD = "root";
    private static final String EMAIL    = "admin@rb.com";

    @Mock
    private UserManager userManager;

    private Injector injector;

    @BeforeMethod
    public void setUp() throws Exception {
        injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                install(new InitModule(PostConstruct.class));
                bind(JpaInitializer.class).toInstance(mock(JpaInitializer.class));
                bind(UserManager.class).toInstance(userManager);
                bindConstant().annotatedWith(Names.named("codenvy.admin.name")).to(NAME);
                bindConstant().annotatedWith(Names.named("codenvy.admin.initial_password")).to(PASSWORD);
                bindConstant().annotatedWith(Names.named("codenvy.admin.email")).to(EMAIL);
            }
        });
    }

    @Test
    public void shouldCreateAdminUser() throws Exception {
        when(userManager.getById(NAME)).thenThrow(new NotFoundException("nfex"));
        injector.getInstance(AdminUserCreator.class);

        verify(userManager).getById(NAME);
        verify(userManager).create(new UserImpl(NAME, PASSWORD, NAME, EMAIL, emptyList()), false);
    }

    @Test
    public void shouldNotCreateAdminWhenItAlreadyExists() throws Exception {
        final UserImpl user = new UserImpl(NAME, PASSWORD, NAME, EMAIL, emptyList());
        when(userManager.getById(NAME)).thenReturn(user);
        injector.getInstance(AdminUserCreator.class);

        verify(userManager).getById(NAME);
        verify(userManager, times(0)).create(user, false);
    }
}
