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
package com.codenvy.selenium.miscellaneous;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;

import com.codenvy.selenium.pageobject.MicrosoftOauthPage;
import com.codenvy.selenium.pageobject.site.LoginAndCreateOnpremAccountPage;
import com.google.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class LoginWithMicrosoftOAuthTest {

  @Inject private Ide ide;
  @Inject private LoginAndCreateOnpremAccountPage loginPage;
  @Inject private MicrosoftOauthPage microsoftOauthPage;
  @Inject private Dashboard dashboard;
  @Inject private SeleniumWebDriver seleniumWebDriver;

  @Inject
  @Named("vsts.user")
  private String vstsAccountEmail;

  @Inject
  @Named("vsts.password")
  private String vstsAccountPassword;

  @Test
  public void shouldLoginWithMicrosoftOAuth() {
    loginPage.open();

    loginPage.clickOnMicrosoftOauthBtn();
    microsoftOauthPage.loginToMicrosoftAccount(vstsAccountEmail, vstsAccountPassword);

    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            ExpectedConditions.urlContains(
                "https://app.vssps.visualstudio.com/oauth2/authorize?client_id"));

    microsoftOauthPage.clickOnAcceptBtn();
    dashboard.waitDashboardToolbarTitle();
  }
}
