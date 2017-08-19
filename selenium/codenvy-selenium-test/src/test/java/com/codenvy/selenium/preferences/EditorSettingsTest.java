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
package com.codenvy.selenium.preferences;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProfileServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.InjectTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class EditorSettingsTest {

  @InjectTestUser("editorsettingstestuser@some.email")
  private TestUser testUser;

  @InjectTestWorkspace(user = "editorsettingstestuser@some.email")
  private TestWorkspace testWorkspace;

  @Inject private Ide ide;
  @Inject private CodenvyEditor editor;
  @Inject private Preferences preferences;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private Menu menu;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Wizard wizard;
  @Inject private TestProfileServiceClient testProfileServiceClient;

  private static final String PROJECT_NAME = NameGenerator.generate("Prj", 6);
  private static final String PATH_FOR_EXPAND =
      PROJECT_NAME + "/src/main/java/org.eclipse.che.examples";
  private static final String[] HEADER_SETTINGS = {
    "Tabs", "Language tools", "Typing", "White spaces", "Rulers"
  };
  private static final String PATH =
      PROJECT_NAME + "/src/main/java/org/eclipse/che/examples/GreetingController.java";

  private static final String EXPECTED_TEXT_WITH_CHECKING_SETTINGS =
      "()\n" + "{}\n" + "<>\n" + "[]\n" + "\"\"\n" + "''\n" + "/*\n" + " * \n" + " */\n" + "/**\n"
          + " * \n" + " */";

  private static final String EXPECTED_TEXT_WITH_UNCHECKING_SETTINGS =
      "(\n" + "{\n" + "<\n" + "[\n" + "\"\n" + "'\n" + "/*\n" + "\n" + "/**\n";

  @BeforeClass
  public void setUp() throws Exception {
    Map<String, String> attributes = new HashMap<>();
    attributes.put("firstName", "Selenium");
    attributes.put("employerName", "Codenvy");
    attributes.put("jobtitle", "Ops");
    attributes.put("countryName", "Ukraine");
    testProfileServiceClient.setAttributes(attributes, testUser.getAuthToken());
  }

  @Test
  public void editorSettingsTest() throws Exception {
    ide.open(testWorkspace);

    URL resource = EditorSettingsTest.class.getResource("editor");
    List<String> expectedEditorSettingsList =
        Files.readAllLines(Paths.get(resource.toURI()), Charset.forName("UTF-8"));

    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    wizard.selectProjectAndCreate(Wizard.SamplesName.WEB_JAVA_SPRING, PROJECT_NAME);

    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PATH_FOR_EXPAND, "GreetingController.java");
    menu.runCommand(
        TestMenuCommandsConstants.Profile.PROFILE_MENU,
        TestMenuCommandsConstants.Profile.PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.waitMenuInCollapsedDropdown(Preferences.DropDownIdeSettingsMenus.EDITOR);
    preferences.selectDroppedMenuByName(Preferences.DropDownIdeSettingsMenus.EDITOR);
    List<String> settingsList = preferences.getAllSettingsFromEditorWidget(HEADER_SETTINGS);
    Assert.assertEquals(
        settingsList,
        expectedEditorSettingsList,
        "Check settings list from the editor settings section:");
    enterTestCode(Preferences.FlagForEditorWidget.CHECK, settingsList);
    Assert.assertEquals(
        editor.getVisibleTextFromEditor(),
        EXPECTED_TEXT_WITH_CHECKING_SETTINGS,
        "Check text form Editor with checking all settings:");

    editor.waitRulerAnnotationsIsPresent();
    editor.waitRulerFoldingIsPresent();
    editor.waitRulerLineIsPresent();
    editor.waitRulerOverviewIsPresent();
    editor.waitAllPunctuationSeparatorsArePresent();
    editor.waitTextViewRulerIsPresent();
    menu.runCommand(
        TestMenuCommandsConstants.Profile.PROFILE_MENU,
        TestMenuCommandsConstants.Profile.PREFERENCES);
    enterTestCode(Preferences.FlagForEditorWidget.UNCHECK, settingsList);
    Assert.assertEquals(
        editor.getVisibleTextFromEditor(),
        EXPECTED_TEXT_WITH_UNCHECKING_SETTINGS,
        "Check text from Editor with unchecking all settings:");

    editor.waitRulerAnnotationsIsNotPresent();
    editor.waitRulerFoldingIsNotPresent();
    editor.waitRulerLineIsNotPresent();
    editor.waitRulerOverviewIsNotPresent();
    editor.waitAllPunctuationSeparatorsAreNotPresent();
    editor.waitTextViewRulerIsNotPresent();
  }

  private void enterTestCode(Preferences.FlagForEditorWidget flagValue, List<String> settingsList) {
    preferences.setAllCheckboxSettingsInEditorWidget(flagValue, settingsList);
    preferences.clickOnOkBtn();
    preferences.clickOnCloseBtn();
    loader.waitOnClosed();

    projectExplorer.waitItem(PATH);
    projectExplorer.openItemByPath(PATH);

    loader.waitOnClosed();
    editor.waitActiveEditor();
    editor.deleteAllContent();
    editor.setCursorToLine(1);
    editor.typeTextIntoEditor(Keys.chord("("));
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.typeTextIntoEditor(Keys.chord("{"));
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.typeTextIntoEditor(Keys.chord("<"));
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.typeTextIntoEditor(Keys.chord("["));
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.typeTextIntoEditor(Keys.chord("\""));
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.typeTextIntoEditor(Keys.chord("'"));
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.typeTextIntoEditor(Keys.chord("/*"));
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    if (Preferences.FlagForEditorWidget.CHECK.equals(flagValue)) {
      editor.setCursorToLine(9);
    }

    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.typeTextIntoEditor(Keys.chord("/**"));
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
  }
}
