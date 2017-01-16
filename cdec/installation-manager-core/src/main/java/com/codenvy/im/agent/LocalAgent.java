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

import com.codenvy.im.commands.decorators.PuppetErrorInterrupter;
import com.codenvy.im.console.Console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codenvy.im.agent.AgentException.create;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
public class LocalAgent extends AbstractAgent {
    private static final Logger LOG = Logger.getLogger(LocalAgent.class.getSimpleName());

    protected final static String READ_PASSWORD_PROMPT              = String.format("[sudo] password for %s: ", System.getProperty("user.name"));
    protected static final String CHECK_PASSWORD_NECESSITY_COMMAND  = "ERROR=$(sudo -n true 2>&1); if [[ $? != 0 ]] ; then echo $ERROR; fi";
    protected static final String CHECK_IS_PASSWORD_CORRECT_COMMAND = "sudo -S true";
    protected static final String PASSWORD_INCORRECT_STATUS         = "Sorry, try again.";
    protected static final String NEED_PASSWORD_MESSAGE             = "sudo: a password is required";

    private static char[] pwdCache = null;

    public LocalAgent() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(String command) throws AgentException {
        if (isPasswordInputRequired(command)) {
            return executeWithPassword(command);
        } else {
            return executeWithoutPassword(command);
        }
    }

    private String executeWithPassword(String command) throws AgentException {
        // workaround to issue CDEC-460 where there was sudo password prompt thrown from IM in time of installing Codenvy
        if (PuppetErrorInterrupter.isReadPuppetLogCommand(command)) {
            LOG.log(Level.WARNING, "It is impossible to read puppet log: sudo right is required");    // ignore to don't interrupt installation
            return "";
        }

        if (pwdCache == null) {
            pwdCache = obtainPassword();
        }

        try {
            String commandWhichIsReadPasswordFromInput =
                    command.replace("sudo ", "sudo -S ");  // make sudo be able to read password from input stream
            Process process = getProcess(commandWhichIsReadPasswordFromInput);
            passPasswordToProcess(process, pwdCache);
            int exitStatus = process.waitFor();

            InputStream in = process.getInputStream();
            InputStream err = process.getErrorStream();

            return processOutput(command, exitStatus, in, err);
        } catch (Exception e) {
            String errMessage = String.format("Can't execute command '%s'.", command);
            throw create(errMessage, e);
        }
    }

    private String executeWithoutPassword(String command) throws AgentException {
        try {
            Process process = getProcess(command);

            int exitStatus = process.waitFor();
            InputStream in = process.getInputStream();
            InputStream err = process.getErrorStream();

            return processOutput(command, exitStatus, in, err);
        } catch (Exception e) {
            String errMessage = String.format("Can't execute command '%s'.", command);
            throw create(errMessage, e);
        }
    }

    protected void passPasswordToProcess(Process process, char[] pwd) throws UnsupportedEncodingException {
        OutputStreamWriter osw = new OutputStreamWriter(process.getOutputStream(), "UTF-8");
        final PrintWriter stdIn = new PrintWriter(osw);

        for (char ch : pwd) {
            stdIn.append(ch);
        }

        stdIn.append('\n');
        stdIn.flush();
    }

    protected boolean isPasswordInputRequired(String command) {
        if (!command.contains("sudo ")) {
            return false;
        }

        // REST request then
        if (!isConsoleAccessible()) {
            return false;
        }

        try {
            String result = executeWithoutPassword(CHECK_PASSWORD_NECESSITY_COMMAND);

            if ((result != null) && result.contains(NEED_PASSWORD_MESSAGE)) {
                LOG.log(Level.WARNING, String.format("Command '%s' requires sudo password", command));
                return true;
            }

            return false;

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Can't verify requirement of sudo password", e);
            return false;
        }
    }

    protected char[] obtainPassword() throws AgentException, IllegalStateException {
        Console console = getConsole();

        try {
            console.hideProgressor();
            console.println();

            for (int i = 0; i < 3; i++) {
                console.print(READ_PASSWORD_PROMPT);
                Object answer = console.readPassword();
                if (answer == null) {
                    console.println(PASSWORD_INCORRECT_STATUS);
                    continue;
                }

                char[] answerChar = ((String)answer).toCharArray();
                if (isPasswordCorrect(answerChar)) {
                    console.restoreProgressor();
                    return answerChar;
                }

                console.println(PASSWORD_INCORRECT_STATUS);
            }

            console.println("sudo: 3 incorrect password attempts");
            throw new RuntimeException();
        } catch (Exception e) {
            throw new AgentException("Can't obtain correct password", e);
        } finally {
            console.showProgressor();
        }
    }

    protected Process getProcess(String command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);
        return pb.start();
    }

    protected Console getConsole() throws AgentException {
        Console console;
        try {
            console = Console.getInstance();
        } catch (Exception | NoClassDefFoundError e) {
            throw new AgentException("Can't obtain correct password", e);
        }
        return console;
    }

    protected boolean isPasswordCorrect(char[] checkingPwd) throws Exception {
        Process process = getProcess(CHECK_IS_PASSWORD_CORRECT_COMMAND);
        passPasswordToProcess(process, checkingPwd);
        InputStream errorStream = process.getErrorStream();

        // Don't use process.waitFor() because the process is hang up in case of incorrect password.
        // Read error stream using errorStream.read(), because IOUtils.toString(bufferedReader) hang up in case of incorrect password.
        String errorMessage = "";
        for (; ; ) {
            int c = errorStream.read();
            if (c == -1) {    // check end of stream and exit here if there is no login error
                return true;
            }

            errorMessage += (char)c;
            if (errorMessage.contains(PASSWORD_INCORRECT_STATUS)) {
                process.destroy();
                return false;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    /**
     * @return true if org/fusesource/jansi/Ansi is found, or false otherwise
     */
    protected boolean isConsoleAccessible() {
        try {
            getConsole();
        } catch (AgentException e) {
            return false;
        }

        return true;
    }
}
