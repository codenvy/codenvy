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
import com.codenvy.im.artifacts.ArtifactFactory;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.response.DownloadArtifactInfo;
import com.codenvy.im.response.DownloadProgressResponse;
import com.codenvy.im.response.InstallArtifactInfo;
import com.codenvy.im.response.InstallArtifactStepInfo;
import com.codenvy.im.response.UpdateArtifactInfo;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author Dmytro Nochevnov */
public class TestDownloadCommand extends AbstractTestCommand {
    private AbstractIMCommand spyCommand;

    @BeforeMethod
    public void initMocks() throws IOException {
        AbstractIMCommand.updateImClientDone = false;
        spyCommand = spy(new DownloadCommand());
        performBaseMocks(spyCommand, true);
    }

    @Test
    public void testDownload() throws Exception {
        doNothing().when(mockFacade).startDownload(null, null);
        doReturn(new DownloadProgressResponse(DownloadArtifactInfo.Status.DOWNLOADED,
                                              100,
                                              Collections.<DownloadArtifactInfo>emptyList())).when(mockFacade).getDownloadProgress();

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "Downloading might take several minutes depending on your internet connection. Please wait.\n" +
                             "{\n" +
                             "  \"artifacts\" : [ ],\n" +
                             "  \"status\" : \"OK\"\n" +
                             "}\n");
    }


    @Test
    public void testDownloadWhenErrorInResponse() throws Exception {
        doNothing().when(mockFacade).startDownload(null, null);
        doReturn(new DownloadProgressResponse(DownloadArtifactInfo.Status.FAILED,
                                              0,
                                              Collections.<DownloadArtifactInfo>emptyList())).when(mockFacade).getDownloadProgress();

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "Downloading might take several minutes depending on your internet connection. Please wait.\n" +
                             "{\n" +
                             "  \"artifacts\" : [ ],\n" +
                             "  \"status\" : \"ERROR\"\n" +
                             "}\n");
    }

    @Test
    public void testDownloadWhenServiceThrowsError() throws Exception {
        String expectedOutput = "{\n"
                                + "  \"message\" : \"Server Error Exception\",\n"
                                + "  \"status\" : \"ERROR\"\n"
                                + "}";
        doThrow(new RuntimeException("Server Error Exception")).when(mockFacade).startDownload(null, null);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "Downloading might take several minutes depending on your internet connection. Please wait.\n" + expectedOutput + "\n");
    }

    @Test
    public void testListLocalOption() throws Exception {
        doReturn(Collections.emptyList()).when(mockFacade).getDownloads(null, null);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--list-local", Boolean.TRUE);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.getOutputStream();
        assertEquals(output, "{\n" +
                             "  \"artifacts\" : [ ],\n" +
                             "  \"status\" : \"OK\"\n" +
                             "}\n");
    }

    @Test
    public void testListRemoteOption() throws Exception {
        final Artifact codenvy = ArtifactFactory.createArtifact(CDECArtifact.NAME);

        doReturn(ImmutableList.of(UpdateArtifactInfo.createInstance(codenvy.getName(),
                                                                    "1.0.0",
                                                                    UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD),
                                  UpdateArtifactInfo.createInstance(codenvy.getName(),
                                                                    "1.0.1",
                                                                    UpdateArtifactInfo.Status.DOWNLOADED),
                                  UpdateArtifactInfo.createInstance(codenvy.getName(),
                                                                    "1.0.2",
                                                                    UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD)))
            .when(mockFacade).getAllUpdates(codenvy);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--list-remote", Boolean.TRUE);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.getOutputStream();
        assertEquals(output, "[ {\n"
                             + "  \"artifact\" : \"codenvy\",\n"
                             + "  \"version\" : \"1.0.2\",\n"
                             + "  \"status\" : \"AVAILABLE_TO_DOWNLOAD\"\n"
                             + "}, {\n"
                             + "  \"artifact\" : \"codenvy\",\n"
                             + "  \"version\" : \"1.0.1\",\n"
                             + "  \"status\" : \"DOWNLOADED\"\n"
                             + "}, {\n"
                             + "  \"artifact\" : \"codenvy\",\n"
                             + "  \"version\" : \"1.0.0\",\n"
                             + "  \"status\" : \"AVAILABLE_TO_DOWNLOAD\"\n"
                             + "} ]\n");
    }

    @Test
    public void testAutomaticUpdateCli() throws Exception {
        final Version versionToUpdate = Version.valueOf("1.0.0");
        final Artifact imArtifact = createArtifact(InstallManagerArtifact.NAME);

        UpdateArtifactInfo updateInfo = UpdateArtifactInfo.createInstance(imArtifact.getName(),
                                                                          versionToUpdate.toString(),
                                                                          UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD);
        doReturn(Collections.singletonList(updateInfo)).when(mockFacade).getAllUpdatesAfterInstalledVersion(imArtifact);

        doReturn(new DownloadProgressResponse(DownloadArtifactInfo.Status.DOWNLOADED, null, 100, Collections.EMPTY_LIST))
            .when(mockFacade).getDownloadProgress();

        InstallOptions installOptions = new InstallOptions();
        installOptions.setConfigProperties(Collections.EMPTY_MAP);
        installOptions.setInstallType(InstallType.SINGLE_SERVER);
        installOptions.setStep(0);

        String stepId = "1";
        doReturn(stepId).when(mockFacade).update(imArtifact, versionToUpdate, installOptions);
        InstallArtifactStepInfo installStepInfo = new InstallArtifactStepInfo();
        installStepInfo.setStatus(InstallArtifactInfo.Status.SUCCESS);
        doReturn(installStepInfo).when(mockFacade).getUpdateStepInfo(stepId);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--list-local", Boolean.TRUE);
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertTrue(output.startsWith("The Codenvy CLI is out of date. We are doing an automatic update. Relaunch.\n"));

        verify(mockFacade).startDownload(imArtifact, versionToUpdate);
        verify(mockFacade).waitForInstallStepCompleted(stepId);
        verify(spyConsole).exit(0);
    }

    @Test
    public void testAutomaticUpdateCliWhenException() throws Exception {
        final Artifact imArtifact = createArtifact(InstallManagerArtifact.NAME);

        doThrow(new RuntimeException("Error")).when(mockFacade).getAllUpdatesAfterInstalledVersion(imArtifact);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--list-local", Boolean.TRUE);
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "WARNING: automatic update of IM CLI client failed. See logs for details.\n"
                             + "\n"
                             + "{\n"
                             + "  \"artifacts\" : [ ],\n"
                             + "  \"status\" : \"OK\"\n"
                             + "}\n");
    }

    @Test
    public void testAutomaticUpdateCliWhenDownloadIsFailing() throws Exception {
        final Version versionToUpdate = Version.valueOf("1.0.0");
        final Artifact imArtifact = createArtifact(InstallManagerArtifact.NAME);

        UpdateArtifactInfo updateInfo = UpdateArtifactInfo.createInstance(imArtifact.getName(),
                                                                          versionToUpdate.toString(),
                                                                          UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD);
        doReturn(Collections.singletonList(updateInfo)).when(mockFacade).getAllUpdatesAfterInstalledVersion(imArtifact);

        doReturn(new DownloadProgressResponse(DownloadArtifactInfo.Status.FAILED, "Download error.", 100, Collections.EMPTY_LIST))
            .when(mockFacade).getDownloadProgress();

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--list-local", Boolean.TRUE);
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "WARNING: automatic update of IM CLI client failed. See logs for details.\n"
                             + "\n"
                             + "{\n"
                             + "  \"artifacts\" : [ ],\n"
                             + "  \"status\" : \"OK\"\n"
                             + "}\n");

        verify(mockFacade).startDownload(imArtifact, versionToUpdate);
    }

    @Test
    public void testAutomaticUpdateCliWhenInstallIsFailing() throws Exception {
        final Version versionToUpdate = Version.valueOf("1.0.0");
        final Artifact imArtifact = createArtifact(InstallManagerArtifact.NAME);

        UpdateArtifactInfo updateInfo = UpdateArtifactInfo.createInstance(imArtifact.getName(),
                                                                          versionToUpdate.toString(),
                                                                          UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD);
        doReturn(Collections.singletonList(updateInfo)).when(mockFacade).getAllUpdatesAfterInstalledVersion(imArtifact);

        doReturn(new DownloadProgressResponse(DownloadArtifactInfo.Status.DOWNLOADED, null, 100, Collections.EMPTY_LIST))
            .when(mockFacade).getDownloadProgress();

        InstallOptions installOptions = new InstallOptions();
        installOptions.setConfigProperties(Collections.EMPTY_MAP);
        installOptions.setInstallType(InstallType.SINGLE_SERVER);
        installOptions.setStep(0);

        String stepId = "1";
        doReturn(stepId).when(mockFacade).update(imArtifact, versionToUpdate, installOptions);
        InstallArtifactStepInfo installStepInfo = new InstallArtifactStepInfo();
        installStepInfo.setStatus(InstallArtifactInfo.Status.FAILURE);
        installStepInfo.setMessage("Install error.");
        doReturn(installStepInfo).when(mockFacade).getUpdateStepInfo(stepId);

        doNothing().when(spyConsole).pressAnyKey("This CLI client is out-dated. To finish automatic update, please, press any key to exit and then restart it.\n");

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--list-local", Boolean.TRUE);
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "WARNING: automatic update of IM CLI client failed. See logs for details.\n"
                             + "\n"
                             + "{\n"
                             + "  \"artifacts\" : [ ],\n"
                             + "  \"status\" : \"OK\"\n"
                             + "}\n");

        verify(mockFacade).startDownload(imArtifact, versionToUpdate);
        verify(mockFacade).waitForInstallStepCompleted(stepId);
    }

}
