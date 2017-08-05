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
package com.codenvy.selenium.assistant;

import com.google.inject.Inject;

import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.FindAction;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;

/**
 * @author Andrey Chizhikov
 */
public class CheckFindActionInCodenvyFeatureTest {

    private static final String FIRST_ACTION_NAME  = "con";
    private static final String SECOND_ACTION_NAME = "comm";
    private static final String THIRD_ACTION_NAME  = "che";

    private static final String FIRST_ACTION_NAME_EXPECTED_ARRAY = "Configuration...  Project\n" +
                                                                   "Configure Classpath  Project\n" +
                                                                   "Content Assist  Assistant\n" +
                                                                   "Convert To Project  Project\n" +
                                                                   "Edit Debug Configurations... [Alt+Shift+F9]  Run\n" +
                                                                   "Import From Codenvy Config...  Project";

    private static final String SECOND_ACTION_NAME_EXPECTED_ARRAY = "Commands Palette [Shift+F10]  Run\n" +
                                                                    "Commit ...  GitCommandGroup\n" +
                                                                    "Commit...  SvnFileCommandGroup\n" +
                                                                    "SvnCredentialsCommandGroup  Subversion";

    private static final String THIRD_ACTION_NAME_EXPECTED_ARRAY = "Branches...  GitCommandGroup\n" +
                                                                   "Checkout Reference...  GitCommandGroup";

    private static final String FIRST_ACTION_NAME_EXPECTED_ARRAY_WITH_FLAG = "Update Project Configuration...  Project\n" +
                                                                             "Configure Classpath  Project\n" +
                                                                             "Content Assist  Assistant\n" +
                                                                             "Convert To Project  Project\n" +
                                                                             "Edit Debug Configurations... [Alt+Shift+F9]  Run\n" +
                                                                             "Import From Codenvy Config...  Project\n" +
                                                                             "consolesTreeContextMenu \n" +
                                                                             "debugGroupContextMenu \n" +
                                                                             "editorTabContextMenu \n" +
                                                                             "mainContextMenu \n" +
                                                                             "projectExplorerContextMenu \n" +
                                                                             "runGroupContextMenu ";

    private static final String SECOND_ACTION_NAME_EXPECTED_ARRAY_WITH_FLAG = "Commands \n" +
                                                                              "Commands Palette [Shift+F10]  Run\n" +
                                                                              "Commit ...  GitCommandGroup\n" +
                                                                              "Commit...  SvnFileCommandGroup\n" +
                                                                              "Execute default command of Debug goal [Alt+D] \n" +
                                                                              "Execute default command of Run goal [Alt+R] \n" +
                                                                              "GitCommandGroup \n" +
                                                                              "SvnAddCommandGroup \n" +
                                                                              "SvnCredentialsCommandGroup  Subversion\n" +
                                                                              "SvnFileCommandGroup \n" +
                                                                              "SvnMiscellaneousCommandGroup \n" +
                                                                              "SvnRemoteCommandGroup \n" +
                                                                              "SvnRepositoryCommandGroup ";
    private static final String THIRD_ACTION_NAME_EXPECTED_ARRAY_WITH_FLAG  = "Branches...  GitCommandGroup\n" +
                                                                              "Checkout Reference...  GitCommandGroup";

    private static final String PROJECT_NAME = CheckFindActionInCodenvyFeatureTest.class.getSimpleName();

    @Inject
    private TestWorkspace            testWorkspace;
    @Inject
    private FindAction               findAction;
    @Inject
    private ProjectExplorer          projectExplorer;
    @Inject
    private Menu                     menu;
    @Inject
    private Ide                      ide;
    @Inject
    private TestProjectServiceClient testProjectServiceClient;

    @BeforeClass
    public void setUp() throws Exception {
        URL resource = CheckFindActionInCodenvyFeatureTest.this.getClass().getResource("/projects/default-spring-project");
        testProjectServiceClient.importProject(testWorkspace.getId(), Paths.get(resource.toURI()),
                                               PROJECT_NAME,
                                               ProjectTemplates.MAVEN_SPRING);

        ide.open(testWorkspace);
        projectExplorer.waitVisibleItem(PROJECT_NAME);
        projectExplorer.selectItem(PROJECT_NAME);
        menu.runCommand(TestMenuCommandsConstants.Assistant.ASSISTANT, TestMenuCommandsConstants.Assistant.FIND_ACTION);
        findAction.setCheckBoxInSelectedPosition();
    }


    @Test(dataProvider = "checkActionsForCodenvyDataWithChkBox")
    public void checkSearchActionsForAllItemsTest(String actionName, String result) {
        checkAction(actionName, result);
    }

    @Test(dataProvider = "checkAllActionsForCodenvyDataWithoutChkBox", priority = 1)
    public void checkSearchActionsForMenuItemsTest(String actionName, String result) {
        findAction.setCheckBoxInNotSelectedPosition();
        checkAction(actionName, result);
    }


    private void checkAction(String actionName, String expectedResult) {
        findAction.typeTextIntoFindActionForm(actionName);
        findAction.waitTextInFormFindAction(expectedResult);
        findAction.clearTextBoxActionForm();
    }

    @DataProvider
    private Object[][] checkAllActionsForCodenvyDataWithoutChkBox() {
        return new Object[][]{
                {FIRST_ACTION_NAME, FIRST_ACTION_NAME_EXPECTED_ARRAY},
                {SECOND_ACTION_NAME, SECOND_ACTION_NAME_EXPECTED_ARRAY},
                {THIRD_ACTION_NAME, THIRD_ACTION_NAME_EXPECTED_ARRAY}
        };
    }

    @DataProvider
    private Object[][] checkActionsForCodenvyDataWithChkBox() {
        return new Object[][]{
                {FIRST_ACTION_NAME, FIRST_ACTION_NAME_EXPECTED_ARRAY_WITH_FLAG},
                {SECOND_ACTION_NAME, SECOND_ACTION_NAME_EXPECTED_ARRAY_WITH_FLAG},
                {THIRD_ACTION_NAME, THIRD_ACTION_NAME_EXPECTED_ARRAY_WITH_FLAG}
        };
    }
}
