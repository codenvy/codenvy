/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.im.cli.command;

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.codenvy.im.artifacts.VersionLabel;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.response.InstallArtifactInfo;
import com.codenvy.im.response.UpdateArtifactInfo;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Reshetnyak
 * @author Dmytro Nochevnov
 */
public class TestVersionCommand extends AbstractTestCommand {
    public static final InstallArtifactInfo INSTALLED_CDEC_1_0_1_UNSTABLE = InstallArtifactInfo.createInstance(CDECArtifact.NAME,
                                                                                                               "1.0.1",
                                                                                                               VersionLabel.UNSTABLE,
                                                                                                               InstallArtifactInfo.Status.SUCCESS);

    public static final InstallArtifactInfo INSTALLED_CDEC_1_0_1_STABLE = InstallArtifactInfo.createInstance(CDECArtifact.NAME,
                                                                                                             "1.0.1",
                                                                                                             VersionLabel.STABLE,
                                                                                                             InstallArtifactInfo.Status.SUCCESS);

    public static final UpdateArtifactInfo UPDATE_CDEC_1_0_2_STABLE_AVAILABLE_TO_DOWNLOAD = UpdateArtifactInfo.createInstance(CDECArtifact.NAME,
                                                                                                                              "1.0.2",
                                                                                                                              VersionLabel.STABLE,
                                                                                                                              UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD);

    public static final UpdateArtifactInfo UPDATE_CDEC_1_0_3_SNAPSHOT_UNSTABLE_AVAILABLE_TO_DOWNLOAD = UpdateArtifactInfo.createInstance(CDECArtifact.NAME,
                                                                                                                                         "1.0.3-SNAPSHOT",
                                                                                                                                         VersionLabel.UNSTABLE,
                                                                                                                                         UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD);

    public static final UpdateArtifactInfo UPDATE_CDEC_1_0_2_STABLE_DOWNLOADED = UpdateArtifactInfo.createInstance(CDECArtifact.NAME,
                                                                                                                   "1.0.2",
                                                                                                                   VersionLabel.STABLE,
                                                                                                                   UpdateArtifactInfo.Status.DOWNLOADED);

    public static final UpdateArtifactInfo UPDATE_CDEC_1_0_3_SNAPSHOT_UNSTABLE_DOWNLOADED = UpdateArtifactInfo.createInstance(CDECArtifact.NAME,
                                                                                                                              "1.0.3-SNAPSHOT",
                                                                                                                              VersionLabel.UNSTABLE,
                                                                                                                              UpdateArtifactInfo.Status.DOWNLOADED);

    public static final InstallArtifactInfo INSTALLED_IM_1_0_1_STABLE = InstallArtifactInfo.createInstance(InstallManagerArtifact.NAME,
                                                                                                           "1.0.1",
                                                                                                           VersionLabel.STABLE,
                                                                                                           InstallArtifactInfo.Status.SUCCESS);

    public static final InstallArtifactInfo INSTALLED_CDEC_1_0_1_WITHOUT_LABEL = InstallArtifactInfo.createInstance(CDECArtifact.NAME,
                                                                                                                    "1.0.1",
                                                                                                                    InstallArtifactInfo.Status.SUCCESS);

    public static final UpdateArtifactInfo UPDATE_CDEC_1_0_2_WITHOUT_LABEL = UpdateArtifactInfo.createInstance(CDECArtifact.NAME,
                                                                                                               "1.0.2",
                                                                                                               UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD);

    private VersionCommand spyCommand;

    @BeforeMethod
    public void initMocks() throws Exception {
        spyCommand = spy(new VersionCommand());
        performBaseMocks(spyCommand, true);

        doReturn(new HashMap<>(ImmutableMap.of("a", "MANDATORY"))).when(mockConfigManager).loadCodenvyDefaultProperties(Version.valueOf("1.0.1"),
                                                                                                                        InstallType.SINGLE_SERVER);
        doReturn(new Config(new HashMap<>(ImmutableMap.of("a", "MANDATORY")))).when(mockConfigManager)
                                                                              .loadInstalledCodenvyConfig(InstallType.MULTI_SERVER);

        doNothing().when(spyCommand).updateImCliClientIfNeeded();
    }


