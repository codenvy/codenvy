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

import com.codenvy.client.Codenvy;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import static java.lang.String.format;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Deletes Factory command.
 * This command will delete a factory.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "delete-factory", description = "Delete a factory")
public class DeleteFactoryCommand extends AbsCommand {

    @Argument(name = "id", description = "Factory ID", required = true)
    private String factoryID;

    @Option(name = "--remote", description = "Name of the remote codenvy")
    private String remoteName;


    @Option(name = "-f", description = "No confirmation is required")
    private boolean confirmation;

    /**
     * Deletes the given factory
     */
    protected Object execute() throws IOException {
        init();

        // check remote
        if (!getSelectedRemote()) {
            return null;
        }


        Codenvy codenvy = getMultiRemoteCodenvy().getCodenvy(remoteName);
        if (codenvy == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(format("Please login into the remote %s", remoteName));
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // confirmation ?
        if (!confirmation) {
            String confirmLine = null;
            if (isInteractive()) {
                System.out.print("Confirm [y/N]");
                System.out.flush();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(session.getKeyboard(), Charset.defaultCharset()))) {
                    confirmLine = reader.readLine();
                }
                System.out.println(System.lineSeparator());
            } else {
                ConsoleReader consoleReader = new ConsoleReader(System.in, System.out);
                consoleReader.setExpandEvents(false);
                confirmLine = consoleReader.readLine("Confirm [y/N]");
            }

            if (!"y".equalsIgnoreCase(confirmLine)) {
                System.out.println("Cancelling action");
                return null;
            }
        }


        codenvy.factory().delete(factoryID).execute();
        System.out.println(String.format("The factory with ID %s has been successfully deleted", factoryID));

        return null;
    }



    protected boolean getSelectedRemote() {
        // no remote, use default
        if (remoteName == null) {
            remoteName = getMultiRemoteCodenvy().getDefaultRemoteName();
        } else {
            if (getMultiRemoteCodenvy().getRemote(remoteName) == null) {
                Ansi buffer = Ansi.ansi();
                buffer.fg(RED);
                buffer.a(format("The specified remote %s does not exists", remoteName));
                buffer.reset();
                System.out.println(buffer.toString());
                return false;
            }
        }
        return true;
    }



}
