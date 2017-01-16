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


import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.impl.jline.Branding;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;
import static org.fusesource.jansi.Ansi.Color.CYAN;

/**
 * Defines a global help for Codenvy commands available.
 * This will be used when using for example ./codenvy without arguments as the name of this command is empty and is in the codenvy prefix.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "", description = "Help")
public class HelpCommand extends AbsCommand {


    protected String color(String name) {
        return Ansi.ansi().fg(CYAN).a(name).reset().toString();
    }

    /**
     * Display the current commands.
     * @return nothing
     */
    protected Object execute() {
        init();

        Ansi buffer = Ansi.ansi();

        // add the branding banner
        buffer.a(Branding.loadBrandingProperties().getProperty("banner"));

        // display commands
        buffer.a(INTENSITY_BOLD).a("COMMANDS").a(INTENSITY_BOLD_OFF).a("\n");

        String value = buildAsciiForm().withEntry(color("remote"), "Add or remove remote Codenvy cloud references")
                                       .withEntry(color("login"), "Login to a remote Codenvy cloud")
                                       .withEntry(color("logout"), "Logout to a remote Codenvy cloud")
                                       .withEntry(color("list"), "List workspaces, projects and processes")
                                       .withEntry(color("clone-local"), "Clone a remote Codenvy project to a local directory")
                                       .withEntry(color("build"), "Build a project")
                                       .withEntry(color("run"), "Run a project")
                                       .withEntry(color("logs"), "Display output logs for a runner or builder")
                                       .withEntry(color("info"), "Display information for a project, runner, or builder")
                                       .withEntry(color("open"), "Starts a browser session to access a project, builder or runner")
                                       .withEntry(color("stop"), "Stop one or more runner processes")
                                       .withEntry(color("create-project"), "Create a project")
                                       .withEntry(color("create-factory"), "Create a factory")
                                       .withEntry(color("delete-factory"), "Delete a factory")
                                       .withEntry(color("update-factory"), "Update a factory")
                                       .withEntry(color("list-factories"), "List factories")
                                       .withEntry(color("privacy"), "Change privacy for a project")
                                       .withEntry(color("delete-project"), "Delete a project")
                                       .withEntry(color("push"), "Push local project changes back to Codenvy")
                                       .withEntry(color("pull"), "Update project sync point directory created by clone-local")
                                       .alphabeticalSort().toAscii();
        buffer.a(value);

        // Display Remotes
        buffer.a("\n");
        buffer.a("\n");
        buffer.a(getMultiRemoteCodenvy().listRemotes());
        buffer.a("\n");
        buffer.a("To add a new remote, use 'remote add <remote-name> <URL>'");

        buffer.a("\n");
        buffer.a("Use '\u001B[1m[command] --help\u001B[0m' for help on a specific command.\r\n");
        System.out.println(buffer.toString());
        return null;
    }
}
