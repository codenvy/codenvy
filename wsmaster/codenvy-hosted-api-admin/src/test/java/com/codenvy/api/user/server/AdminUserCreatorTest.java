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
package com.codenvy.api.user.server;

import static java.util.Collections.emptyList;
import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.persist.jpa.JpaPersistModule;
import java.util.List;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.event.PostUserPersistedEvent;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.eclipse.che.inject.lifecycle.InitModule;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;
import org.eclipse.che.multiuser.api.permission.server.model.impl.SystemPermissionsImpl;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link AdminUserCreator}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class AdminUserCreatorTest {

  private static final String NAME = "admin";
  private static final String PASSWORD = "root";
  private static final String EMAIL = "admin@rb.com";

  @Mock private PermissionsManager permissionsManager;

  private UserImpl user;

  @BeforeMethod
  public void setUp() throws Exception {
    user = new UserImpl("qwe", "qwe", "qwe", "qwe", emptyList());

    final AbstractPermissionsDomain mock = mock(AbstractPermissionsDomain.class);
    doNothing().when(permissionsManager).storePermission(any(SystemPermissionsImpl.class));
    when(permissionsManager.getDomain(nullable(String.class))).thenReturn(cast(mock));
    when(mock.getAllowedActions()).thenReturn(emptyList());
    when(mock.newInstance(nullable(String.class), nullable(String.class), nullable(List.class)))
        .then(
            invocation ->
                new SystemPermissionsImpl(
                    (String) invocation.getArguments()[0],
                    (List<String>) invocation.getArguments()[2]));
  }

  @SuppressWarnings("unchecked")
  private static <R, T extends R> T cast(R qwe) {
    return (T) qwe;
  }

  @Test
  public void shouldCreateAdminUser() throws Exception {
    Injector injector = Guice.createInjector(new OrgModule());
    UserManager userManager = injector.getInstance(UserManager.class);
    when(userManager.getByName(NAME)).thenThrow(new NotFoundException("nfex"));
    when(userManager.create(any(UserImpl.class), anyBoolean())).thenReturn(user);
    injector.getInstance(AdminUserCreator.class);

    verify(userManager).getByName(NAME);
    verify(userManager).create(new UserImpl(NAME, EMAIL, NAME, PASSWORD, emptyList()), false);
    verify(permissionsManager)
        .storePermission(
            argThat(
                (ArgumentMatcher<SystemPermissionsImpl>)
                    argument -> argument.getUserId().equals("qwe")));
  }

  @Test
  public void shouldNotCreateAdminWhenItAlreadyExists() throws Exception {
    final UserImpl user = new UserImpl(NAME, EMAIL, NAME, PASSWORD, emptyList());
    Injector injector = Guice.createInjector(new OrgModule());
    UserManager userManager = injector.getInstance(UserManager.class);
    when(userManager.getByName(NAME)).thenReturn(user);
    injector.getInstance(AdminUserCreator.class);

    verify(userManager).getByName(NAME);
    verify(userManager, times(0)).create(user, false);
  }

  @Test
  public void shouldAddSystemPermissionsInLdapMode() throws Exception {
    Injector injector = Guice.createInjector(new LdapModule());
    UserManager userManager = injector.getInstance(UserManager.class);
    when(userManager.getByName(nullable(String.class))).thenReturn(user);
    when(userManager.create(any(UserImpl.class), anyBoolean())).thenReturn(user);
    AdminUserCreator creator = injector.getInstance(AdminUserCreator.class);
    creator.onEvent(
        new PostUserPersistedEvent(new UserImpl(NAME, EMAIL, NAME, PASSWORD, emptyList())));
    verify(permissionsManager)
        .storePermission(
            argThat(
                (ArgumentMatcher<SystemPermissionsImpl>)
                    argument -> argument.getUserId().equals(NAME)));
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
      bind(SchemaInitializer.class)
          .toInstance(
              new FlywaySchemaInitializer(inMemoryDefault(), "che-schema", "codenvy-schema"));
      bind(DBInitializer.class).asEagerSingleton();
      bindConstant().annotatedWith(Names.named("codenvy.admin.name")).to(NAME);
      bindConstant().annotatedWith(Names.named("codenvy.admin.initial_password")).to(PASSWORD);
      bindConstant().annotatedWith(Names.named("codenvy.admin.email")).to(EMAIL);
      bind(PermissionsManager.class).toInstance(permissionsManager);
      bind(UserManager.class).toInstance(mock(UserManager.class));
    }
  }
}
