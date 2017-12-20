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
package com.codenvy.selenium.vsts;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.COMMIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.RESET;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.PULL;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.PUSH;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.BLANK;

import com.codenvy.selenium.core.client.OnpremTestOAuthServiceClient;
import com.codenvy.selenium.pageobject.site.LoginVSTS;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Date;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckWorkWithVSTSProviderTest {
  private static final String GIT_URL_TO_PROJECT =
      "https://iedexmain1.visualstudio.com/_git/MyFirstProject";
  private static final String PROJECT_NAME = NameGenerator.generate("project", 5);
  private static final String OAUTH_PROVIDER = "microsoft";
  private static final String PATH_TO_README = PROJECT_NAME + "/README.md";
  private static final String README_FILE = "README.md";
  private static final String PUSH_MSG = "Pushed to origin";
  private static final String PULL_MSG = "Pulled from " + GIT_URL_TO_PROJECT;

  private static final Long TIME = new Date().getTime();

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser user;
  @Inject private ImportProjectFromLocation importWidget;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Wizard wizard;
  @Inject private AskDialog authorizationDialog;
  @Inject private LoginVSTS loginVSTS;
  @Inject private CodenvyEditor editor;
  @Inject private Git git;
  @Inject private Events events;
  @Inject private Consoles consoles;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private OnpremTestOAuthServiceClient oAuthApiUtils;

  @Inject
  @Named("vsts.user")
  private String vstsLogin;

  @Inject
  @Named("vsts.password")
  private String vstsPassword;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(ws);

    if (oAuthApiUtils.isOAuthProviderValid(OAUTH_PROVIDER, user.getAuthToken())) {
      oAuthApiUtils.invalidateOAuthProvider(OAUTH_PROVIDER, user.getAuthToken());
    }
  }

  @Test
  public void cloneProjectFromVSTS() {
    String ideWin = seleniumWebDriver.getWindowHandle();

    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);
    importWidget.waitMainForm();
    importWidget.selectGitSourceItem();
    loader.waitOnClosed();
    importWidget.typeURi(GIT_URL_TO_PROJECT);
    importWidget.typeProjectName(PROJECT_NAME);
    importWidget.clickImportBtnWithoutWait();
    authorizationDialog.waitFormToOpen();
    authorizationDialog.clickOkBtn();
    authorizationDialog.waitFormToClose();

    seleniumWebDriver.switchToNoneCurrentWindow(ideWin);

    loginVSTS.clickOnOldSignInPageLink();
    loginVSTS.waitLoginPage();
    if (stateVSTSLoginPage()) {
      loginVSTS.enterLogin(vstsLogin);
      loginVSTS.enterPassword(vstsPassword);
      loginVSTS.waitSignInPage();
      performSignInPage();
    } else {
      loginVSTS.enterLogin(vstsLogin);
      try {
        loginVSTS.clickOnContinueBtn();
      } catch (org.openqa.selenium.TimeoutException ex) {
        loginVSTS.waitSignInPage();
      }
      loginVSTS.waitSignInPage();
      performSignInPage();
    }
    loginVSTS.clickOnAcceptBtn();

    seleniumWebDriver.switchTo().window(ideWin);

    loader.waitOnClosed();
    wizard.selectTypeProject(BLANK);
    loader.waitOnClosed();
    wizard.clickSaveButton();
    loader.waitOnClosed();
    wizard.waitCreateProjectWizardFormIsClosed();

    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitItem(PATH_TO_README);
  }

  @Test(priority = 1)
  public void commitAndPushVSTS() {
    projectExplorer.openItemByPath(PATH_TO_README);
    loader.waitOnClosed();
    editor.waitActiveEditor();
    editor.deleteAllContent();
    editor.setCursorToDefinedLineAndChar(1, 1);

    editor.typeTextIntoEditor(TIME.toString());
    editor.waitTextIntoEditor(TIME.toString());
    loader.waitOnClosed();
    editor.closeFileByNameWithSaving(README_FILE);
    editor.waitWhileFileIsClosed(README_FILE);

    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(GIT, COMMIT);
    git.waitAndRunCommit(TIME.toString());
    loader.waitOnClosed();

    menu.runCommand(GIT, REMOTES_TOP, PUSH);
    loader.waitOnClosed();
    git.waitPushFormToOpen();
    git.clickPush();
    git.waitPushFormToClose();
    consoles.waitProcessInProcessConsoleTree("Git push");
    events.doubleClickProjectEventsTab();
    events.waitExpectedMessage(PUSH_MSG);
  }

  @Test(priority = 2)
  public void resetAndPullVSTS() {
    projectExplorer.openItemByPath(PATH_TO_README);
    loader.waitOnClosed();
    editor.closeAllTabs();

    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(GIT, RESET);
    git.waitResetWindowOpen();
    git.waitCommitIsPresentResetWindow(TIME.toString());
    git.selectCommitResetWindow(2);
    git.selectHardReset();
    git.clickResetBtn();
    git.waitResetWindowClose();
    loader.waitOnClosed();

    menu.runCommand(GIT, REMOTES_TOP, PULL);
    git.waitPullFormToOpen();
    git.clickPull();
    git.waitPullFormToClose();
    loader.waitOnClosed();
    consoles.waitProcessInProcessConsoleTree("Git pull");
    events.clickProjectEventsTab();
    events.clickProjectEventsTab();
    events.waitExpectedMessage(PULL_MSG);

    projectExplorer.openItemByPath(PATH_TO_README);
    loader.waitOnClosed();
    editor.waitTextIntoEditor(TIME.toString());
  }

  /**
   * auxulary method for defining the Microsoft login page (the page may have login and password
   * fields on single page, sometimes password field on one page, login field on other page)
   *
   * @return true if we on the page with login and password page and false if we on page with only
   *     password field
   */
  private boolean stateVSTSLoginPage() {

    try {
      loginVSTS.waitLoginField(2);
      loginVSTS.waitPasswordField(2);
      return true;
    } catch (TimeoutException ex) {
      return false;
    }
  }

  /** enter password and click on sign in button on Microsoft VSTS Page */
  private void performSignInPage() {
    loginVSTS.enterPassword(vstsPassword);
    loginVSTS.clickOnSignInBtn();
  }
}
