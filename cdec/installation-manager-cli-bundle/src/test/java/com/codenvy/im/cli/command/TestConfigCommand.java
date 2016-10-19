/*
 *  [2012] - [2016] Codenvy, S.A.
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
import com.codenvy.im.managers.Config;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static java.lang.String.format;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
public class TestConfigCommand extends AbstractTestCommand {
    private AbstractIMCommand spyCommand;

    @BeforeMethod
    public void initMocks() throws IOException {
        spyCommand = spy(new ConfigCommand());
        performBaseMocks(spyCommand, true);
    }

    @Test
    public void testGetCdecConfig() throws Exception {
        final ImmutableMap<String, String> unsorted = ImmutableMap.of("prop2", "value2",
                                                                      "prop1", "value1");
        doReturn(unsorted).when(mockFacade).getArtifactConfig(createArtifact(CDECArtifact.NAME));

        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "prop1=value1\n"
                             + "prop2=value2\n");
    }

    @Test
    public void testGetInstallationManagerConfig() throws Exception {
        final ImmutableMap<String, String> unsorted = ImmutableMap.of("prop2", "value2",
                                                                      "prop1", "value1");
        doReturn(unsorted).when(mockFacade).getArtifactConfig(createArtifact(InstallManagerArtifact.NAME));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--im-cli", Boolean.TRUE);
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "prop1=value1\n"
                             + "prop2=value2\n");
    }

    @Test
    public void testGetCdecConfigProperty() throws Exception {
        final ImmutableMap<String, String> properties = ImmutableMap.of("prop1", "value1",
                                                                        "prop2", "value2");
        doReturn(properties).when(mockFacade).getArtifactConfig(createArtifact(CDECArtifact.NAME));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("property", "prop2");
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "prop2=value2\n");
    }

    @Test
    public void shouldDisplayErrorOnGettingCdecConfigProperty() throws Exception {
        doThrow(new IOException("IO Error")).when(mockFacade).getArtifactConfig(createArtifact(CDECArtifact.NAME));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("property", "prop1");
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "{\n"
                             + "  \"message\" : \"IO Error\",\n"
                             + "  \"status\" : \"ERROR\"\n"
                             + "}\n");
    }

    @Test
    public void shouldDisplayErrorOnGettingNonexistsCdecConfigProperty() throws Exception {
        final ImmutableMap<String, String> properties = ImmutableMap.of("prop1", "value1");
        doReturn(properties).when(mockFacade).getArtifactConfig(createArtifact(CDECArtifact.NAME));

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("property", "non-exists");
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "{\n"
                             + "  \"message\" : \"Property 'non-exists' not found\",\n"
                             + "  \"status\" : \"ERROR\"\n"
                             + "}\n");
    }

    @Test
    public void testUpdateCdecConfigProperty() throws Exception {
        final String newValue = "new-value";
        final String propertyToUpdate = "prop2";

        final ImmutableMap<String, String> properties = ImmutableMap.of("prop1", "value1",
                                                                        propertyToUpdate, "value2");
        doReturn(properties).when(mockFacade).getArtifactConfig(createArtifact(CDECArtifact.NAME));

        String messageToConfirmUpdate = "Do you want to update Codenvy property '" + propertyToUpdate + "' with new value '" + newValue + "'?";
        doReturn(true).when(spyConsole).askUser(messageToConfirmUpdate);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("property", propertyToUpdate);
        commandInvoker.argument("value", newValue);
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "");

        verify(mockFacade).updateArtifactConfig(createArtifact(CDECArtifact.NAME),
                                                ImmutableMap.of(propertyToUpdate, newValue));

        verify(spyConsole).askUser(messageToConfirmUpdate);
    }

    @Test
    public void testUpdateCdecConfigPropertyOnEmptyValue() throws Exception {
        final String newValue = "";
        final String propertyToUpdate = "prop2";

        final ImmutableMap<String, String> properties = ImmutableMap.of("prop1", "value1",
                                                                        propertyToUpdate, "value2");
        doReturn(properties).when(mockFacade).getArtifactConfig(createArtifact(CDECArtifact.NAME));

        String messageToConfirmUpdate = "Do you want to update Codenvy property '" + propertyToUpdate + "' with new value '" + newValue + "'?";
        doReturn(true).when(spyConsole).askUser(messageToConfirmUpdate);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("property", propertyToUpdate);
        commandInvoker.argument("value", newValue);
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "");

        verify(mockFacade).updateArtifactConfig(createArtifact(CDECArtifact.NAME),
                                                ImmutableMap.of(propertyToUpdate, newValue));

        verify(spyConsole).askUser(messageToConfirmUpdate);
    }

    @Test
    public void testDiscardUpdatingCdecConfigProperty() throws Exception {
        final String newValue = "new-value";
        final String propertyToUpdate = "prop2";

        String messageToConfirmUpdate = "Do you want to update Codenvy property '" + propertyToUpdate + "' with new value '" + newValue + "'?";
        doReturn(false).when(spyConsole).askUser(messageToConfirmUpdate);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("property", propertyToUpdate);
        commandInvoker.argument("value", newValue);
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "");

        verify(mockFacade, never()).updateArtifactConfig(createArtifact(CDECArtifact.NAME),
                                                ImmutableMap.of(anyString(), anyString()));

        verify(mockFacade, never()).getArtifactConfig(any(Artifact.class));


        verify(spyConsole).askUser(messageToConfirmUpdate);
    }

    @Test
    public void shouldDisplayErrorOnUpdatingNonexistsCdecConfigProperty() throws Exception {
        final String newValue = "new-value";
        final String propertyToUpdate = "non-exists";

        final ImmutableMap<String, String> properties = ImmutableMap.of("prop1", "value1");
        doReturn(properties).when(mockFacade).getArtifactConfig(createArtifact(CDECArtifact.NAME));

        String messageToConfirmUpdate = "Do you want to update Codenvy property '" + propertyToUpdate + "' with new value '" + newValue + "'?";
        doReturn(true).when(spyConsole).askUser(messageToConfirmUpdate);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("property", propertyToUpdate);
        commandInvoker.argument("value", newValue);
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "{\n"
                             + "  \"message\" : \"Property 'non-exists' not found\",\n"
                             + "  \"status\" : \"ERROR\"\n"
                             + "}\n");

        verify(mockFacade, never()).updateArtifactConfig(createArtifact(CDECArtifact.NAME),
                                                         ImmutableMap.of(anyString(), anyString()));

        verify(spyConsole).askUser(messageToConfirmUpdate);
    }

    @Test
    public void shouldDisplayErrorOnUpdatingCdecConfigProperty() throws Exception {
        final String newValue = "new-value";
        final String propertyToUpdate = "prop1";

        final ImmutableMap<String, String> properties = ImmutableMap.of(propertyToUpdate, "value1");
        doReturn(properties).when(mockFacade).getArtifactConfig(createArtifact(CDECArtifact.NAME));

        doThrow(new IOException("IO Error")).when(mockFacade).updateArtifactConfig(createArtifact(CDECArtifact.NAME),
                                                                                ImmutableMap.of(propertyToUpdate, newValue));

        String messageToConfirmUpdate = "Do you want to update Codenvy property '" + propertyToUpdate + "' with new value '" + newValue + "'?";
        doReturn(true).when(spyConsole).askUser(messageToConfirmUpdate);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("property", propertyToUpdate);
        commandInvoker.argument("value", newValue);
        CommandInvoker.Result result = commandInvoker.invoke();

        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "{\n"
                             + "  \"message\" : \"IO Error\",\n"
                             + "  \"status\" : \"ERROR\"\n"
                             + "}\n");

        verify(spyConsole).askUser(messageToConfirmUpdate);
    }

    @Test
    public void testChangeCodenvyHostUrl() throws Exception {
        String testDns = "test.com";
        Map<String, String> properties = ImmutableMap.of(Config.HOST_URL, testDns);
        doNothing().when(mockFacade).updateArtifactConfig(createArtifact(CDECArtifact.NAME), properties);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--hostname", testDns);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, "{\n" +
                             "  \"status\" : \"OK\"\n" +
                             "}\n");
    }

    @Test
    public void testChangeCodenvyHostUrlWhenServiceThrowsError() throws Exception {
        String testDns = "test.com";
        Map<String, String> properties = ImmutableMap.of(Config.HOST_URL, testDns);
        String expectedOutput = "{\n"
                                + "  \"message\" : \"Server Error Exception\",\n"
                                + "  \"status\" : \"ERROR\"\n"
                                + "}";
        doThrow(new RuntimeException("Server Error Exception"))
            .when(mockFacade).updateArtifactConfig(createArtifact(CDECArtifact.NAME), properties);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--hostname", testDns);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, expectedOutput + "\n");
    }
}
