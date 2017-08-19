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
package com.codenvy.selenium.site;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;

import com.codenvy.selenium.pageobject.site.LoginAndCreateOnpremAccountPage;
import com.codenvy.selenium.pageobject.site.RecoverPasswordPage;
import com.codenvy.selenium.util.MailReceiverUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class CreateAccountAndResetPasswordOnOnpremTest {

  /**
   * Represents regexp patterns for matching defined link (for example: confirmation for workspace
   * creation, reset password, add member into organizations and etc.)
   */
  private static final String CONFIRM_WORKSPACE_CREATION_PATTERN =
      "(http|https)://%s/.*bearertoken=\\w*\\&?";

  private static final String RESET_PASSWORD_PATTERN =
      "(http|https)://%s/site/.*setup-password.*id=\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}";

  @Inject private Ide ide;

  @Inject
  @Named("google.user")
  private String testUser;

  @Inject
  @Named("google.password")
  private String testPassword;

  @Inject private TestIdeUrlProvider ideUrlProvider;

  @Inject
  @Named("sys.host")
  private String productHost;

  @Inject private Dashboard dashboard;
  @Inject private LoginAndCreateOnpremAccountPage loginPage;
  @Inject private RecoverPasswordPage recoverPasswordPage;
  @Inject private TestAuthServiceClient authServiceClient;
  @Inject private TestUserServiceClient testUserServiceClient;

  private MailReceiverUtils mailReceiverUtils;
  private String userName;

  @BeforeClass
  public void setUp() throws Exception {
    userName = testUser.split("@")[0];

    try {
      testUserServiceClient.deleteByEmail(testUser);
    } catch (NotFoundException ignored) {
    }

    mailReceiverUtils = new MailReceiverUtils(testUser, testPassword);
  }

  @AfterClass
  public void tearDown() throws Exception {
    mailReceiverUtils.cleanMailBox();

    try {
      testUserServiceClient.deleteByEmail(testUser);
    } catch (NotFoundException ignored) {
    }
  }

  /**
   * Make sure mail server set up before test properly in data/codenvy.env file:
   * CODENVY_MAIL_HOST=smtp.gmail.com CODENVY_MAIL_HOST_PORT=465 CODENVY_MAIL_USE_SSL=true
   * CODENVY_MAIL_TRANSPORT_PROTOCOL=smtp CODENVY_MAIL_SMTP_AUTH=true
   * CODENVY_MAIL_SMTP_SOCKETFACTORY_CLASS=javax.net.ssl.SSLSocketFactory
   * CODENVY_MAIL_SMTP_SOCKETFACTORY_FALLBACK=false CODENVY_MAIL_SMTP_SOCKETFACTORY_PORT=465
   * CODENVY_MAIL_SMTP_AUTH_USERNAME= CODENVY_MAIL_SMTP_AUTH_PASSWORD=
   */
  @Test
  public void createAccountTest() throws Exception {
    String expectedMessageInMail = "To gain access to Codenvy, please verify your email";
    ide.driver().get(ideUrlProvider.get().toString());

    loginPage.waitLoginPage();
    loginPage.clickCreateNewAccount();
    loginPage.enterEmail(testUser);
    loginPage.enterUserName(userName);
    loginPage.clickSignUp();

    ide.driver()
        .get(
            mailReceiverUtils.waitAndGetLink(
                ELEMENT_TIMEOUT_SEC,
                EXPECTED_MESS_IN_CONSOLE_SEC,
                expectedMessageInMail,
                format(CONFIRM_WORKSPACE_CREATION_PATTERN, productHost)));

    dashboard.waitDeveloperFaceImg();
  }

  @Test(priority = 1)
  public void checkRestorePassword() throws Exception {
    String expectedMessageInMail = "Click to reset your Codenvy password - this link is valid for";
    String newPassword = NameGenerator.generate(testPassword, 2);
    authServiceClient.logout(ide.driver().manage().getCookieNamed("session-access-key").getValue());
    ide.driver().manage().deleteAllCookies();
    ide.driver().get(ideUrlProvider.get().toString());

    loginPage.waitLoginPage();
    loginPage.clickOnResetPasswordLnk();
    recoverPasswordPage.sendMessageResetPassword(testUser);

    ide.driver()
        .get(
            mailReceiverUtils.waitAndGetLink(
                ELEMENT_TIMEOUT_SEC,
                EXPECTED_MESS_IN_CONSOLE_SEC,
                expectedMessageInMail,
                format(RESET_PASSWORD_PATTERN, productHost)));

    recoverPasswordPage.setNewPasswordAndSave(newPassword);
    loginPage.loginToDashboard(userName, newPassword);
    dashboard.waitDeveloperFaceImg();
  }
}
