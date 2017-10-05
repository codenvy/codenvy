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
package com.codenvy.selenium.core;

import static org.eclipse.che.selenium.core.utils.PlatformUtils.isMac;

import com.codenvy.selenium.core.client.OnpremTestAuthServiceClient;
import com.codenvy.selenium.core.client.OnpremTestMachineServiceClient;
import com.codenvy.selenium.core.client.OnpremTestOrganizationServiceClient;
import com.codenvy.selenium.core.client.OnpremTestUserServiceClient;
import com.codenvy.selenium.core.provider.OnpremTestApiEndpointUrlProvider;
import com.codenvy.selenium.core.provider.OnpremTestDashboardUrlProvider;
import com.codenvy.selenium.core.provider.OnpremTestIdeUrlProvider;
import com.codenvy.selenium.core.requestfactory.TestAdminHttpJsonRequestFactory;
import com.codenvy.selenium.core.requestfactory.TestDefaultUserHttpJsonRequestFactory;
import com.codenvy.selenium.core.user.OnpremAdminTestUser;
import com.codenvy.selenium.core.user.OnpremTestUserNamespaceResolver;
import com.codenvy.selenium.core.workspace.OnpremTestWorkspaceUrlResolver;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import javax.inject.Named;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.action.GenericActionsFactory;
import org.eclipse.che.selenium.core.action.MacOSActionsFactory;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestMachineServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClientFactory;
import org.eclipse.che.selenium.core.configuration.SeleniumTestConfiguration;
import org.eclipse.che.selenium.core.configuration.TestConfiguration;
import org.eclipse.che.selenium.core.provider.CheTestSvnPasswordProvider;
import org.eclipse.che.selenium.core.provider.CheTestSvnRepo1Provider;
import org.eclipse.che.selenium.core.provider.CheTestSvnRepo2Provider;
import org.eclipse.che.selenium.core.provider.CheTestSvnUsernameProvider;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.core.provider.TestSvnPasswordProvider;
import org.eclipse.che.selenium.core.provider.TestSvnRepo1Provider;
import org.eclipse.che.selenium.core.provider.TestSvnRepo2Provider;
import org.eclipse.che.selenium.core.provider.TestSvnUsernameProvider;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactoryCreator;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.user.TestUserFactory;
import org.eclipse.che.selenium.core.user.TestUserImpl;
import org.eclipse.che.selenium.core.user.TestUserNamespaceResolver;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProviderImpl;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceUrlResolver;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;

/**
 * Guice module per suite.
 *
 * @author Anatolii Bazko
 */
public class OnpremSeleniumSuiteModule extends AbstractModule {

  @Override
  public void configure() {
    TestConfiguration config = new SeleniumTestConfiguration();
    config
        .getMap()
        .forEach((key, value) -> bindConstant().annotatedWith(Names.named(key)).to(value));

    bind(TestSvnPasswordProvider.class).to(CheTestSvnPasswordProvider.class);
    bind(TestSvnUsernameProvider.class).to(CheTestSvnUsernameProvider.class);
    bind(TestSvnRepo1Provider.class).to(CheTestSvnRepo1Provider.class);
    bind(TestSvnRepo2Provider.class).to(CheTestSvnRepo2Provider.class);

    bind(TestWorkspaceUrlResolver.class).to(OnpremTestWorkspaceUrlResolver.class);
    bind(TestUserNamespaceResolver.class).to(OnpremTestUserNamespaceResolver.class);

    bind(TestApiEndpointUrlProvider.class).to(OnpremTestApiEndpointUrlProvider.class);
    bind(TestIdeUrlProvider.class).to(OnpremTestIdeUrlProvider.class);
    bind(TestDashboardUrlProvider.class).to(OnpremTestDashboardUrlProvider.class);

    bind(HttpJsonRequestFactory.class).to(TestUserHttpJsonRequestFactory.class);
    bind(TestUserHttpJsonRequestFactory.class).to(TestDefaultUserHttpJsonRequestFactory.class);
    install(new FactoryModuleBuilder().build(TestUserHttpJsonRequestFactoryCreator.class));

    bind(TestUserServiceClient.class).to(OnpremTestUserServiceClient.class);
    bind(TestAuthServiceClient.class).to(OnpremTestAuthServiceClient.class);
    bind(TestMachineServiceClient.class).to(OnpremTestMachineServiceClient.class);

    bind(TestUser.class).to(TestUserImpl.class).in(Singleton.class);
    bind(TestWorkspaceProvider.class).to(TestWorkspaceProviderImpl.class).asEagerSingleton();

    install(new FactoryModuleBuilder().build(TestWorkspaceServiceClientFactory.class));

    install(
        new FactoryModuleBuilder()
            .implement(TestUser.class, TestUserImpl.class)
            .build(TestUserFactory.class));

    bind(AdminTestUser.class).to(OnpremAdminTestUser.class);
  }

  @Provides
  public TestWorkspace getWorkspace(
      TestWorkspaceProvider testWorkspaceProvider,
      TestUser testUser,
      @Named("workspace.default_memory_gb") int defaultMemoryGb)
      throws Exception {

    TestWorkspace workspace =
        testWorkspaceProvider.createWorkspace(testUser, defaultMemoryGb, WorkspaceTemplate.DEFAULT);
    workspace.await();
    return workspace;
  }

  @Provides
  public ActionsFactory getActionFactory() {
    return isMac() ? new MacOSActionsFactory() : new GenericActionsFactory();
  }

  @Provides
  @Named("admin")
  public OnpremTestOrganizationServiceClient getAdminOrganizationServiceClient(
      TestApiEndpointUrlProvider apiEndpointUrlProvider,
      TestAdminHttpJsonRequestFactory requestFactory) {
    return new OnpremTestOrganizationServiceClient(apiEndpointUrlProvider, requestFactory);
  }
}
