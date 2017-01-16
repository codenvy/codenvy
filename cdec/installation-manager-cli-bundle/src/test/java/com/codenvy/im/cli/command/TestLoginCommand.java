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

import com.codenvy.im.cli.preferences.CodenvyOnpremPreferences;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static java.lang.String.format;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov */
public class TestLoginCommand extends AbstractTestCommand {
    private LoginCommand spyCommand;

    public static final String ANOTHER_CODENVY_ONPREM_URL = "https://test.codenvy.onprem";

    @BeforeMethod
    public void initMocks() throws IOException {
        spyCommand = spy(new LoginCommand());
        performBaseMocks(spyCommand, true);

        doReturn(CODENVY_ONPREM_URL).when(mockConfigManager).getHostUrl();
    }

    @Test
    public void testLogin() throws Exception {
        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("username", CODENVY_ONPREM_USER);
        commandInvoker.argument("password", CODENVY_ONPREM_USER_PASSWORD);

        doReturn(true).when(mockMultiRemoteCodenvy).login(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME, CODENVY_ONPREM_USER, CODENVY_ONPREM_USER_PASSWORD);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, format("Login success on '%s'.\n", CODENVY_ONPREM_URL));

        verify(mockConfigManager).getHostUrl();
        verify(mockPreferences).setUrl(CODENVY_ONPREM_URL);
    }

    @Test
    public void testLoginFailed() throws Exception {
        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("username", CODENVY_ONPREM_USER);
        commandInvoker.argument("password", CODENVY_ONPREM_USER_PASSWORD);

        // simulate fail login
        doReturn(false).when(mockMultiRemoteCodenvy).login(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME, CODENVY_ONPREM_USER, CODENVY_ONPREM_USER_PASSWORD);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, format("Login failed on '%s'.\n", CODENVY_ONPREM_URL));

        verify(mockConfigManager).getHostUrl();
        verify(mockPreferences).setUrl(CODENVY_ONPREM_URL);
    }

    @Test
    public void testLoginWhenServiceThrowsError() throws Exception {
        String expectedOutput = "{\n"
                                + "  \"message\" : \"Server Error Exception\",\n"
                                + "  \"status\" : \"ERROR\"\n"
                                + "}";
        doThrow(new RuntimeException("Server Error Exception")).when(mockConfigManager).getHostUrl();

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, expectedOutput + "\n");

        verify(mockPreferences, never()).setUrl(anyString());
        verify(mockMultiRemoteCodenvy, never()).login(anyString(), anyString(), anyString());
    }

    @Test
    public void testLoginToSpecificUrl() throws Exception {
        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.option("--url", ANOTHER_CODENVY_ONPREM_URL);
        commandInvoker.argument("username", CODENVY_ONPREM_USER);
        commandInvoker.argument("password", CODENVY_ONPREM_USER_PASSWORD);

        doReturn(true).when(mockMultiRemoteCodenvy).login(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME, CODENVY_ONPREM_USER, CODENVY_ONPREM_USER_PASSWORD);
        doReturn(ANOTHER_CODENVY_ONPREM_URL).when(mockPreferences).getUrl();

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, String.format("Login success on '%s'.\n",
                                           ANOTHER_CODENVY_ONPREM_URL));

        verify(mockPreferences).setUrl(ANOTHER_CODENVY_ONPREM_URL);
        verify(mockConfigManager, never()).getHostUrl();
    }

}
