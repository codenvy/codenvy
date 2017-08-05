/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.selenium.factory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.pageobject.GitHub;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.Profile;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import com.codenvy.selenium.pageobject.site.LoginAndCreateOnpremAccountPage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Mihail Kuznyetsov
 */
public class AuthenticateAndAcceptFactoryThroughGitHubOAuthTest {
    @Inject
    private ProjectExplorer                 projectExplorer;
    @Inject
    private LoginAndCreateOnpremAccountPage loginPage;
    @Inject
    private GitHub                          gitHub;
    @Inject
    private Profile                         profile;
    @Inject
    private Ide                             ide;
    @Inject
    private NotificationsPopupPanel         notificationsPopupPanel;
    @Inject
    @Named("github.username")
    private String                          gitHubUsername;
    @Inject
    @Named("github.password")
    private String                          gitHubPassword;
    @Inject
    private TestFactoryInitializer          testFactoryInitializer;
    @Inject
    private SeleniumWebDriver               seleniumWebDriver;
    @Inject
    private TestUserServiceClient           testUserServiceClient;
    @Inject
    private TestWorkspaceServiceClient      workspaceServiceClient;

    private TestFactory testFactory;

    @BeforeClass
    public void setUp() throws Exception {
        testFactory = testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL).build();
    }


    @AfterClass
    public void tearDown() throws Exception {
        String authToken = ide.driver().manage().getCookieNamed("session-access-key").getValue();

        User user = testUserServiceClient.getUser(authToken);

        workspaceServiceClient.getAll(authToken)
                              .forEach(ws -> {
                                  try {
                                      workspaceServiceClient.delete(ws, user.getName(), authToken);
                                  } catch (Exception e) {
                                      throw new RuntimeException(e);
                                  }
                              });

        testUserServiceClient.deleteByEmail(user.getEmail());
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
