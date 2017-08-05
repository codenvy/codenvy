/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.selenium.ssh;

import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestSshServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestWorkspaceConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsExplorer;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * @author Musienko Maxim
 */
public class LoginBySshKeyTest {
    private static final String  NAME_COMMAND_FOR_CREATION_SSH_DIRECTORY = "createSshDirectory";
    private static final String  TITLE_OF_SSH_KEY                        = "checkSSHConnection";
    private static final String  PATH_TO_SSH_KEY_FOLDER                  = "~/.ssh/test.ssh";
    private static final String  NAME_COMMAND_FOR_CREATION_SSH_KEY       = "createSshKey";
    private static final String  NAME_COMMAND_CONNECTION_BY_SSH_KEY      = "connectBySsh";
    private static final String  PROJECT_NAME                            = NameGenerator.generate("project", 6);
    private static final Pattern GET_SSH_COMMAND_PATTERN                 = compile("ssh.* -p \\d.*-i");
    private static final String  CHECKED_FILE_NAME                       = "checkedFile" + new Random().nextInt(9999) + ".txt";

    @Inject
    private Ide           ide;
    @Inject
    private TestWorkspace ws1;
    @Inject
    private TestWorkspace ws2;

    @Inject
    private DefaultTestUser            user;
    @Inject
    private CommandsExplorer           commandsExplorer;
    @Inject
    private ProjectExplorer            projectExplorer;
    @Inject
    private Preferences                preferences;
    @Inject
    private NotificationsPopupPanel    notifications;
    @Inject
    private Loader                     loader;
    @Inject
    private Consoles                   consoles;
    @Inject
    private ToastLoader                toastLoader;
    @Inject
    private Menu                       menu;
    @Inject
    private TestCommandServiceClient   testCommandServiceClient;
    @Inject
    private TestSshServiceClient       testSshServiceClient;
    @Inject
    private TestWorkspaceServiceClient workspaceServiceClient;
    @Inject
    private TestProjectServiceClient   testProjectServiceClient;
    @Inject
    private SeleniumWebDriver          seleniumWebDriver;

    @BeforeClass
    public void setUp() throws Exception {
        URL resource = LoginBySshKeyTest.this.getClass().getResource("/projects/default-spring-project");
        testProjectServiceClient.importProject(ws1.getId(), Paths.get(resource.toURI()), PROJECT_NAME,
                                               ProjectTemplates.MAVEN_SPRING);
        testProjectServiceClient.createFileInProject(ws1.getId(), PROJECT_NAME, CHECKED_FILE_NAME, "text");

        ide.open(ws1);
        String commandForPreparingSshDirectory = "mkdir -p /home/user/.ssh";
        testCommandServiceClient.createCommand(commandForPreparingSshDirectory, NAME_COMMAND_FOR_CREATION_SSH_DIRECTORY,
                                               TestCommandsConstants.CUSTOM,
                                               ws1.getId());
        projectExplorer.waitProjectExplorer();
        notifications.waitExpectedMessageOnProgressPanelAndClosed(TestWorkspaceConstants.RUNNING_WORKSPACE_MESS);
    }

    @AfterClass
    public void tearDown() throws Exception {
        testSshServiceClient.deleteMachineKeyByName(TITLE_OF_SSH_KEY);
    }

