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
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.Profile;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Mihail Kuznyetsov */
public class AuthenticateAndAcceptFactoryThroughCredentialsTest {
  @Inject private Ide ide;
  @Inject private DefaultTestUser defaultUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private LoginAndCreateOnpremAccountPage loginPage;
  @Inject private Profile profile;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private SeleniumWebDriver seleniumWebDriver;

  private TestFactory testFactory;

  @BeforeClass
  public void setUp() throws Exception {
    testFactory = testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL).build();
  }

  @AfterClass
  public void tearDown() throws Exception {
    testFactory.delete();
  }

  @Test
  public void loginThroughEnteringCredentialsAndAcceptFactory() throws Exception {
    testFactory.open(ide.driver());
    loginPage.waitMainElementsOnLoginPage();

    loginPage.loginToDashboard(defaultUser.getName(), defaultUser.getPassword());

    profile.handleProfileOnboardingWithTestData();
    seleniumWebDriver.switchFromDashboardIframeToIde();

    projectExplorer.waitProjectExplorer();
    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClosed("Project Spring imported");
  }
}
