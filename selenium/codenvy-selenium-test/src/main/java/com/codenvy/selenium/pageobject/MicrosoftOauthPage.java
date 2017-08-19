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
package com.codenvy.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class MicrosoftOauthPage {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public MicrosoftOauthPage(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private static final String LOGIN_PAGE = "https://login.live.com/";

  private interface Locators {
    String EMAIL_OR_PHONE_FIELD_ID = "input[type='email']";
    String PASSWORD_FIELD_ID = "//input[@name='passwd' and @type='password']";
    String SIGN_IN = "input[value='Sign in']";
    String ACCEPT_APP_BTN_CSS = "button#accept-button";
  }

  @FindBy(css = Locators.EMAIL_OR_PHONE_FIELD_ID)
  WebElement emailField;

  @FindBy(xpath = Locators.PASSWORD_FIELD_ID)
  WebElement passwordField;

  @FindBy(css = Locators.SIGN_IN)
  WebElement signInBtn;

  @FindBy(css = Locators.ACCEPT_APP_BTN_CSS)
  WebElement acceptBtn;

  /**
   * type the user email or login
   *
   * @param userEmail email/login to Microsoft account, defined by user
   */
  public void typeEmailOrPhone(String userEmail) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(emailField))
        .sendKeys(userEmail);
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(emailField))
        .sendKeys(Keys.ENTER);
  }

  /**
   * type the user password or email
   *
   * @param userPassword password to Microsoft account, defined by user
   */
  public void typeUserPassword(String userPassword) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.urlContains(LOGIN_PAGE));
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(passwordField))
        .sendKeys(userPassword);
  }

  /** click in Sign in button */
  public void clickOnSignInBtn() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(signInBtn))
        .click();
  }

  public void clickOnAcceptBtn() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(acceptBtn))
        .click();
  }

  /**
   * performs login to MicrosoftAccount
   *
   * @param password
   * @param login
   */
  public void loginToMicrosoftAccount(String login, String password) {
    typeEmailOrPhone(login);
    typeUserPassword(password);
    clickOnSignInBtn();
  }
}