    @Test(dataProvider = "getTestVersionData")
    public void testCodenvyVersion(List<InstallArtifactInfo> installed, List<UpdateArtifactInfo> updates, String expectedOutput) throws Exception {
        doReturn(installed).when(mockFacade).getInstalledVersions();
        doReturn(updates).when(mockFacade).getAllUpdatesAfterInstalledVersion(any(Artifact.class));

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, expectedOutput);
    }

    @DataProvider
    private Object[][] getTestVersionData() {
        return new Object[][]{
            {ImmutableList.of(),
             ImmutableList.of(),
             "{\n"
             + "  \"artifact\" : \"codenvy\"\n"
             + "}\n"},

            {ImmutableList.of(),
             ImmutableList.of(UPDATE_CDEC_1_0_2_STABLE_AVAILABLE_TO_DOWNLOAD,
                              UPDATE_CDEC_1_0_3_SNAPSHOT_UNSTABLE_AVAILABLE_TO_DOWNLOAD),
             "{\n"
             + "  \"artifact\" : \"codenvy\",\n"
             + "  \"availableVersion\" : {\n"
             + "    \"stable\" : \"1.0.2\",\n"
             + "    \"unstable\" : \"1.0.3-SNAPSHOT\"\n"
             + "  }\n"
             + "}\n"},

            {ImmutableList.of(INSTALLED_IM_1_0_1_STABLE,
                              INSTALLED_CDEC_1_0_1_STABLE),
             ImmutableList.of(),
             "{\n"
             + "  \"artifact\" : \"codenvy\",\n"
             + "  \"version\" : \"1.0.1\",\n"
             + "  \"label\" : \"STABLE\",\n"
             + "  \"status\" : \"You are running the latest stable version of Codenvy!\"\n"
             + "}\n"},

            {ImmutableList.of(INSTALLED_IM_1_0_1_STABLE,
                              INSTALLED_CDEC_1_0_1_UNSTABLE),
             ImmutableList.of(),
             "{\n"
             + "  \"artifact\" : \"codenvy\",\n"
             + "  \"version\" : \"1.0.1\",\n"
             + "  \"label\" : \"UNSTABLE\"\n"
             + "}\n"},

            {ImmutableList.of(INSTALLED_IM_1_0_1_STABLE,
                              INSTALLED_CDEC_1_0_1_STABLE),

             ImmutableList.of(UPDATE_CDEC_1_0_3_SNAPSHOT_UNSTABLE_DOWNLOADED),
             "{\n"
             + "  \"artifact\" : \"codenvy\",\n"
             + "  \"version\" : \"1.0.1\",\n"
             + "  \"label\" : \"STABLE\",\n"
             + "  \"availableVersion\" : {\n"
             + "    \"unstable\" : \"1.0.3-SNAPSHOT\"\n"
             + "  },\n"
             + "  \"status\" : \"You are running the latest stable version of Codenvy!\"\n"
             + "}\n"},

            {ImmutableList.of(INSTALLED_IM_1_0_1_STABLE,
                              INSTALLED_CDEC_1_0_1_UNSTABLE),
             ImmutableList.of(UPDATE_CDEC_1_0_3_SNAPSHOT_UNSTABLE_DOWNLOADED),
             "{\n"
             + "  \"artifact\" : \"codenvy\",\n"
             + "  \"version\" : \"1.0.1\",\n"
             + "  \"label\" : \"UNSTABLE\",\n"
             + "  \"availableVersion\" : {\n"
             + "    \"unstable\" : \"1.0.3-SNAPSHOT\"\n"
             + "  }\n"
             + "}\n"},

            {ImmutableList.of(INSTALLED_IM_1_0_1_STABLE,
                              INSTALLED_CDEC_1_0_1_STABLE),

             ImmutableList.of(UPDATE_CDEC_1_0_2_STABLE_AVAILABLE_TO_DOWNLOAD,
                              UPDATE_CDEC_1_0_3_SNAPSHOT_UNSTABLE_AVAILABLE_TO_DOWNLOAD),
             "{\n"
             + "  \"artifact\" : \"codenvy\",\n"
             + "  \"version\" : \"1.0.1\",\n"
             + "  \"label\" : \"STABLE\",\n"
             + "  \"availableVersion\" : {\n"
             + "    \"stable\" : \"1.0.2\",\n"
             + "    \"unstable\" : \"1.0.3-SNAPSHOT\"\n"
             + "  },\n"
             + "  \"status\" : \"There is a new stable version of Codenvy available. Run 'download' command to download it.\"\n"
             + "}\n"},

            {ImmutableList.of(INSTALLED_IM_1_0_1_STABLE,
                              INSTALLED_CDEC_1_0_1_STABLE),

             ImmutableList.of(UPDATE_CDEC_1_0_2_STABLE_DOWNLOADED,
                              UPDATE_CDEC_1_0_3_SNAPSHOT_UNSTABLE_DOWNLOADED),
             "{\n"
             + "  \"artifact\" : \"codenvy\",\n"
             + "  \"version\" : \"1.0.1\",\n"
             + "  \"label\" : \"STABLE\",\n"
             + "  \"availableVersion\" : {\n"
             + "    \"stable\" : \"1.0.2\",\n"
             + "    \"unstable\" : \"1.0.3-SNAPSHOT\"\n"
             + "  },\n"
             + "  \"status\" : \"There is a new stable version of Codenvy available. Run 'install' command to install it.\"\n"
             + "}\n"},

            {ImmutableList.of(INSTALLED_CDEC_1_0_1_WITHOUT_LABEL),
             ImmutableList.of(UPDATE_CDEC_1_0_2_WITHOUT_LABEL),
             "{\n"
             + "  \"artifact\" : \"codenvy\",\n"
             + "  \"version\" : \"1.0.1\"\n"
             + "}\n"}
        };
    }

}
