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
package com.codenvy.im.agent;

import com.codenvy.im.console.Console;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import static org.mockito.Matchers.anyString;
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
public class TestLocalAgent {
    private static final String TEST_PASSWORD        = "test";
    public static final  String COMMAND_WITHOUT_SUDO = "sleep 1; ls;";
    public static final  String COMMAND_WITH_SUDO    = "sudo true";
    public static final  int    ERROR_EXIT_CODE      = 1;

    @Mock
    Console mockConsole;

    ProcessTested spyProcess;

    private ByteArrayOutputStream outputStreamOfSpyProcess;
    private ByteArrayInputStream  inputStreamOfSpyProcess;
    private ByteArrayInputStream  errorStreamOfSpyProcess;

    private String outputOfMockConsole;

    LocalAgent spyTestAgent;

    @BeforeMethod
    public void setupTestAgent() throws IOException, InterruptedException {
        outputStreamOfSpyProcess = new ByteArrayOutputStream();
        inputStreamOfSpyProcess = new ByteArrayInputStream(new byte[0]);
        errorStreamOfSpyProcess = new ByteArrayInputStream(new byte[0]);

        spyProcess = spy(new ProcessTested());
        spyTestAgent = spy(new LocalAgent());

        doReturn(spyProcess).when(spyTestAgent).getProcess(anyString());
        doReturn(mockConsole).when(spyTestAgent).getConsole();
        doReturn(true).when(spyTestAgent).isConsoleAccessible();
    }

