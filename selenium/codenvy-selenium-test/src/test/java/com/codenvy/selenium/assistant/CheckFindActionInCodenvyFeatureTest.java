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
package com.codenvy.selenium.assistant;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_ACTION;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.FindAction;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckFindActionInCodenvyFeatureTest {

  private static final String FIRST_ACTION_NAME = "config";
  private static final String SECOND_ACTION_NAME = "commands";
  private static final String THIRD_ACTION_NAME = "che";

  private static final String PROJECT_NAME = NameGenerator.generate("project", 5);

  private static final String FIRST_ACTION_NAME_EXPECTED_ARRAY =
      "Update Project Configuration...  Project\n"
          + "Configure Classpath  Project\n"
          + "Edit Debug Configurations... [Alt+Shift+F9]  Run\n"
          + "Import From Codenvy Config...  Project";
  private static final String SECOND_ACTION_NAME_EXPECTED_ARRAY =
      "Commands Palette [Shift+F10]  Run";
  private static final String THIRD_ACTION_NAME_EXPECTED_ARRAY =
      "Branches... [Ctrl+B]  GitCommandGroup\n" + "Checkout Reference...  GitCommandGroup";

  private static final String FIRST_ACTION_NAME_EXPECTED_ARRAY_WITH_FLAG =
      "Configuration \n"
          + "Update Project Configuration...  Project\n"
          + "Configure Classpath  Project\n"
          + "Edit Debug Configurations... [Alt+Shift+F9]  Run\n"
          + "Import From Codenvy Config...  Project\n"
          + "breakpointConfiguration ";
  private static final String SECOND_ACTION_NAME_EXPECTED_ARRAY_WITH_FLAG =
      "Commands \n" + "Commands Palette [Shift+F10]  Run";
  private static final String THIRD_ACTION_NAME_EXPECTED_ARRAY_WITH_FLAG =
      "Branches... [Ctrl+B]  GitCommandGroup\n" + "Checkout Reference...  GitCommandGroup";

  @Inject private TestWorkspace testWorkspace;
  @Inject private FindAction findAction;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Ide ide;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource =
        CheckFindActionInCodenvyFeatureTest.this
            .getClass()
            .getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SPRING);

    ide.open(testWorkspace);
    projectExplorer.waitVisibleItem(PROJECT_NAME);
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(ASSISTANT, FIND_ACTION);
    findAction.setCheckBoxInSelectedPosition();
  }

  @Test(dataProvider = "checkActionsForCodenvyDataWithChkBox")
  public void checkSearchActionsForAllItemsTest(String actionName, String result) {
    checkAction(actionName, result);
  }

  @Test(dataProvider = "checkAllActionsForCodenvyDataWithoutChkBox")
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
    return new Object[][] {
      {FIRST_ACTION_NAME, FIRST_ACTION_NAME_EXPECTED_ARRAY},
      {SECOND_ACTION_NAME, SECOND_ACTION_NAME_EXPECTED_ARRAY},
      {THIRD_ACTION_NAME, THIRD_ACTION_NAME_EXPECTED_ARRAY}
    };
  }

  @DataProvider
  private Object[][] checkActionsForCodenvyDataWithChkBox() {
    return new Object[][] {
      {FIRST_ACTION_NAME, FIRST_ACTION_NAME_EXPECTED_ARRAY_WITH_FLAG},
      {SECOND_ACTION_NAME, SECOND_ACTION_NAME_EXPECTED_ARRAY_WITH_FLAG},
      {THIRD_ACTION_NAME, THIRD_ACTION_NAME_EXPECTED_ARRAY_WITH_FLAG}
    };
  }
}
