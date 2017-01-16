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
package com.codenvy.api.user.server;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.PermissionsManager;
import com.codenvy.api.permission.server.model.impl.SystemPermissionsImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.event.PostUserPersistedEvent;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.inject.lifecycle.InitModule;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.annotation.PostConstruct;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
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

    @Mock
    private PermissionsManager permissionsManager;

    @BeforeMethod
    public void setUp() throws Exception {
        final AbstractPermissionsDomain mock = mock(AbstractPermissionsDomain.class);
        final UserImpl user = new UserImpl("qwe", "qwe", "qwe", "qwe", emptyList());
        doNothing().when(permissionsManager).storePermission(any(SystemPermissionsImpl.class));
        when(permissionsManager.getDomain(anyString())).thenReturn(cast(mock));
        when(mock.getAllowedActions()).thenReturn(emptyList());
        when(mock.newInstance(anyString(), anyString(), anyListOf(String.class))).then(
                invocation -> new SystemPermissionsImpl((String)invocation.getArguments()[0], (List<String>)invocation.getArguments()[2]));
        when(userManager.getByName(anyString())).thenReturn(user);
        when(userManager.create(any(UserImpl.class), anyBoolean())).thenReturn(user);
    }

    @SuppressWarnings("unchecked")
    private static <R, T extends R> T cast(R qwe) {
        return (T)qwe;
    }

    @Test
    public void shouldCreateAdminUser() throws Exception {
        when(userManager.getByName(NAME)).thenThrow(new NotFoundException("nfex"));
        Injector injector = Guice.createInjector(new OrgModule());
        injector.getInstance(AdminUserCreator.class);

        verify(userManager).getByName(NAME);
        verify(userManager).create(new UserImpl(NAME, EMAIL, NAME, PASSWORD, emptyList()), false);
        verify(permissionsManager).storePermission(argThat(new ArgumentMatcher<SystemPermissionsImpl>() {
            @Override
            public boolean matches(Object argument) {
                return ((SystemPermissionsImpl)argument).getUserId().equals("qwe");
            }
        }));
    }

    @Test
    public void shouldNotCreateAdminWhenItAlreadyExists() throws Exception {
        final UserImpl user = new UserImpl(NAME, EMAIL, NAME, PASSWORD, emptyList());
        when(userManager.getById(NAME)).thenReturn(user);
        Injector injector = Guice.createInjector(new OrgModule());
        injector.getInstance(AdminUserCreator.class);

        verify(userManager).getByName(NAME);
        verify(userManager, times(0)).create(user, false);
    }

    @Test
    public void shouldAddSystemPermissionsInLdapMode() throws Exception {
        Injector injector = Guice.createInjector(new LdapModule());
        AdminUserCreator creator = injector.getInstance(AdminUserCreator.class);
        creator.onEvent(new PostUserPersistedEvent(new UserImpl(NAME, EMAIL, NAME, PASSWORD, emptyList())));
        verify(permissionsManager).storePermission(argThat(new ArgumentMatcher<SystemPermissionsImpl>() {
            @Override
            public boolean matches(Object argument) {
                return ((SystemPermissionsImpl)argument).getUserId().equals(NAME);
            }
        }));
    }

    public class OrgModule extends BaseModule {
        @Override
        protected void configure() {
            super.configure();
            bindConstant().annotatedWith(Names.named("sys.auth.handler.default")).to("org");
        }
    }

    public class LdapModule extends BaseModule {
        @Override
        protected void configure() {
            super.configure();
            bindConstant().annotatedWith(Names.named("sys.auth.handler.default")).to("ldap");
        }
    }

    private class BaseModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new InitModule(PostConstruct.class));
            install(new JpaPersistModule("test"));
            bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(inMemoryDefault(), "che-schema", "codenvy-schema"));
            bind(DBInitializer.class).asEagerSingleton();
            bind(UserManager.class).toInstance(userManager);
            bindConstant().annotatedWith(Names.named("codenvy.admin.name")).to(NAME);
            bindConstant().annotatedWith(Names.named("codenvy.admin.initial_password")).to(PASSWORD);
            bindConstant().annotatedWith(Names.named("codenvy.admin.email")).to(EMAIL);
            bind(PermissionsManager.class).toInstance(permissionsManager);
        }
    }

}