    @BeforeMethod
    public void setupConsole() throws IOException {
        mockConsole = mock(Console.class);

        doNothing().when(mockConsole).showProgressor();
        doNothing().when(mockConsole).hideProgressor();
        doNothing().when(mockConsole).restoreProgressor();

        outputOfMockConsole = "";
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                outputOfMockConsole += "\n";
                return null;
            }
        }).when(mockConsole).println();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                outputOfMockConsole += invocation.getArguments()[0] + "\n";
                return null;
            }
        }).when(mockConsole).println(anyString());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                outputOfMockConsole += invocation.getArguments()[0];
                return null;
            }
        }).when(mockConsole).print(anyString());
    }

    @Test
    public void testSimpleCommandSuccess() throws Exception {
        Agent agent = new LocalAgent();
        assertNotNull(agent.execute(COMMAND_WITHOUT_SUDO));
    }

    @Test(expectedExceptions = AgentException.class,
            expectedExceptionsMessageRegExp = ".*Output: ; Error: ls: cannot access unExisted_file: No such file or directory.")
    public void testSimpleCommandError() throws Exception {
        Agent agent = new LocalAgent();
        agent.execute("ls unExisted_file");
    }

    @Test(expectedExceptions = AgentException.class,
            expectedExceptionsMessageRegExp = "Can't obtain correct password")
    public void testSudoCommandRequiredPasswordWithoutConsole() throws Exception {
        inputStreamOfSpyProcess = new ByteArrayInputStream(LocalAgent.NEED_PASSWORD_MESSAGE.getBytes());
        spyTestAgent.execute("sudo true;");
    }

    @Test
    public void testSudoCommandWithPassingPassword() throws Exception {
        String expectedConsoleOutput = "\n[sudo] password for " + System.getProperty("user.name") + ": ";

        doReturn(TEST_PASSWORD).when(mockConsole).readPassword();
        inputStreamOfSpyProcess = new ByteArrayInputStream(LocalAgent.NEED_PASSWORD_MESSAGE.getBytes());
        spyTestAgent.execute("sudo true;");
        assertEquals(outputOfMockConsole, expectedConsoleOutput);
    }

    @Test
    public void testSudoCommandWithPassingPasswordWhenConsoleInaccessible() throws Exception {
        doReturn(false).when(spyTestAgent).isConsoleAccessible();
        spyTestAgent.execute("sudo true;");
        assertEquals(outputOfMockConsole, "");
    }

    @Test(expectedExceptions = AgentException.class,
            expectedExceptionsMessageRegExp = "Can't execute command '" + COMMAND_WITH_SUDO + "'. Output: mock output; Error: mock error.")
    public void testSudoCommandWithPassingPasswordError() throws Exception {
        doReturn(TEST_PASSWORD.toCharArray()).when(spyTestAgent).obtainPassword();
        doReturn(true).when(spyTestAgent).isPasswordInputRequired(COMMAND_WITH_SUDO);
        doReturn(ERROR_EXIT_CODE).when(spyProcess).waitFor();
        inputStreamOfSpyProcess = new ByteArrayInputStream("mock output".getBytes());
        errorStreamOfSpyProcess = new ByteArrayInputStream("mock error".getBytes());
        spyTestAgent.execute(COMMAND_WITH_SUDO);
    }

    @Test
    public void testIsPasswordRequiredOnCommandWithoutSudo() throws IOException {
        assertFalse(spyTestAgent.isPasswordInputRequired(COMMAND_WITHOUT_SUDO));
        verify(spyTestAgent, never()).getProcess(LocalAgent.CHECK_PASSWORD_NECESSITY_COMMAND);
    }

    @Test
    public void testIsPasswordRequiredOnCommandWithSudoWhenProcessReturnsEmptyResult() throws IOException {
        assertFalse(spyTestAgent.isPasswordInputRequired(COMMAND_WITH_SUDO));
        verify(spyTestAgent).getProcess(LocalAgent.CHECK_PASSWORD_NECESSITY_COMMAND);
    }

    @Test
    public void testIsPasswordRequiredOnCommandWithSudoWhenProcessReturnsCorrectStatus() throws IOException {
        inputStreamOfSpyProcess = new ByteArrayInputStream(LocalAgent.NEED_PASSWORD_MESSAGE.getBytes());
        assertTrue(spyTestAgent.isPasswordInputRequired(COMMAND_WITH_SUDO));
        verify(spyTestAgent).getProcess(LocalAgent.CHECK_PASSWORD_NECESSITY_COMMAND);
    }

    @Test
    public void testIsPasswordRequiredOnCommandWithSudoWhenProcessThrowsException() throws InterruptedException, IOException {
        doThrow(InterruptedException.class).when(spyProcess).waitFor();
        assertFalse(spyTestAgent.isPasswordInputRequired(COMMAND_WITH_SUDO));
        verify(spyTestAgent).getProcess(LocalAgent.CHECK_PASSWORD_NECESSITY_COMMAND);
    }

    @Test
    public void testPassPasswordToProcess() throws InterruptedException, UnsupportedEncodingException {
        spyTestAgent.passPasswordToProcess(spyProcess, TEST_PASSWORD.toCharArray());
        assertEquals(outputStreamOfSpyProcess.toString(), TEST_PASSWORD + "\n");
    }

    @Test
    public void testIsPasswordCorrectTrue() throws Exception {
        errorStreamOfSpyProcess = new ByteArrayInputStream(new byte[]{-1});
        assertTrue(spyTestAgent.isPasswordCorrect(TEST_PASSWORD.toCharArray()));
        verify(spyTestAgent).getProcess(LocalAgent.CHECK_IS_PASSWORD_CORRECT_COMMAND);
    }

    @Test
    public void testIsPasswordCorrectFalse() throws Exception {
        errorStreamOfSpyProcess = new ByteArrayInputStream(LocalAgent.PASSWORD_INCORRECT_STATUS.getBytes());
        assertFalse(spyTestAgent.isPasswordCorrect(TEST_PASSWORD.toCharArray()));
        verify(spyTestAgent).getProcess(LocalAgent.CHECK_IS_PASSWORD_CORRECT_COMMAND);
    }

    @Test
    public void testGetProcess() throws IOException {
        LocalAgent agent = new LocalAgent();
        assertNotNull(agent.getProcess(COMMAND_WITHOUT_SUDO));
    }

    @Test
    public void testObtainPassword() throws Exception {
        String expectedConsoleOutput = "\n[sudo] password for " + System.getProperty("user.name") + ": ";
        doReturn(TEST_PASSWORD).when(mockConsole).readPassword();

        assertEquals(new String(spyTestAgent.obtainPassword()), TEST_PASSWORD);
        assertEquals(outputOfMockConsole, expectedConsoleOutput);
        verify(spyTestAgent).isPasswordCorrect(TEST_PASSWORD.toCharArray());
        verify(mockConsole).hideProgressor();
        verify(mockConsole).showProgressor();
        verify(mockConsole).restoreProgressor();
    }

    @Test
    public void testObtainPasswordCorrectOnSecondTry() throws Exception {
        String expectedConsoleOutput = String.format("\n[sudo] password for %1$s: " +
                                                     "\nSorry, try again." +
                                                     "\n[sudo] password for %1$s: " +
                                                     "\n", System.getProperty("user.name"));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                outputOfMockConsole += "\n";
                return TEST_PASSWORD;
            }
        }).when(mockConsole).readPassword();

        errorStreamOfSpyProcess = new ByteArrayInputStream(LocalAgent.PASSWORD_INCORRECT_STATUS.getBytes());

        assertEquals(new String(spyTestAgent.obtainPassword()), TEST_PASSWORD);
        assertEquals(outputOfMockConsole, expectedConsoleOutput);
        verify(spyTestAgent, times(2)).isPasswordCorrect(TEST_PASSWORD.toCharArray());
        verify(mockConsole).hideProgressor();
        verify(mockConsole).showProgressor();
        verify(mockConsole).restoreProgressor();
    }

    @Test
    public void testFailObtainPasswordCorrectOnSecondTry() throws Exception {
        String expectedConsoleOutput = String.format("\n[sudo] password for %1$s: " +
                                                     "\nSorry, try again." +
                                                     "\n[sudo] password for %1$s: " +
                                                     "\nSorry, try again." +
                                                     "\n[sudo] password for %1$s: " +
                                                     "\nSorry, try again." +
                                                     "\nsudo: 3 incorrect password attempts\n", System.getProperty("user.name"));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                outputOfMockConsole += "\n";
                return TEST_PASSWORD;
            }
        }).when(mockConsole).readPassword();

        doReturn(false).when(spyTestAgent).isPasswordCorrect(TEST_PASSWORD.toCharArray());

        try {
            spyTestAgent.obtainPassword();
        } catch (AgentException e) {
            assertEquals(e.getMessage(), "Can't obtain correct password");

            assertEquals(outputOfMockConsole, expectedConsoleOutput);
            verify(spyTestAgent, times(3)).isPasswordCorrect(TEST_PASSWORD.toCharArray());
            verify(mockConsole).hideProgressor();
            verify(mockConsole).showProgressor();
            verify(mockConsole, never()).restoreProgressor();
            return;
        }

        fail("Here should be AgentException.");
    }

    @Test
    public void testIsConsoleAccessible() throws AgentException {
        LocalAgent testAgent = spy(new LocalAgent());
        doReturn(null).when(testAgent).getConsole();
        assertTrue(testAgent.isConsoleAccessible());   // mockConsole is accessible because of mockConsole returning by spyTestAgent.getConsole() method
    }

    @Test
    public void testIsConsoleInAccessible() throws AgentException {
        LocalAgent testAgent = spy(new LocalAgent());
        doThrow(new AgentException("error")).when(testAgent).getConsole();
        assertFalse(testAgent.isConsoleAccessible());
    }

    @Test(dataProvider = "getCommandErrorOutput")
    public void shouldIgnoreCertainErrors(String command, String errorOutput) throws Exception {
        doReturn(ImmutableMap.of("command 1", Pattern.compile("^error 1.*"),
                                 "command 2", Pattern.compile("^error 2.*")))
            .when(spyTestAgent).getIgnoringErrors();
    
        doReturn(ERROR_EXIT_CODE).when(spyProcess).waitFor();

        doReturn(false).when(spyTestAgent).isPasswordInputRequired(command);

    	final String TEST_OUTPUT = "command output";
        inputStreamOfSpyProcess = new ByteArrayInputStream(TEST_OUTPUT.getBytes());

        errorStreamOfSpyProcess = new ByteArrayInputStream(errorOutput.getBytes());
        
        try {
            String output = spyTestAgent.execute(command);
            assertEquals(output, TEST_OUTPUT);
        } catch (Exception e) {
        	fail("Unexpected exception: " + e.getMessage());
        }
    }
    
    @DataProvider
    public Object[][] getCommandErrorOutput() {
        return new Object[][]{
            {"command 1", "error 1 "},
            {"command 2", "error 2 "},
        };
    }

    class ProcessTested extends Process {
        @Override
        public OutputStream getOutputStream() {
            return outputStreamOfSpyProcess;
        }

        @Override
        public InputStream getInputStream() {
            return inputStreamOfSpyProcess;
        }

        @Override
        public InputStream getErrorStream() {
            return errorStreamOfSpyProcess;
        }

        @Override
        public int waitFor() throws InterruptedException {
            return 0;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {
        }
    }
}
