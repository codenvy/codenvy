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
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Andrey Chizhikov */
@Singleton
public class LoginVSTS {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public LoginVSTS(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String LOGIN = "//input[@type='email']";
    String PASSWORD = "//input[@type='password']";
    String LOGIN_PAGE = "login_panel";
    String OLD_AUTH_PAGE = "uxOptOutLink";
    String SIGN_IN_BTN = "//input[@type='submit']";
    String ACCEPT = "accept-button";
    String CONTINUE = "cred_continue_button";
    String SIGN_IN_PAGE = "idA_PWD_ForgotPassword";
  }

  @FindBy(id = Locators.LOGIN_PAGE)
  WebElement loginPage;

  @FindBy(xpath = Locators.LOGIN)
  WebElement loginInput;

  @FindBy(xpath = Locators.PASSWORD)
  WebElement passwordInput;

  @FindBy(xpath = Locators.SIGN_IN_BTN)
  WebElement signInBtn;

  @FindBy(id = Locators.ACCEPT)
  WebElement acceptBtn;

  @FindBy(id = Locators.CONTINUE)
  WebElement continueBtn;

  @FindBy(id = Locators.SIGN_IN_PAGE)
  WebElement signInPage;

  @FindBy(id = Locators.OLD_AUTH_PAGE)
  WebElement oldAuthPageLink;

  /** Wait 'Visual Studio' authorization window is opened */
  public void waitLoginPage() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC).until(visibilityOf(loginPage));
  }

  /** Wait 'Sign in' window is opened */
  public void waitSignInPage() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC).until(visibilityOf(signInPage));
  }

  /**
   * Enter login in the 'Visual Studio' authorization window
   *
   * @param login login for microsoft account
   */
  public void enterLogin(String login) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(loginInput))
        .sendKeys(login);
  }

  /**
   * Enter password in the 'Visual Studio' authorisation window
   *
   * @param password password for microsoft account
   */
  public void enterPassword(String password) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(passwordInput))
        .sendKeys(password);
  }

  /** Click on 'Sign in' button in the 'Visual Studio' authorization window */
  public void clickOnSignInBtn() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(signInBtn))
        .click();
  }

  /** Click on 'Accept' button in the 'Visual Studio' authorization window */
  public void clickOnAcceptBtn() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(acceptBtn))
        .click();
  }

  /** Click on 'Continue' button in the 'Visual Studio' authorization window */
  public void clickOnContinueBtn() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(continueBtn))
        .click();
  }

  /**
   * wait login field on Microsoft Sign in page
   *
   * @param timeoutDefinedByUser
   */
  public void waitLoginField(int timeoutDefinedByUser) {
    new WebDriverWait(seleniumWebDriver, timeoutDefinedByUser).until(visibilityOf(passwordInput));
  }

  /**
   * wait password field on Microsoft Sign in page
   *
   * @param timeoutDefinedByUser
   */
  public void waitPasswordField(int timeoutDefinedByUser) {
    new WebDriverWait(seleniumWebDriver, timeoutDefinedByUser).until(visibilityOf(passwordInput));
  }

  /** Click on 'Go back to the old one' link */
  public void clickOnOldAuthPageLink() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(oldAuthPageLink))
        .click();
  }
}
