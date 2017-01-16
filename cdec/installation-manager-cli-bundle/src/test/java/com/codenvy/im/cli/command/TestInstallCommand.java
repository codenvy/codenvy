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
import com.codenvy.im.commands.PatchCDECCommand;
import com.codenvy.im.event.Event;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.response.InstallArtifactInfo;
import com.codenvy.im.response.InstallArtifactStepInfo;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static java.lang.String.format;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Dmytro Nochevnov
 *         Alexander Reshetnyak
 */
public class TestInstallCommand extends AbstractTestCommand {
    public static final String       TEST_ARTIFACT            = CDECArtifact.NAME;
    public static final String       TEST_VERSION             = "1.0.1";
    public static final String       ERROR_MESSAGE            = "error";
    public static final List<String> INSTALL_INFO             = ImmutableList.of("step 1", "step 2", "step 3");
    public static final Path         PATH_TO_TEST_UPDATE_INFO = Paths.get(TestInstallCommand.class.getClassLoader().getResource(".").getPath())
                                                                     .resolve(PatchCDECCommand.UPDATE_INFO);
    private InstallCommand spyCommand;

    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;

    PrintStream originOut = System.out;
    PrintStream originErr = System.err;

    @BeforeMethod
    public void initMocks() throws Exception {
        spyCommand = spy(new InstallCommand(mockConfigManager));
        performBaseMocks(spyCommand, true);

        doReturn(new HashMap<>(ImmutableMap.of("a", "MANDATORY"))).when(mockConfigManager).loadCodenvyDefaultProperties(Version.valueOf("1.0.1"),
                                                                                                                        InstallType.SINGLE_SERVER);
        doReturn(new Config(new HashMap<>(ImmutableMap.of("a", "MANDATORY")))).when(mockConfigManager)
                                                                              .loadInstalledCodenvyConfig(InstallType.MULTI_SERVER);

        doReturn(INSTALL_INFO).when(mockFacade).getInstallInfo(any(Artifact.class), any(InstallType.class));
    }

    @BeforeMethod
    public void initStreams() {
        this.outputStream = new ByteArrayOutputStream();
        this.errorStream = new ByteArrayOutputStream();

        System.setOut(new PrintStream(this.outputStream));
        System.setErr(new PrintStream(this.errorStream));
    }

    @AfterMethod
    public void restoreSystemStreams() {
        System.setOut(originOut);
        System.setErr(originErr);
    }

    @AfterMethod
    public void removeUpdateInfo() {
        FileUtils.deleteQuietly(PATH_TO_TEST_UPDATE_INFO.toFile());
    }

    @Test
    public void shouldInstallArtifactOfCertainVersion() throws Exception {
        ArgumentCaptor<Event> eventArgument = ArgumentCaptor.forClass(Event.class);

        InstallArtifactStepInfo info = mock(InstallArtifactStepInfo.class);
        doReturn(InstallArtifactInfo.Status.SUCCESS).when(info).getStatus();

        doReturn(info).when(mockFacade).getUpdateStepInfo(anyString());
        doReturn("id").when(mockFacade).install(any(Artifact.class), any(Version.class), any(InstallOptions.class));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.argument("version", TEST_VERSION);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, format("step 1 [OK]\n" +
                                    "step 2 [OK]\n" +
                                    "step 3 [OK]\n" +
                                    "{\n" +
                                    "  \"artifacts\" : [ {\n" +
                                    "    \"artifact\" : \"%s\",\n" +
                                    "    \"version\" : \"%s\",\n" +
                                    "    \"status\" : \"SUCCESS\"\n" +
                                    "  } ],\n" +
                                    "  \"status\" : \"OK\"\n" +
                                    "}\n", TEST_ARTIFACT, TEST_VERSION));

        verify(mockFacade, times(2)).logSaasAnalyticsEvent(eventArgument.capture());

