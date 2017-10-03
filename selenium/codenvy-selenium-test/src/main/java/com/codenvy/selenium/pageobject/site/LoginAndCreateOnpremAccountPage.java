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
package com.codenvy.selenium.pageobject.site;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.site.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author */
@Singleton
public class LoginAndCreateOnpremAccountPage implements LoginPage {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final TestDashboardUrlProvider dashboardUrlProvider;
  private WebDriverWait redrawTimeout;
  private WebDriverWait loadPageTimeout;

  @Inject
  public LoginAndCreateOnpremAccountPage(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      TestDashboardUrlProvider dashboardUrlProvider) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.dashboardUrlProvider = dashboardUrlProvider;
    PageFactory.initElements(seleniumWebDriver, this);
    redrawTimeout = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    loadPageTimeout = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
  }

  private interface Locators {

    String LOGIN_ON_TENANT_BTN = "//input[@value='Login']";
    String GOOGLE_OAUTH_BUTTON = "a.oauth-button.google";
    String GITHUB_OAUTH_BUTTON = "a.oauth-button.github";
    String MICROSOFT_OAUTH_BUTTON_CSS = "a.microsoft";
    String RESET_YOUR_PASSWORD = "Reset Your Password";
    String CREATE_A_NEW_ACCOUNT = "Create a New Account";
    String UNAVAILABLE_RESOURCE_WINDOWS = "//div[@class='light']";
    String SING_UP_BUTTON_XPATH = "//input[@value='Sign Up']";
    String EMAIL_INPUT_XPATH = "//input[@name='email']";
    String USER_NAME_INPUT_XPATH = "//input[@name='username']";
    String PASSWORD_INPUT_XPATH = "//input[@name='password']";
  }

  @FindBy(xpath = Locators.USER_NAME_INPUT_XPATH)
  private WebElement usernameInput;

  @FindBy(xpath = Locators.PASSWORD_INPUT_XPATH)
  private WebElement passwordInput;

  @FindBy(xpath = Locators.LOGIN_ON_TENANT_BTN)
  private WebElement loginButton;

  @FindBy(css = Locators.GOOGLE_OAUTH_BUTTON)
  private WebElement googleOauthBtn;

  @FindBy(css = Locators.GITHUB_OAUTH_BUTTON)
  private WebElement gitHubOauthBtn;

  @FindBy(linkText = Locators.RESET_YOUR_PASSWORD)
  private WebElement resetPaswordLink;

  @FindBy(linkText = Locators.CREATE_A_NEW_ACCOUNT)
  private WebElement createAnewAccountLink;

  @FindBy(xpath = Locators.UNAVAILABLE_RESOURCE_WINDOWS)
  private WebElement unavailableResourceWindow;

  @FindBy(css = Locators.MICROSOFT_OAUTH_BUTTON_CSS)
  private WebElement microsoftOauthBtn;

  @FindBy(xpath = Locators.SING_UP_BUTTON_XPATH)
  private WebElement signUp;

  @FindBy(xpath = Locators.EMAIL_INPUT_XPATH)
  private WebElement emailInput;

  @Override
  public void login(String username, String password) {
    waitMainElementsOnLoginPage();
    usernameInput.clear();
    usernameInput.sendKeys(username);
    passwordInput.clear();
    passwordInput.sendKeys(password);
    clickOnLogInBtnAndGo();
  }

  @Override
  public boolean isOpened() {
    try {
      waitLoginPage();
      return true;
    } catch (TimeoutException ex) {
      ex.printStackTrace();
      return false;
    }
  }

  /** wait main elements on login page */
  public void waitLoginPage() {
    waitMainElementsOnLoginPage();
  }

  /** wait appearance main buttons and fields on login page */
  public void waitMainElementsOnLoginPage() {
    loadPageTimeout.until(
        ExpectedConditions.visibilityOfAllElements(Arrays.asList(usernameInput, passwordInput)));
  }

  /** wait main elements on login page */
  public void waitLoginPageClosed() {
    loadPageTimeout.until(
        ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.USER_NAME_INPUT_XPATH)));
    redrawTimeout.until(
        ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.PASSWORD_INPUT_XPATH)));
  }

  /**
   * login on specified workspace from login page
   *
   * @param userName
   * @param password
   */
  public void loginToDashboard(String userName, String password) {
    waitMainElementsOnLoginPage();
    usernameInput.clear();
    usernameInput.sendKeys(userName);
    passwordInput.clear();
    passwordInput.sendKeys(password);
    clickOnLogInBtnAndGo();
  }

  /** click on button Login and wait closing of the IDE loader */
  public void clickOnLogInBtnAndGo() {
    loginButton.click();
    loader.waitOnClosed();
  }

  /** click on 'Create a New Account' link on login page */
  public void clickCreateNewAccount() {
    loadPageTimeout.until(ExpectedConditions.visibilityOf(createAnewAccountLink)).click();
  }

  /** click on 'Sign Up' button */
  public void clickSignUp() {
    redrawTimeout.until(ExpectedConditions.elementToBeClickable(signUp)).click();
  }

  /** click on google oAuth button on the codenvy login page */
  public void clickGoogLeIcon() {
    loadPageTimeout.until(ExpectedConditions.visibilityOf(googleOauthBtn)).click();
  }

  /** click on git oAuth button on the codenvy login page */
  public void clickOnGitIcon() {
    loadPageTimeout.until(ExpectedConditions.visibilityOf(gitHubOauthBtn)).click();
  }

  public void clickOnMicrosoftOauthBtn() {
    loadPageTimeout.until(ExpectedConditions.visibilityOf(microsoftOauthBtn)).click();
  }

  /**
   * enter users email
   *
   * @param email email of user
   */
  public void enterEmail(String email) {
    redrawTimeout.until(ExpectedConditions.elementToBeClickable(emailInput)).sendKeys(email);
    redrawTimeout.until(ExpectedConditions.textToBePresentInElementValue(emailInput, email));
  }

  /**
   * enter users name
   *
   * @param userName name of user
   */
  public void enterUserName(String userName) {
    redrawTimeout.until(ExpectedConditions.elementToBeClickable(usernameInput)).sendKeys(userName);
    redrawTimeout.until(ExpectedConditions.textToBePresentInElementValue(usernameInput, userName));
  }

  public void clickOnResetPasswordLnk() {
    redrawTimeout.until(ExpectedConditions.elementToBeClickable(resetPaswordLink)).click();
  }

  public void open() {
    seleniumWebDriver.get(dashboardUrlProvider.get().toString());
  }
}
