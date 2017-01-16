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
package com.codenvy.im.console;

import jline.console.ConsoleReader;

import com.codenvy.im.response.BasicResponse;

import org.eclipse.che.commons.json.JsonParseException;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiOutputStream;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static com.codenvy.im.utils.Commons.toJson;
import static java.lang.Thread.sleep;
import static org.fusesource.jansi.Ansi.ansi;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** @author Dmytro Nochevnov */
public class TestConsole {
    public static final String CODENVY_PREFIX = "[CODENVY] ";
    public static final String CODENVY_PREFIX_WITH_ANSI = "\u001B[94m" + CODENVY_PREFIX + "\u001B[m";

    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;

    PrintStream originOut = System.out;
    PrintStream originErr = System.err;

    @Mock
    ConsoleReader mockConsoleReader;

    @BeforeMethod
    public void setUp() {
        initMocks(this);

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

    @Test
    public void testPrintError() throws IOException {
        String testErrorMessage = "error";

        Console spyConsole = createNonInteractiveConsole();
        spyConsole.printError(testErrorMessage, true);
        assertEquals(getOutputContent(), "\u001B[31m" + testErrorMessage + "\n\u001B[m");
        verify(spyConsole, never()).exit(1);
    }

    @Test
    public void testPrintProgressPercent() throws IOException {
        String expectedResult = "[=====>                                            ]   10%     ";
        int testPercents = 10;

        Console spyConsole = createInteractiveConsole();
        spyConsole.printProgress(testPercents);
        assertEquals(removeAnsi(getOutputContent()), expectedResult);

        spyConsole = createNonInteractiveConsole();
        spyConsole.printProgress(testPercents);
        assertEquals(removeAnsi(getOutputContent()), expectedResult);
    }

    @Test
    public void testPrintProgressMessage() throws IOException {
        String testProgressMessage = "-\\|//";

        Console spyConsole = createInteractiveConsole();
        spyConsole.printProgress(testProgressMessage);
        assertEquals(removeAnsi(getOutputContent()), testProgressMessage);

        spyConsole = createNonInteractiveConsole();
        spyConsole.printProgress(testProgressMessage);
        assertEquals(removeAnsi(getOutputContent()), testProgressMessage);
    }

    @Test
    public void testCleanCurrentLine() throws IOException {
        Ansi expectedAnsi = ansi().eraseLine(Ansi.Erase.ALL);
        String initialContent = "string\n";

        Console spyConsole = createInteractiveConsole();
        outputStream.write(initialContent.getBytes());
        spyConsole.cleanCurrentLine();
        assertEquals(getOutputContent(), initialContent + expectedAnsi.toString());

        spyConsole = createNonInteractiveConsole();
        outputStream.write(initialContent.getBytes());
        spyConsole.cleanCurrentLine();
        assertEquals(getOutputContent(), initialContent + expectedAnsi.toString());
    }

    @Test
    public void testShowHideLoaderBar() throws IOException, InterruptedException {
        Console.LoadingBar.Visualizer loaderVisualizer = new Console(false).new LoadingBar().new Visualizer();
        long timeout = loaderVisualizer.CHAR_CHANGE_TIMEOUT_MILLIS * (loaderVisualizer.LOADER_CHARS.length + 2);

        Console spyConsole = createInteractiveConsole();
        spyConsole.showProgressor();
        sleep(timeout);
        spyConsole.hideProgressor();
        String actualOutput = getOutputContent();
        assertTrue(actualOutput.startsWith("\u001B[s-\u001B[u\u001B[s\\\u001B[u\u001B[s|\u001B[u\u001B[s/\u001B[u\u001B[s-\u001B[u\u001B[s"));
        assertTrue(actualOutput.endsWith(" \u001B[u\u001B[s"));

        spyConsole = createNonInteractiveConsole();
        spyConsole.showProgressor();
        sleep(timeout);
        spyConsole.hideProgressor();
        actualOutput = getOutputContent();
        assertTrue(actualOutput.startsWith("\u001B[s-\u001B[u\u001B[s\\\u001B[u\u001B[s|\u001B[u\u001B[s/\u001B[u\u001B[s-\u001B[u\u001B[s"));
        assertTrue(actualOutput.endsWith(" \u001B[u\u001B[s"));
    }

    @Test
    public void testShowHideRestoreHideLoaderBar() throws IOException, InterruptedException {
        Console.LoadingBar.Visualizer loaderVisualizer = new Console(false).new LoadingBar().new Visualizer();
        long timeout = loaderVisualizer.CHAR_CHANGE_TIMEOUT_MILLIS * (loaderVisualizer.LOADER_CHARS.length + 2);

        Console spyConsole = createInteractiveConsole();
        spyConsole.showProgressor();
        sleep(timeout);
        spyConsole.hideProgressor();
        spyConsole.println("test");
        sleep(timeout);
        spyConsole.restoreProgressor();
        sleep(timeout);
        spyConsole.hideProgressor();
        String actualOutput = getOutputContent();
        assertTrue(actualOutput.startsWith("\u001B[s-\u001B[u\u001B[s\\\u001B[u\u001B[s|\u001B[u\u001B[s/\u001B[u\u001B[s-\u001B[u\u001B[s"));
        assertTrue(actualOutput
                           .contains("\u001B[u\u001B[stest\n\u001B[u\u001B[s-\u001B[u\u001B[s\\\u001B[u\u001B[s|\u001B[u\u001B[s/\u001B[u\u001B[s-"));
        assertTrue(actualOutput.endsWith(" \u001B[u\u001B[s"));

        spyConsole = createNonInteractiveConsole();
        spyConsole.showProgressor();
        sleep(timeout);
        spyConsole.hideProgressor();
        spyConsole.println("test");
        sleep(timeout);
        spyConsole.restoreProgressor();
        sleep(timeout);
        spyConsole.hideProgressor();
        actualOutput = getOutputContent();
        assertTrue(actualOutput.startsWith("\u001B[s-\u001B[u\u001B[s\\\u001B[u\u001B[s|\u001B[u\u001B[s/\u001B[u\u001B[s-\u001B[u\u001B[s"));
        assertTrue(actualOutput.contains(
                "\u001B[u\u001B[s\u001B[94m[CODENVY] \u001B[mtest\n\u001B[u\u001B[s-\u001B[u\u001B[s\\\u001B[u\u001B[s|\u001B[u\u001B[s/\u001B[u" +
                "\u001B[s-"));
        assertTrue(actualOutput.endsWith(" \u001B[u\u001B[s"));
    }

    @Test
    public void testShowLoaderBarAfterShow() throws IOException, InterruptedException {
        String expectedOutputStart = "\u001B[s-\u001B[u\u001B[s\\\u001B[u\u001B[s|\u001B[u\u001B[s/\u001B[u\u001B[s-\u001B[u\u001B[s";
        String expectedOutputEnd = " \u001B[u\u001B[s";
        Console.LoadingBar.Visualizer loaderVisualizer = new Console(false).new LoadingBar().new Visualizer();
        long timeout = loaderVisualizer.CHAR_CHANGE_TIMEOUT_MILLIS * (loaderVisualizer.LOADER_CHARS.length + 2);

        Console spyConsole = createInteractiveConsole();
        spyConsole.showProgressor();
        spyConsole.showProgressor();
        sleep(timeout);
        spyConsole.showProgressor();
        spyConsole.hideProgressor();

        String actualOutput = getOutputContent();
        assertTrue(actualOutput.startsWith(expectedOutputStart));
        assertTrue(actualOutput.endsWith(expectedOutputEnd));
    }

    @Test
    public void testHideLoaderBarAfterHide() throws IOException, InterruptedException {
        String expectedOutputStart = "\u001B[s-\u001B[u\u001B[s\\\u001B[u\u001B[s|\u001B[u\u001B[s/\u001B[u\u001B[s-\u001B[u\u001B[s";
        String expectedEndOutputEnd = " \u001B[u\u001B[s";
        Console.LoadingBar.Visualizer loaderVisualizer = new Console(false).new LoadingBar().new Visualizer();
        long timeout = loaderVisualizer.CHAR_CHANGE_TIMEOUT_MILLIS * (loaderVisualizer.LOADER_CHARS.length + 2);

        Console spyConsole = createInteractiveConsole();
        spyConsole.showProgressor();
        sleep(timeout);
        spyConsole.hideProgressor();
        spyConsole.hideProgressor();

        String actualOutput = getOutputContent();
        assertTrue(actualOutput.startsWith(expectedOutputStart));
        assertTrue(actualOutput.endsWith(expectedEndOutputEnd));
    }

    @Test
    public void testCleanLineAbove() throws IOException {
        Ansi expectedAnsi = ansi().cursorUp(1).eraseLine(Ansi.Erase.ALL);
        String initialContent = "string\n";

        Console spyConsole = createInteractiveConsole();
        outputStream.write(initialContent.getBytes());
        spyConsole.cleanLineAbove();
        assertEquals(getOutputContent(), initialContent + expectedAnsi.toString());

        spyConsole = createNonInteractiveConsole();
        outputStream.write(initialContent.getBytes());
        spyConsole.cleanLineAbove();
        assertEquals(getOutputContent(), initialContent + expectedAnsi.toString());
    }

    @Test
    public void testPrint() throws IOException {
        String testMessage = "message";

        Console spyConsole = createInteractiveConsole();
        spyConsole.print(testMessage);
        assertEquals(getOutputContent(), testMessage);

        spyConsole = createNonInteractiveConsole();
        spyConsole.print(testMessage);
        assertEquals(getOutputContent(), CODENVY_PREFIX_WITH_ANSI + testMessage);

        spyConsole = createNonInteractiveConsole();
        spyConsole.printWithoutCodenvyPrompt(testMessage);
        assertEquals(getOutputContent(), testMessage);
    }

    @Test
    public void testPrintLn() throws IOException {
        String testMessage = "message";

        Console spyConsole = createInteractiveConsole();
        spyConsole.println(testMessage);
        assertEquals(getOutputContent(), testMessage + "\n");

        spyConsole = createNonInteractiveConsole();
        spyConsole.println(testMessage);
        assertEquals(getOutputContent(), CODENVY_PREFIX_WITH_ANSI + testMessage + "\n");
    }

    @Test
    public void testPrintSuccess() throws IOException {
        String testMessage = "message";

        Console spyConsole = createInteractiveConsole();
        spyConsole.printSuccess(testMessage);
        assertEquals(getOutputContent(), "\u001B[32m" + testMessage + "\n\u001B[m");

        spyConsole = createNonInteractiveConsole();
        spyConsole.printSuccess(testMessage);
        assertEquals(getOutputContent(), CODENVY_PREFIX_WITH_ANSI + "\u001B[32m" + testMessage + "\n\u001B[m");

        spyConsole = createNonInteractiveConsole();
        spyConsole.printSuccessWithoutCodenvyPrompt(testMessage);
        assertEquals(getOutputContent(), "\u001B[32m" + testMessage + "\n\u001B[m");
    }

    @Test
    public void testPrintOkResponse() throws JsonParseException, IOException {
        BasicResponse response = BasicResponse.ok();
        response.setMessage("test");

        Console spyConsole = createInteractiveConsole();
        spyConsole.printResponseExitInError(response);
        assertEquals(getOutputContent(), toJson(response) + "\n");

        spyConsole = createNonInteractiveConsole();
        spyConsole.printResponseExitInError(response);
        assertEquals(getOutputContent(), CODENVY_PREFIX_WITH_ANSI + toJson(response) + "\n");
    }

    @Test
    public void testPrintErrorResponse() throws JsonParseException, IOException {
        BasicResponse response = BasicResponse.error("error");

        Console spyConsole = createInteractiveConsole();
        spyConsole.printResponseExitInError(response);
        assertEquals(getOutputContent(), "\u001B[31m" + toJson(response) + "\n\u001B[m");
        verify(spyConsole, never()).exit(1);

        spyConsole = createNonInteractiveConsole();
        spyConsole.printResponseExitInError(response);
        assertEquals(getOutputContent(), CODENVY_PREFIX_WITH_ANSI + "\u001B[31m" + toJson(response) + "\n\u001B[m");
        verify(spyConsole).exit(1);
    }

    @Test
    public void testAskUserAgree() throws IOException {
        String testMessage = "ask";
        String userResponse = "y";

        Console spyConsole = createInteractiveConsole();
        doReturn(userResponse).when(mockConsoleReader).readLine(any(Character.class));

        assertTrue(spyConsole.askUser(testMessage));
        assertEquals(getOutputContent(), testMessage + " [y/N] ");
    }

    @Test
    public void testAskUserDisagree() throws IOException {
        String testMessage = "ask";
        String userResponse = "N";

        Console spyConsole = createInteractiveConsole();
        doReturn(userResponse).when(mockConsoleReader).readLine(any(Character.class));

        assertFalse(spyConsole.askUser(testMessage));
        assertEquals(getOutputContent(), testMessage + " [y/N] ");
    }


    @Test
    public void testReadLine() throws IOException {
        String testLine = "line";

        Console spyConsole = createInteractiveConsole();
        doReturn(testLine).when(mockConsoleReader).readLine(any(Character.class));

        assertEquals(spyConsole.readLine(), testLine);
        assertEquals(getOutputContent(), "");
    }

    @Test
    public void testReadPassword() throws IOException {
        String testPassword = "123";

        Console spyConsole = createInteractiveConsole();
        doReturn(testPassword).when(mockConsoleReader).readLine(any(Character.class));

        assertEquals(spyConsole.readPassword(), testPassword);
        assertEquals(getOutputContent(), "");
    }

    @Test
    public void testPrintErrorAndExit() throws IOException {
        String testErrorMessage = "error";

        Console spyConsole = createInteractiveConsole();
        spyConsole.printErrorAndExit(testErrorMessage);
        assertEquals(getOutputContent(), "\u001B[31m" + testErrorMessage + "\n\u001B[m");
        verify(spyConsole, never()).exit(1);

        spyConsole = createNonInteractiveConsole();
        spyConsole.printErrorAndExit(testErrorMessage);
        assertEquals(getOutputContent(), CODENVY_PREFIX_WITH_ANSI + "\u001B[31m" + testErrorMessage + "\n\u001B[m");
        verify(spyConsole).exit(1);
    }

    @Test
    public void testPrintException() throws Exception {
        Exception exception = new RuntimeException("runtime error");
        String expectedResult = "{\n"
                                + "  \"message\" : \"runtime error\",\n"
                                + "  \"status\" : \"ERROR\"\n"
                                + "}\n";

        Console spyConsole = createInteractiveConsole();
        spyConsole.printErrorAndExit(exception);
        assertEquals(getOutputContent(), "\u001B[31m" + expectedResult + "\u001B[m");
        verify(spyConsole, never()).exit(1);

        spyConsole = createNonInteractiveConsole();
        spyConsole.printErrorAndExit(exception);
        assertEquals(getOutputContent(), CODENVY_PREFIX_WITH_ANSI + "\u001B[31m" + expectedResult + "\u001B[m");
        verify(spyConsole).exit(1);
    }

    @Test
    public void testGetInstance() throws IOException {
        Console console = ConsoleTested.create(true);
        assertEquals(Console.getInstance(), console);
    }

    @Test
    public void testGetInstanceError() throws IOException {
        Console console = ConsoleTested.create(true);
        assertEquals(Console.getInstance(), console);
    }

    @Test
    public void testPrintWarning() throws IOException {
        String testMessage = "warning";

        Console spyConsole = createNonInteractiveConsole();
        spyConsole.printWarning(testMessage, true);
        assertEquals(getOutputContent(), "\u001B[93m" + testMessage + "\n\u001B[m");
    }

    private Console createInteractiveConsole() throws IOException {
        setUp();
        Console spyConsole = spy(Console.create(true));
        spyConsole.consoleReader = mockConsoleReader;
        return spyConsole;
    }

    private Console createNonInteractiveConsole() throws IOException {
        setUp();
        Console spyConsole = spy(Console.create(false));
        spyConsole.consoleReader = mockConsoleReader;
        doNothing().when(spyConsole)
                   .exit(anyInt());  // avoid error "The forked VM terminated without properly saying goodbye. VM crash or System.exit called?"
        return spyConsole;
    }

    private String getOutputContent() {
        return outputStream.toString();
    }

    private String removeAnsi(final String content) {
        if (content == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            AnsiOutputStream aos = new AnsiOutputStream(baos);
            aos.write(content.getBytes());
            aos.flush();
            return baos.toString();
        } catch (IOException e) {
            return content;
        }
    }

    public static class ConsoleTested extends Console {
        protected ConsoleTested(boolean interactive) throws IOException {
            super(interactive);
        }
    }
}
