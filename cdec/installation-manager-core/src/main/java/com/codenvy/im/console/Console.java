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

import com.codenvy.im.response.BasicResponse;
import com.codenvy.im.response.Response;
import com.codenvy.im.response.ResponseCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Singleton;
import jline.console.ConsoleReader;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonParseException;
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codenvy.im.utils.Commons.toJson;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

/** @author Dmytro Nochevnov */
@Singleton
public class Console {
    public static final  Ansi   ERASE_LINE_ABOVE   = ansi().a(ansi().cursorUp(1).eraseLine(Ansi.Erase.ALL));
    public static final  Ansi   ERASE_CURRENT_LINE = ansi().eraseLine(Ansi.Erase.ALL);
    public static final  String CODENVY_PREFIX     = "[CODENVY] ";
    public static final  String LIGHT_BLUE         = '\u001b' + "[94m";
    public static final  String LIGHT_YELLOW       = '\u001b' + "[93m";

    private static final Logger LOG                = Logger.getLogger(Console.class.getName());

    private static final AtomicReference<Console> consoleInstance = new AtomicReference<>();
    private final boolean       interactive;
    protected     ConsoleReader consoleReader;
    private final LoadingBar    loaderBar;
    private       int           rowCounter;


    protected Console(boolean interactive) throws IOException {
        this.interactive = interactive;
        this.consoleReader = new ConsoleReader();
        this.loaderBar = new LoadingBar();
    }

    /**
     * Returns console instance.
     * It has to be created first using {@link #create} method with appropriate mode.
     */
    public static Console getInstance() throws IllegalStateException {
        if (consoleInstance.get() == null) {
            throw new IllegalStateException("There is no console.");
        }
        return consoleInstance.get();
    }

    /**
     * Factory method to initialize console with appropriate mode.
     */
    public static Console create(boolean interactive) throws IOException {
        consoleInstance.set(new Console(interactive));
        return consoleInstance.get();
    }

    public void printlnWithoutPrompt(Object message) {
        print(message, true);
        println();
    }

    public void println(String message) {
        print(message);
        println();
    }

    public void println() {
        print(ansi().newline());
        rowCounter++;
    }

    public void print(String message) {
        print((Object)message, false);
    }

    public void printProgress(int percents) {
        printProgress(createProgressBar(percents));
    }

    public void printProgress(String message) {
        saveCursorPosition();
        printWithoutCodenvyPrompt(message);
        restoreCursorPosition();
    }

    public void cleanCurrentLine() {
        print(ERASE_CURRENT_LINE);
    }

    public void cleanLineAbove() {
        print(ERASE_LINE_ABOVE);
    }

    public void printWithoutCodenvyPrompt(String message) {
        print((Object)message, true);
    }

    public void printSuccess(String message) {
        printSuccess(message, false);
    }

    public void printSuccessWithoutCodenvyPrompt(String message) {
        printSuccess(message, true);
    }

    private void printSuccess(String message, boolean suppressCodenvyPrompt) {
        print(ansi().fg(GREEN).a(message).newline().reset(), suppressCodenvyPrompt);
    }

    /** @return "true" only if only user typed line equals "y". */
    public boolean askUser(String prompt) throws IOException {
        print(prompt + " [y/N] ");
        String userAnswer = readLine();
        return userAnswer != null && userAnswer.equalsIgnoreCase("y");
    }

    /** @return line typed by user */
    public String readLine() throws IOException {
        return doReadLine(null);
    }

    public String readPassword() throws IOException {
        return doReadLine('*');
    }

    public void pressAnyKey(String prompt) throws IOException {
        print(prompt);
        consoleReader.readCharacter();
    }

    public void reset() throws IllegalStateException {
        try {
            consoleReader.getTerminal().restore();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void printErrorAndExit(Exception ex) throws JsonProcessingException, JsonParseException {
        LOG.log(Level.SEVERE, ex.getMessage(), ex);

        BasicResponse errorResponse = BasicResponse.error(ex.getMessage());
        printResponseExitInError(errorResponse);
    }

    public void printResponseExitInError(Response response) throws JsonParseException, JsonProcessingException {
        if (response.getStatus() == ResponseCode.OK) {
            println(toJson(response));
        } else {
            printErrorAndExit(toJson(response));
        }
    }

    /**
     * Print error message and exit with status = 1 if the command is not executing in interactive mode.
     */
    public void printErrorAndExit(String message) {
        LOG.log(Level.SEVERE, message);

        printError(message, false);

        if (!interactive) {
            exit(1);
        }
    }

    public void printError(String message, boolean suppressCodenvyPrompt) {
        print(ansi().fg(RED).a(message).newline().reset(), suppressCodenvyPrompt);
    }


    public void exit(int status) {
        reset();
        System.exit(status); // NOSONAR
    }

    public void showProgressor() {
        loaderBar.show();
    }

    public void restoreProgressor() {
        loaderBar.restore();
    }

    public void hideProgressor() {
        loaderBar.hide();
    }

    private void saveCursorPosition() {
        print(ansi().saveCursorPosition());
        rowCounter = 0;
    }

    private void restoreCursorPosition() {
        print(ansi().restorCursorPosition());

        for (; rowCounter > 0; rowCounter--) {
            cleanCurrentLine();
            print(ansi().cursorUp(1));
        }
    }

    private String createProgressBar(int percent) {
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < 50; i++) {
            if (i < (percent / 2)) {
                bar.append("=");
            } else if (i == (percent / 2)) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }

        bar.append("]   ").append(percent).append("%     ");
        return bar.toString();
    }

    private void print(Object o) {
        System.out.print(o);
        System.out.flush();
    }

    private void print(Object o, boolean suppressCodenvyPrompt) {
        if (!interactive && !suppressCodenvyPrompt) {
            printCodenvyPrompt();
        }

        print(o);
    }

    private void printCodenvyPrompt() {
        print(ansi().a(LIGHT_BLUE + CODENVY_PREFIX).reset()); // light blue
    }

    private String doReadLine(@Nullable Character mask) throws IOException {
        rowCounter++;
        return consoleReader.readLine(mask);
    }

    public void printWarning(String message, boolean suppressCodenvyPrompt) {
        print(ansi().a(LIGHT_YELLOW + message).newline().reset(), suppressCodenvyPrompt);
    }

    protected class LoadingBar {
        private Visualizer visualizer;

        public void show() {
            if (visualizer != null && visualizer.isAlive()) {
                return;
            }

            visualizer = new Visualizer();
            visualizer.start();
        }

        public void hide() {
            if (visualizer != null && visualizer.isAlive()) {
                visualizer.interrupt();

                visualizer = null;
                printProgress(" ");  // remove last loader symbol
                saveCursorPosition();
            }
        }

        public void restore() {
            restoreCursorPosition();
            show();
        }

        /** Printing progress thread */
        protected class Visualizer extends Thread {
            protected final int    CHAR_CHANGE_TIMEOUT_MILLIS = 250;
            protected final char[] LOADER_CHARS               = {'-', '\\', '|', '/'};

            @Override
            public void run() throws IllegalStateException {
                int step = 0;
                while (!isInterrupted()) {
                    printProgress(String.valueOf(LOADER_CHARS[step]));
                    try {
                        sleep(CHAR_CHANGE_TIMEOUT_MILLIS);
                    } catch (InterruptedException e) {
                        break;
                    }

                    step++;
                    if (step == LOADER_CHARS.length) {
                        step = 0;
                    }
                }
            }
        }
    }
}