        List<Event> values = eventArgument.getAllValues();
        assertEquals(values.get(0).getType(), Event.Type.IM_ARTIFACT_INSTALL_STARTED);
        assertTrue(values.get(0).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, TIME=\\d*}", TEST_ARTIFACT, TEST_VERSION)),
                   "Actual parameters: " + values.get(0).getParameters().toString());

        assertEquals(values.get(1).getType(), Event.Type.IM_ARTIFACT_INSTALL_FINISHED_SUCCESSFULLY);
        assertTrue(values.get(1).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, TIME=\\d*}", TEST_ARTIFACT, TEST_VERSION)),
                   "Actual parameters: " + values.get(1).getParameters().toString());
    }

    @Test
    public void shouldInstallMultiServerArtifact() throws Exception {
        ArgumentCaptor<Event> eventArgument = ArgumentCaptor.forClass(Event.class);

        InstallArtifactStepInfo info = mock(InstallArtifactStepInfo.class);
        doReturn(InstallArtifactInfo.Status.SUCCESS).when(info).getStatus();

        doReturn(info).when(mockFacade).getUpdateStepInfo(anyString());
        doReturn("id").when(mockFacade).install(any(Artifact.class), any(Version.class), any(InstallOptions.class));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.argument("version", TEST_VERSION);
        commandInvoker.option("--multi", Boolean.TRUE);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, format("step 1 [OK]\n" +
                                    "step 2 [OK]\n" +
                                    "step 3 [OK]\n" +
                                    "{\n" +
                                    "  \"artifacts\" : [ {\n" +
                                    "    \"artifact\" : \"%s\",\n" +
                                    "    \"version\" : \"%s\",\n" +
                                    "    \"status\" : \"SUCCESS\"\n" +
                                    "  } ],\n" +
                                    "  \"status\" : \"OK\"\n" +
                                    "}\n", TEST_ARTIFACT, TEST_VERSION));

        verify(mockFacade, times(2)).logSaasAnalyticsEvent(eventArgument.capture());

        List<Event> values = eventArgument.getAllValues();
        assertEquals(values.get(0).getType(), Event.Type.IM_ARTIFACT_INSTALL_STARTED);
        assertTrue(values.get(0).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, TIME=\\d*}", TEST_ARTIFACT, TEST_VERSION)),
                   "Actual parameters: " + values.get(0).getParameters().toString());

        assertEquals(values.get(1).getType(), Event.Type.IM_ARTIFACT_INSTALL_FINISHED_SUCCESSFULLY);
        assertTrue(values.get(1).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, TIME=\\d*}", TEST_ARTIFACT, TEST_VERSION)),
                   "Actual parameters: " + values.get(1).getParameters().toString());
    }

    @Test
    public void shouldUpdateAfterEnteringInstallOptions() throws Exception {
        doReturn(false).when(spyCommand).isInstall(any(Artifact.class));
        doReturn(new HashMap<>(ImmutableMap.of("a", "2", "b", "MANDATORY"))).when(mockConfigManager).prepareInstallProperties(anyString(),
                                                                                                                              any(Path.class),
                                                                                                                              any(InstallType.class),
                                                                                                                              any(Artifact.class),
                                                                                                                              any(Version.class),
                                                                                                                              anyBoolean());
        // user always enter "some value" as property value
        doAnswer(invocationOnMock -> {
               spyConsole.print("some value\n");
                return "some value";
        }).when(spyConsole).readLine();

        // no installation info provided
        doReturn(Collections.emptyList()).when(mockFacade).getInstallInfo(any(Artifact.class), any(InstallType.class));

        // firstly don't confirm install options, then confirm
        doAnswer(invocationOnMock -> {
           spyConsole.print(invocationOnMock.getArguments()[0].toString() + " [y/N]\n");
            return false;
        }).doAnswer(invocationOnMock -> {
           spyConsole.print(invocationOnMock.getArguments()[0].toString() + " [y/N]\n");
            return true;
        }).when(spyConsole).askUser(anyString());

        // test displaying update info
        FileUtils.write(PATH_TO_TEST_UPDATE_INFO.toFile(), "update info");
        doReturn(PATH_TO_TEST_UPDATE_INFO).when(spyCommand).getPathToUpdateInfoFile();
        doNothing().when(spyCommand).removeUpdateInfoFile();

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.argument("version", "1.0.2");

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();

        assertEquals(output, "{\n" +
                             "  \"artifacts\" : [ {\n" +
                             "    \"artifact\" : \"codenvy\",\n" +
                             "    \"version\" : \"1.0.2\",\n" +
                             "    \"status\" : \"SUCCESS\"\n" +
                             "  } ],\n" +
                             "  \"status\" : \"OK\"\n" +
                             "}\n" +
                             "update info\n");

        verify(mockFacade, never()).logSaasAnalyticsEvent(any(Event.class));

        verify(spyCommand).removeUpdateInfoFile();
        assertTrue(Files.exists(PATH_TO_TEST_UPDATE_INFO));
    }

    @Test
    public void shouldFailsOnUnknownArtifact() throws Exception {
        doThrow(new IOException("Artifact 'any' not found")).when(mockFacade).getInstallInfo(any(Artifact.class), any(InstallType.class));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", "any");

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "{\n"
                             + "  \"message\" : \"Artifact 'any' not found\",\n"
                             + "  \"status\" : \"ERROR\"\n"
                             + "}\n");

        verify(mockFacade, never()).logSaasAnalyticsEvent(any(Event.class));
    }

    @Test
    public void shouldFailOnInstallationStepException() throws Exception {
        ArgumentCaptor<Event> eventArgument = ArgumentCaptor.forClass(Event.class);

        doThrow(new IOException(ERROR_MESSAGE)).when(mockFacade).install(any(Artifact.class), any(Version.class), any(InstallOptions.class));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.argument("version", TEST_VERSION);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, format("step 1 [FAIL]\n" +
                             "{\n" +
                             "  \"artifacts\" : [ {\n" +
                             "    \"artifact\" : \"%s\",\n" +
                             "    \"version\" : \"%s\",\n" +
                             "    \"status\" : \"FAILURE\"\n" +
                             "  } ],\n" +
                             "  \"message\" : \"%s\",\n" +
                             "  \"status\" : \"ERROR\"\n" +
                             "}\n", TEST_ARTIFACT, TEST_VERSION, ERROR_MESSAGE));

        verify(mockFacade, times(2)).logSaasAnalyticsEvent(eventArgument.capture());

        List<Event> values = eventArgument.getAllValues();
        assertEquals(values.get(0).getType(), Event.Type.IM_ARTIFACT_INSTALL_STARTED);
        assertTrue(values.get(0).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, TIME=\\d*}", TEST_ARTIFACT, TEST_VERSION)),
                   "Actual parameters: " + values.get(0).getParameters().toString());

        assertEquals(values.get(1).getType(), Event.Type.IM_ARTIFACT_INSTALL_FINISHED_UNSUCCESSFULLY);
        assertTrue(values.get(1).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, ERROR-MESSAGE=%s, TIME=\\d*}",
                                                                           TEST_ARTIFACT, TEST_VERSION, ERROR_MESSAGE)),
                   "Actual parameters: " + values.get(1).getParameters().toString());
    }

    @Test
    public void shouldFailOnErrorOfGettingUpdateStepInfo() throws Exception {
        ArgumentCaptor<Event> eventArgument = ArgumentCaptor.forClass(Event.class);
        
        InstallArtifactStepInfo testInstallArtifactStepInfo = new InstallArtifactStepInfo();
        testInstallArtifactStepInfo.setStatus(InstallArtifactInfo.Status.FAILURE);
        testInstallArtifactStepInfo.setMessage(ERROR_MESSAGE);

        doReturn(testInstallArtifactStepInfo).when(mockFacade).getUpdateStepInfo(anyString());

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.argument("version", TEST_VERSION);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, format("step 1 [FAIL]\n" +
                                    "{\n" +
                                    "  \"artifacts\" : [ {\n" +
                                    "    \"artifact\" : \"%s\",\n" +
                                    "    \"version\" : \"%s\",\n" +
                                    "    \"status\" : \"FAILURE\"\n" +
                                    "  } ],\n" +
                                    "  \"message\" : \"%s\",\n" +
                                    "  \"status\" : \"ERROR\"\n" +
                                    "}\n", TEST_ARTIFACT, TEST_VERSION, ERROR_MESSAGE));

        verify(mockFacade, times(2)).logSaasAnalyticsEvent(eventArgument.capture());

        List<Event> values = eventArgument.getAllValues();
        assertEquals(values.get(0).getType(), Event.Type.IM_ARTIFACT_INSTALL_STARTED);
        assertTrue(values.get(0).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, TIME=\\d*}", TEST_ARTIFACT, TEST_VERSION)),
                   "Actual parameters: " + values.get(0).getParameters().toString());

        assertEquals(values.get(1).getType(), Event.Type.IM_ARTIFACT_INSTALL_FINISHED_UNSUCCESSFULLY);
        assertTrue(values.get(1).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, ERROR-MESSAGE=%s, TIME=\\d*}",
                                                                           TEST_ARTIFACT, TEST_VERSION, ERROR_MESSAGE)),
                   "Actual parameters: " + values.get(1).getParameters().toString());
    }


    @Test
    public void testInstallWhenServiceThrowsError2() throws Exception {
        ArgumentCaptor<Event> eventArgument = ArgumentCaptor.forClass(Event.class);

        String errorMessage = "Property is missed";
        doThrow(new IOException(errorMessage)).when(mockFacade).getInstallInfo(any(Artifact.class), any(InstallType.class));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.argument("version", TEST_VERSION);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();

        assertEquals(output, format("{\n"
                             + "  \"message\" : \"%s\",\n"
                             + "  \"status\" : \"ERROR\"\n"
                             + "}\n", errorMessage));

        verify(mockFacade, times(2)).logSaasAnalyticsEvent(eventArgument.capture());

        List<Event> values = eventArgument.getAllValues();
        assertEquals(values.get(0).getType(), Event.Type.IM_ARTIFACT_INSTALL_STARTED);
        assertTrue(values.get(0).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, TIME=\\d*}", TEST_ARTIFACT, TEST_VERSION)),
                   "Actual parameters: " + values.get(0).getParameters().toString());

        assertEquals(values.get(1).getType(), Event.Type.IM_ARTIFACT_INSTALL_FINISHED_UNSUCCESSFULLY);
        assertTrue(values.get(1).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, ERROR-MESSAGE=%s, TIME=\\d*}",
                                                                           TEST_ARTIFACT, TEST_VERSION, errorMessage)),
                   "Actual parameters: " + values.get(1).getParameters().toString());
    }

     @Test
    public void testListInstalledArtifacts() throws Exception {
         doReturn(ImmutableList.of(InstallArtifactInfo.createInstance("codenvy",
                                                                      "1.0.1",
                                                                      InstallArtifactInfo.Status.SUCCESS)))
             .when(mockFacade).getInstalledVersions();

         CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--list", Boolean.TRUE);		
		
        CommandInvoker.Result result = commandInvoker.invoke();		
        String output = result.getOutputStream();		
        assertEquals(output, "{\n"		
                             + "  \"artifacts\" : [ {\n"		
                             + "    \"artifact\" : \"codenvy\",\n"		
                             + "    \"version\" : \"1.0.1\",\n"		
                             + "    \"status\" : \"SUCCESS\"\n"		
                             + "  } ],\n"		
                             + "  \"status\" : \"OK\"\n"		
                             + "}\n");		
    }		
		
    @Test		
    public void testListInstalledArtifactsWhenServiceError() throws Exception {		
        doReturn(new HashMap<>(ImmutableMap.of("a", "2", "b", "MANDATORY"))).when(mockConfigManager)		
                                                                            .merge(anyMap(), anyMap());
		
        doThrow(new RuntimeException("Server Error Exception"))		
                .when(mockFacade)		
                .getInstalledVersions();		
		
        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--list", Boolean.TRUE);		
		
        CommandInvoker.Result result = commandInvoker.invoke();		
        String output = result.disableAnsi().getOutputStream();		
        assertEquals(output, "{\n"		
                             + "  \"message\" : \"Server Error Exception\",\n"		
                             + "  \"status\" : \"ERROR\"\n"		
                             + "}\n");		
    }

    @Test
    public void testEnterInstallOptionsForInstall() throws Exception {
        ArgumentCaptor<Event> eventArgument = ArgumentCaptor.forClass(Event.class);
        
        doReturn(new HashMap<>(ImmutableMap.of("a", "MANDATORY"))).when(mockConfigManager).prepareInstallProperties(anyString(),
                                                                                                                    any(Path.class),
                                                                                                                    any(InstallType.class),
                                                                                                                    any(Artifact.class),
                                                                                                                    any(Version.class),
                                                                                                                    anyBoolean());

        doReturn(true).when(spyCommand).isInstall(any(Artifact.class));
        // user always enter "some value" as property value
        doAnswer(invocationOnMock -> {
           spyConsole.print("some value\n");
            return "some value";
        }).when(spyConsole).readLine();

        // no installation info provided
        doReturn(Collections.emptyList()).when(mockFacade).getInstallInfo(any(Artifact.class), any(InstallType.class));

        // firstly don't confirm install options, then confirm
        doAnswer(invocationOnMock -> {
           spyConsole.print(invocationOnMock.getArguments()[0].toString() + " [y/N]\n");
            return false;
        }).doAnswer(invocationOnMock -> {
           spyConsole.print(invocationOnMock.getArguments()[0].toString() + " [y/N]\n");
            return true;
        }).when(spyConsole).askUser(anyString());

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.argument("version", TEST_VERSION);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();

        assertEquals(output, format("{\n" +
                             "  \"artifacts\" : [ {\n" +
                             "    \"artifact\" : \"%s\",\n" +
                             "    \"version\" : \"%s\",\n" +
                             "    \"status\" : \"SUCCESS\"\n" +
                             "  } ],\n" +
                             "  \"status\" : \"OK\"\n" +
                             "}\n", TEST_ARTIFACT, TEST_VERSION));

        verify(mockFacade, times(2)).logSaasAnalyticsEvent(eventArgument.capture());

        List<Event> values = eventArgument.getAllValues();
        assertEquals(values.get(0).getType(), Event.Type.IM_ARTIFACT_INSTALL_STARTED);
        assertTrue(values.get(0).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, TIME=\\d*}", TEST_ARTIFACT, TEST_VERSION)),
                   "Actual parameters: " + values.get(0).getParameters().toString());

        assertEquals(values.get(1).getType(), Event.Type.IM_ARTIFACT_INSTALL_FINISHED_SUCCESSFULLY);
        assertTrue(values.get(1).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, TIME=\\d*}", TEST_ARTIFACT, TEST_VERSION)),
                   "Actual parameters: " + values.get(1).getParameters().toString());
    }

    @Test
    public void testInstallArtifactFromLocalBinariesFailedIfVersionMissed() throws Exception {
        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--binaries", "/path/to/file");

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "{\n"
                             + "  \"message\" : \"Parameter 'version' is missed\",\n"
                             + "  \"status\" : \"ERROR\"\n"
                             + "}\n");

        verify(mockFacade, never()).logSaasAnalyticsEvent(any(Event.class));
    }

    @Test
    public void testInstallArtifactFromLocalBinariesFailedIfArtifactMissed() throws Exception {
        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--binaries", "/path/to/file");
        commandInvoker.argument("version", "3.10.1");

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "{\n"
                             + "  \"message\" : \"Parameter 'artifact' is missed\",\n"
                             + "  \"status\" : \"ERROR\"\n"
                             + "}\n");

        verify(mockFacade, never()).logSaasAnalyticsEvent(any(Event.class));
    }

    @Test
    public void testInstallArtifactFromLocalBinaries() throws Exception {
        ArgumentCaptor<Event> eventArgument = ArgumentCaptor.forClass(Event.class);
        
        String versionNumber = "3.10.1";
        String binaries = "/path/to/file";

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--binaries", binaries);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.argument("version", versionNumber);

        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();   // TODO [ndp] strange result
        assertEquals(output, "step 1{\n"
                             + "  \"status\" : \"ERROR\"\n"
                             + "}\n");

        verify(mockConfigManager).prepareInstallProperties(isNull(String.class),
                                                       eq(Paths.get(binaries)),
                                                       eq(InstallType.SINGLE_SERVER),
                                                       eq(ArtifactFactory.createArtifact(TEST_ARTIFACT)),
                                                       eq(Version.valueOf(versionNumber)),
                                                       eq(Boolean.TRUE));

        verify(mockFacade).install(eq(ArtifactFactory.createArtifact(TEST_ARTIFACT)),
                                          eq(Version.valueOf(versionNumber)),
                                          eq(Paths.get(binaries)),
                                          any(InstallOptions.class));

        verify(mockFacade, times(1)).logSaasAnalyticsEvent(eventArgument.capture());

        List<Event> values = eventArgument.getAllValues();
        assertEquals(values.get(0).getType(), Event.Type.IM_ARTIFACT_INSTALL_STARTED);
        assertTrue(values.get(0).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, TIME=\\d*}", TEST_ARTIFACT, versionNumber)),
                   "Actual parameters: " + values.get(0).getParameters().toString());
    }

    @Test
    public void testReinstallCodenvy() throws Exception {
        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.option("--reinstall", Boolean.TRUE);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "{\n"
                             + "  \"artifacts\" : [ {\n"
                             + "    \"artifact\" : \"codenvy\",\n"
                             + "    \"status\" : \"SUCCESS\"\n"
                             + "  } ],\n"
                             + "  \"status\" : \"OK\"\n"
                             + "}\n");

        verify(mockFacade).reinstall(createArtifact(TEST_ARTIFACT));
        verify(mockFacade, never()).logSaasAnalyticsEvent(any(Event.class));
    }

    @Test
    public void testReinstallImCli() throws Exception {
        doThrow(new UnsupportedOperationException("error message")).when(mockFacade).reinstall(createArtifact(InstallManagerArtifact.NAME));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", InstallManagerArtifact.NAME);
        commandInvoker.option("--reinstall", Boolean.TRUE);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "{\n"
                             + "  \"artifacts\" : [ {\n"
                             + "    \"artifact\" : \"installation-manager-cli\",\n"
                             + "    \"status\" : \"FAILURE\"\n"
                             + "  } ],\n"
                             + "  \"message\" : \"error message\",\n"
                             + "  \"status\" : \"ERROR\"\n"
                             + "}\n");

        verify(mockFacade, never()).logSaasAnalyticsEvent(any(Event.class));
    }

    @Test
    public void shouldNotInterruptInstallIfLoggingToSaasCodenvyFail() throws Exception {
        InstallArtifactStepInfo info = mock(InstallArtifactStepInfo.class);
        doReturn(InstallArtifactInfo.Status.SUCCESS).when(info).getStatus();

        doReturn(info).when(mockFacade).getUpdateStepInfo(anyString());
        doReturn("id").when(mockFacade).install(any(Artifact.class), any(Version.class), any(InstallOptions.class));

        doThrow(new IOException("error")).when(mockFacade).logSaasAnalyticsEvent(any(Event.class));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.argument("version", TEST_VERSION);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, format("step 1 [OK]\n" +
                                    "step 2 [OK]\n" +
                                    "step 3 [OK]\n" +
                                    "{\n" +
                                    "  \"artifacts\" : [ {\n" +
                                    "    \"artifact\" : \"%s\",\n" +
                                    "    \"version\" : \"%s\",\n" +
                                    "    \"status\" : \"SUCCESS\"\n" +
                                    "  } ],\n" +
                                    "  \"status\" : \"OK\"\n" +
                                    "}\n", TEST_ARTIFACT, TEST_VERSION));

    }

    @Test
    public void shouldInstallArtifactForceFirstStep() throws Exception {
        ArgumentCaptor<Event> eventArgument = ArgumentCaptor.forClass(Event.class);

        InstallArtifactStepInfo info = mock(InstallArtifactStepInfo.class);
        doReturn(InstallArtifactInfo.Status.SUCCESS).when(info).getStatus();

        doReturn(info).when(mockFacade).getUpdateStepInfo(anyString());
        doReturn("id").when(mockFacade).install(any(Artifact.class), any(Version.class), any(InstallOptions.class));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.argument("version", TEST_VERSION);
        commandInvoker.option("--step", 1);
        commandInvoker.option("--forceInstall", true);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "step 1 [OK]\n");

        verify(mockFacade, times(1)).logSaasAnalyticsEvent(eventArgument.capture());

        List<Event> values = eventArgument.getAllValues();
        assertEquals(values.get(0).getType(), Event.Type.IM_ARTIFACT_INSTALL_STARTED);
        assertTrue(values.get(0).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, TIME=\\d*}", TEST_ARTIFACT, TEST_VERSION)),
                   "Actual parameters: " + values.get(0).getParameters().toString());
    }

    @Test
    public void shouldInstallArtifactForceMiddleStepAndFail() throws Exception {
        ArgumentCaptor<Event> eventArgument = ArgumentCaptor.forClass(Event.class);

        InstallArtifactStepInfo info = mock(InstallArtifactStepInfo.class);
        doReturn(InstallArtifactInfo.Status.SUCCESS).when(info).getStatus();

        doReturn(info).when(mockFacade).getUpdateStepInfo(anyString());
        doReturn("id").when(mockFacade).install(any(Artifact.class), any(Version.class), any(InstallOptions.class));

        doThrow(new IOException(ERROR_MESSAGE)).when(mockFacade).install(any(Artifact.class), any(Version.class), any(InstallOptions.class));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.argument("version", TEST_VERSION);
        commandInvoker.option("--step", 2);
        commandInvoker.option("--forceInstall", true);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, format("step 2 [FAIL]\n"
                                    + "{\n"
                                    + "  \"artifacts\" : [ {\n"
                                    + "    \"artifact\" : \"%s\",\n"
                                    + "    \"version\" : \"%s\",\n"
                                    + "    \"status\" : \"FAILURE\"\n"
                                    + "  } ],\n"
                                    + "  \"message\" : \"%s\",\n"
                                    + "  \"status\" : \"ERROR\"\n"
                                    + "}\n",
                                    TEST_ARTIFACT, TEST_VERSION, ERROR_MESSAGE));

        verify(mockFacade, times(1)).logSaasAnalyticsEvent(eventArgument.capture());

        List<Event> values = eventArgument.getAllValues();
        assertEquals(values.get(0).getType(), Event.Type.IM_ARTIFACT_INSTALL_FINISHED_UNSUCCESSFULLY);
        assertTrue(values.get(0).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, ERROR-MESSAGE=%s, TIME=\\d*}",
                                                                           TEST_ARTIFACT, TEST_VERSION, ERROR_MESSAGE)),
                   "Actual parameters: " + values.get(0).getParameters().toString());

    }

    @Test
    public void shouldInstallArtifactForceLastStep() throws Exception {
        ArgumentCaptor<Event> eventArgument = ArgumentCaptor.forClass(Event.class);

        InstallArtifactStepInfo info = mock(InstallArtifactStepInfo.class);
        doReturn(InstallArtifactInfo.Status.SUCCESS).when(info).getStatus();

        doReturn(info).when(mockFacade).getUpdateStepInfo(anyString());
        doReturn("id").when(mockFacade).install(any(Artifact.class), any(Version.class), any(InstallOptions.class));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("artifact", TEST_ARTIFACT);
        commandInvoker.argument("version", TEST_VERSION);
        commandInvoker.option("--step", INSTALL_INFO.size());
        commandInvoker.option("--forceInstall", true);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, format("step 3 [OK]\n" +
                                    "{\n" +
                                    "  \"artifacts\" : [ {\n" +
                                    "    \"artifact\" : \"%s\",\n" +
                                    "    \"version\" : \"%s\",\n" +
                                    "    \"status\" : \"SUCCESS\"\n" +
                                    "  } ],\n" +
                                    "  \"status\" : \"OK\"\n" +
                                    "}\n", TEST_ARTIFACT, TEST_VERSION));

        verify(mockFacade, times(1)).logSaasAnalyticsEvent(eventArgument.capture());

        List<Event> values = eventArgument.getAllValues();
        assertEquals(values.get(0).getType(), Event.Type.IM_ARTIFACT_INSTALL_FINISHED_SUCCESSFULLY);
        assertTrue(values.get(0).getParameters().toString().matches(format("\\{ARTIFACT=%s, VERSION=%s, TIME=\\d*}", TEST_ARTIFACT, TEST_VERSION)),
                   "Actual parameters: " + values.get(0).getParameters().toString());
    }

    @Test
    public void shouldReturnPathToUpdateInfoFile() {
        assertEquals(spyCommand.getPathToUpdateInfoFile().toString(),
                     format("%s/codenvy/update.info", System.getProperty("user.home")));
    }

    @Test
    public void shouldRemoveUpdateInfoFile() throws IOException {
        FileUtils.write(PATH_TO_TEST_UPDATE_INFO.toFile(), "update info");
        doReturn(PATH_TO_TEST_UPDATE_INFO).when(spyCommand).getPathToUpdateInfoFile();

        spyCommand.removeUpdateInfoFile();

        assertFalse(Files.exists(PATH_TO_TEST_UPDATE_INFO));
    }
}
