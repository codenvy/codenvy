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


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import java.util.Set;

/**
 * Allow to delegate to karaf help system
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "help", description = "Help")
public class HelpNonInteractiveCommand extends AbsCommand {

    public static final String COMMANDS = ".commands";

    @Argument(name = "command", description = "Help for the given command")
    private String command = null;

    /**
     * Display the help for the given command.
     * @return nothing
     */
    protected Object execute() throws Exception {
        init();

        if (command == null) {
            return session.execute("codenvy:");
        }

        // assume default shell is codenvy
        String defaultNamespace = "codenvy";

        if (!command.startsWith(defaultNamespace)) {
            command = defaultNamespace + ":" + command;
        }

        if (command.indexOf('|') > 0) {
            if (command.startsWith("command|")) {
                command = command.substring("command|".length());
            } else {
                return null;
            }
        }
        Set<String> names = (Set<String>) session.get(COMMANDS);
        if (names.contains(command)) {
            session.execute(command + " --help");
        } else {
            System.out.println(String.format("Command %s has not been found", command));
        }
        return null;
    }

}
