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
package com.codenvy.selenium.pageobject.dashboard;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.site.LoginPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Dmytro Nochevnov */
@Singleton
public class DashboardSettings extends CodenvyAdminDashboard {

  private interface Constants {
    String LICENSE_AREA_XPATH = "//textarea[@name='licenseKey']";
    String APPLY_LICENSE_BUTTON_XPATH = "//che-button-primary[@che-button-title='Apply']";
    String REMOVE_LICENSE_BUTTON_XPATH = "//che-button-primary[@che-button-title='Remove']";
    String LICENSE_INFO_XPATH =
        "//div[@che-label-name='License']//span[contains(@class, 'onprem-license-info-row')]";
    String LICENSE_EXPIRATION_XPATH =
        "//div[@che-label-name='Expiration']//span[contains(@class, 'onprem-license-info-row')]";
  }

  @FindBy(xpath = Constants.LICENSE_AREA_XPATH)
  public WebElement licenseArea;

  @FindBy(xpath = Constants.APPLY_LICENSE_BUTTON_XPATH)
  public WebElement applyLicenseButton;

  @FindBy(xpath = Constants.REMOVE_LICENSE_BUTTON_XPATH)
  public WebElement removeLicenseButton;

  @FindBy(xpath = Constants.LICENSE_INFO_XPATH)
  public WebElement licenseInfo;

  @FindBy(xpath = Constants.LICENSE_EXPIRATION_XPATH)
  public WebElement licenseExpiration;

  @Inject
  public DashboardSettings(
      SeleniumWebDriver seleniumWebDriver,
      TestUser testUser,
      TestIdeUrlProvider testIdeUrlProvider,
      TestDashboardUrlProvider testDashboardUrlProvider,
      Entrance entrance,
      LoginPage loginPage) {
    super(
        seleniumWebDriver,
        testUser,
        testIdeUrlProvider,
        testDashboardUrlProvider,
        entrance,
        loginPage);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  @Override
  public void waitPageOpened() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC).until(visibilityOf(licenseArea));
  }

  public void applyLicense(String license) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(licenseArea))
        .sendKeys(license);

    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(applyLicenseButton))
        .click();
  }

  public void removeLicense() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(removeLicenseButton))
        .click();
  }

  public String getLicenseInfo() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(licenseInfo));
    return licenseInfo.getText();
  }

  public String getLicenseExpiration() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(licenseExpiration));
    return licenseExpiration.getText();
  }

  public void waitOnRemoveLicenseButtonAppears() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(removeLicenseButton));
  }

  public void waitOnApplyLicenseButtonAppears() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(applyLicenseButton));
  }
}
