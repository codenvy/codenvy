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
package com.codenvy.selenium.factory;

import com.codenvy.selenium.pageobject.site.LoginAndCreateOnpremAccountPage;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClientFactory;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.user.TestUserNamespaceResolver;
import org.eclipse.che.selenium.pageobject.GitHub;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.Profile;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Mihail Kuznyetsov */
public class AuthenticateAndAcceptFactoryThroughGitHubOAuthTest {
  @Inject private ProjectExplorer projectExplorer;
  @Inject private LoginAndCreateOnpremAccountPage loginPage;
  @Inject private GitHub gitHub;
  @Inject private Profile profile;
  @Inject private Ide ide;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private TestUser testUser;
  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestUserServiceClient testUserServiceClient;
  @Inject private TestApiEndpointUrlProvider apiEndpointUrlProvider;
  @Inject private TestUserNamespaceResolver testUserNamespaceResolver;
  @Inject private TestWorkspaceServiceClientFactory testWorkspaceServiceClientFactory;

  private TestFactory testFactory;

  @BeforeClass
  public void setUp() throws Exception {
    testFactory = testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL).build();
  }

  //  @AfterClass
  // This method removes default user instead of github user.
  // Need to be reworked https://github.com/codenvy/codenvy/issues/2471
  public void tearDown() throws Exception {
    User user = testUserServiceClient.findByEmail(testUser.getEmail());
    TestWorkspaceServiceClient workspaceServiceClient =
        testWorkspaceServiceClientFactory.create(testUser.getEmail(), testUser.getPassword());
    workspaceServiceClient
        .getAll()
        .forEach(
            ws -> {
              try {
                workspaceServiceClient.delete(ws, user.getName());
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });

    testUserServiceClient.remove(testUserServiceClient.findByEmail(testUser.getEmail()).getId());
    testFactory.delete();
  }

  @Test
  public void loginThroughGitHubOAuthAndAcceptFactory() throws Exception {
    testFactory.open(ide.driver());

    loginPage.waitMainElementsOnLoginPage();
    loginPage.clickOnGitIcon();

    gitHub.typeLogin(gitHubUsername);
    gitHub.typePass(gitHubPassword);
    gitHub.clickOnSignInButton();

    if (gitHub.isAuthorizeButtonPresent()) {
      gitHub.clickOnAuthorizeBtn();
    }

    profile.handleProfileOnboardingWithTestData();
    seleniumWebDriver.switchFromDashboardIframeToIde();

    projectExplorer.waitProjectExplorer();
    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClosed("Project Spring imported");
  }
}
