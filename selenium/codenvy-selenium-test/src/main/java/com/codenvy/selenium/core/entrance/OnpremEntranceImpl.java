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
package com.codenvy.selenium.core.entrance;

import com.codenvy.selenium.pageobject.site.LoginAndCreateOnpremAccountPage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProfileServiceClient;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.core.user.TestUser;

@Singleton
public class OnpremEntranceImpl implements Entrance {

  private final SeleniumWebDriver seleniumWebDriver;
  private final LoginAndCreateOnpremAccountPage loginAndCreateOnpremAccountPage;
  private final TestProfileServiceClient testProfileServiceClient;

  @Inject
  public OnpremEntranceImpl(
      SeleniumWebDriver seleniumWebDriver,
      LoginAndCreateOnpremAccountPage loginAndCreateOnpremAccountPage,
      TestProfileServiceClient testProfileServiceClient) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loginAndCreateOnpremAccountPage = loginAndCreateOnpremAccountPage;
    this.testProfileServiceClient = testProfileServiceClient;
  }

  /**
   * Login to product by UI.
   *
   * @param user
   */
  @Override
  public void login(TestUser user) {
    if (loginAndCreateOnpremAccountPage.isOpened()) {
      loginAndCreateOnpremAccountPage.waitLoginPage();
      loginAndCreateOnpremAccountPage.loginToDashboard(user.getName(), user.getPassword());
    }
  }
}
