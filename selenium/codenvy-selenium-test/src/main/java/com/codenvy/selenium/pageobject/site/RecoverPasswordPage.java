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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class RecoverPasswordPage {

  private final SeleniumWebDriver seleniumWebDriver;
  WebDriverWait redrawWait;

  @Inject
  public RecoverPasswordPage(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
    redrawWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  private static final class Locators {
    private static final String MAIN_PAGE = "//*[contains(text(),'Forgot your password?')]";
    private static final String EMAIL_FIELD = "emailid";
    private static final String RESET_PASS_BTN = "input[value='Reset password']";
    private static final String PAGE_ABOUT_SEND_MESS =
        "//*[contains(text(),'sent a reset password link to your email.')]";
    private static final String SET_NEW_PASSWORD_FIELD_CSS =
        "form#resetPasswordForm input[placeholder='New Password']";
    private static final String REPEAT_NEW_PASSWORD_FIELD_CSS =
        "form#resetPasswordForm input[placeholder='Repeat New Password']";
    private static final String SAVE_NEW_PASSWORD_BUTTON =
        "form#resetPasswordForm input[Value='Save']";
  }

  @FindBy(xpath = Locators.MAIN_PAGE)
  WebElement mainPageContainer;

  @FindBy(id = Locators.EMAIL_FIELD)
  WebElement emailField;

  @FindBy(css = Locators.RESET_PASS_BTN)
  WebElement resetButton;

  @FindBy(xpath = Locators.PAGE_ABOUT_SEND_MESS)
  WebElement pageWithsendMessageInfo;

  @FindBy(css = Locators.SET_NEW_PASSWORD_FIELD_CSS)
  WebElement setNewPasswordField;

  @FindBy(css = Locators.REPEAT_NEW_PASSWORD_FIELD_CSS)
  WebElement repeatNewPasswordField;

  @FindBy(css = Locators.SAVE_NEW_PASSWORD_BUTTON)
  WebElement saveNewPasswordField;

  public void sendMessageResetPassword(String userEmai) {
    redrawWait.until(ExpectedConditions.visibilityOf(mainPageContainer));
    redrawWait.until(ExpectedConditions.visibilityOf(emailField)).sendKeys(userEmai);
    resetButton.click();
    redrawWait.until(ExpectedConditions.visibilityOf(pageWithsendMessageInfo));
  }

  public void setNewPasswordAndSave(String newPassword) {
    redrawWait.until(ExpectedConditions.visibilityOf(setNewPasswordField)).sendKeys(newPassword);
    repeatNewPasswordField.sendKeys(newPassword);
    saveNewPasswordField.click();
  }
}