    @Test
    public void loginBySshKeyTest() throws Exception {
        menu.runCommand(TestMenuCommandsConstants.Profile.PROFILE_MENU, TestMenuCommandsConstants.Profile.PREFERENCES);
        preferences.waitPreferencesForm();
        preferences.waitDropDownHeaderMenu(Preferences.DropDownListsHeaders.KEYS_SETTINGS);
        preferences.selectDroppedMenuByName(Preferences.DropDownSshKeysMenu.MACHINE);
        preferences.clickOnGenerateKeyButton();
        preferences.generateNewSshKey(TITLE_OF_SSH_KEY);
        preferences.clickOnCloseBtn();

        String sshPrivateKeyFromFirstMachine = testSshServiceClient.getPrivateKeyByName(TITLE_OF_SSH_KEY);
        workspaceServiceClient.stop(ws1.getName(), user.getName(), false);
        seleniumWebDriver.navigate().refresh();
        notifications.waitExpectedMessageOnProgressPanelAndClosed(TestWorkspaceConstants.RUNNING_WORKSPACE_MESS);
        notifications.waitPopUpPanelsIsClosed();
        consoles.clickOnOpenSshTerminalBtn();

        String commandForConnection = getCommandFromSshConsole();
        ide.open(ws2);
        projectExplorer.waitProjectExplorer();
        notifications.waitExpectedMessageOnProgressPanelAndClosed(TestWorkspaceConstants.RUNNING_WORKSPACE_MESS);
        String commandForCreatingSshKey =
                "mkdir -p ~/.ssh && echo -e '" + sshPrivateKeyFromFirstMachine.replaceAll("\n", "\\\\n") + "' > " + PATH_TO_SSH_KEY_FOLDER;
        testCommandServiceClient.createCommand(commandForCreatingSshKey, NAME_COMMAND_FOR_CREATION_SSH_KEY, TestCommandsConstants.CUSTOM,
                                               ws2.getId());
        consoles.clickOnOpenSshTerminalBtn();
        testCommandServiceClient.createCommand(
                "chmod 0700 " + PATH_TO_SSH_KEY_FOLDER + " && " + commandForConnection + " \" cd /projects/" + PROJECT_NAME +
                " && ls \" ",
                NAME_COMMAND_CONNECTION_BY_SSH_KEY, TestCommandsConstants.CUSTOM,
                ws2.getId());

        //This actions for redrawing the just add commands on Toolbar widget after fixing CHE-1357 the refresh browser block should be removed
        seleniumWebDriver.navigate().refresh();
        notifications.waitExpectedMessageOnProgressPanelAndClosed(TestWorkspaceConstants.RUNNING_WORKSPACE_MESS);
        new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.invisibilityOfElementLocated(By.id("gwt-debug-glass-panel")));
        toastLoader.waitAppeareanceAndClosing();
        launchCustomeCommandFromIDEToolBar(NAME_COMMAND_FOR_CREATION_SSH_KEY);
        launchCustomeCommandFromIDEToolBar(NAME_COMMAND_CONNECTION_BY_SSH_KEY);
        consoles.waitExpectedTextIntoConsole(CHECKED_FILE_NAME);
    }

    /**
     * Match and get command for ssh connection from the SSH console and add command for strict host key
     *
     * @return command for connection with ssh
     */
    private String getCommandFromSshConsole() {
        String strictHostKeyCheckCommand = " -o StrictHostKeyChecking=no";
        String sshConsoleContent = consoles.getVisibleTextFromCommandConsole();

        Matcher matcher = GET_SSH_COMMAND_PATTERN.matcher(sshConsoleContent);
        if (matcher.find()) {
            return (matcher.group().replace("[", "")) + " " + PATH_TO_SSH_KEY_FOLDER + strictHostKeyCheckCommand;
        } else
            throw new RuntimeException("Cannot find the instruction about SSH connection in SSH command console. " +
                                       "Make sure that information is present and instructions are not changed");
    }

    /**
     * Launch defined command from toolbar dropdawn menu
     *
     * @param command
     *         the name of launched command
     */
    private void launchCustomeCommandFromIDEToolBar(String command) {
        commandsExplorer.openCommandsExplorer();
        commandsExplorer.waitCommandExplorerIsOpened();
        commandsExplorer.runCommandByName(command);

        loader.waitOnClosed();

        projectExplorer.clickOnProjectExplorerTabInTheLeftPanel();
        projectExplorer.waitProjectExplorer();
    }
}
