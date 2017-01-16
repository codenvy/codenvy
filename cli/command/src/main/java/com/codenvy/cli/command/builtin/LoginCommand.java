/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.command.builtin;

import jline.console.ConsoleReader;

import com.codenvy.client.auth.Token;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import static java.lang.String.format;

/**
 * Allow to login in the default remote or a given remote
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "login", description = "Login to a remote Codenvy cloud")
public class LoginCommand extends AbsCommand {

    @Option(name = "--remote", description = "Name of the remote codenvy", required = false)
    private String remoteName;

    @Argument(name = "username", description = "username of the remote instance", required = false, multiValued = false, index = 0)
    private String username;

    @Argument(name = "password", description = "password of the remote instance", required = false, multiValued = false, index = 1)
    private String password;

    @Option(name = "--token", description = "Token of the remote codenvy", required = false)
    private String tokenValue;

    @Override
    protected Object execute() throws Exception {

        init();

        // Is there any available remote ?
        if (!checkifAvailableRemotes()) {
            return null;
        }

        // no username and no password, needs to prompt
        if (username == null) {
            if (isInteractive()) {
                System.out.print("Username:");
                System.out.flush();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(session.getKeyboard(), Charset.defaultCharset()))) {
                    username = reader.readLine();
                }
                System.out.println(System.lineSeparator());
            } else {
                ConsoleReader consoleReader = new ConsoleReader(System.in, System.out);
                consoleReader.setExpandEvents(false);
                username = consoleReader.readLine("Username:");
            }
        }

        // login by token and not by password
        if (tokenValue != null) {
            Token token = getCodenvyClient().newTokenBuilder(tokenValue).build();
            if (getMultiRemoteCodenvy().login(remoteName, username, token)) {
                if (remoteName == null) {
                    System.out.println(format("Login success with given token on default remote '%s' [%s]", getMultiRemoteCodenvy().getDefaultRemoteName(),
                                              getMultiRemoteCodenvy().getDefaultRemote().getUrl()));
                } else {
                    System.out.println(format("Login success with given token on remote '%s' [%s]", remoteName,
                                              getMultiRemoteCodenvy().getRemote(remoteName).getUrl()));
                }
            } else {
                System.out.println("Login failed with given token: please check the credentials.");
            }

        } else {
            if (password == null) {
                if (isInteractive()) {
                    System.out.print("Password for " + username + ":");
                    System.out.flush();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(session.getKeyboard(), Charset.defaultCharset()))) {
                        password = reader.readLine();
                    }
                    System.out.println(System.lineSeparator());
                } else {
                    ConsoleReader consoleReader = new ConsoleReader(System.in, System.out);
                    consoleReader.setExpandEvents(false);
                    password = consoleReader.readLine(String.format("Password for %s:", username), Character.valueOf('*'));
                }
            }

            if (getMultiRemoteCodenvy().login(remoteName, username, password)) {
                if (remoteName == null) {
                    System.out.println(format("Login success on default remote '%s' [%s]", getMultiRemoteCodenvy().getDefaultRemoteName(),
                                              getMultiRemoteCodenvy().getDefaultRemote().getUrl()));
                } else {
                    System.out.println(format("Login success on remote '%s' [%s]", remoteName,
                                              getMultiRemoteCodenvy().getRemote(remoteName).getUrl()));
                }
            } else {
                System.out.println("Login failed: please check the credentials.");
            }
        }

        return null;
    }
}
