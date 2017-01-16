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

import com.codenvy.cli.command.builtin.model.UserBuilderStatus;
import com.codenvy.cli.command.builtin.model.UserProject;
import com.codenvy.cli.command.builtin.model.UserRunnerStatus;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import java.util.List;

import static com.codenvy.cli.command.builtin.MultiRemoteCodenvy.checkOnlyOne;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Allows to list all processes or processes of a given project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "info", description = "Display information for a project, runner, or builder")
public class InfoCommand extends AbsCommand {


    @Argument(name = "id", description = "Specify the project/runner/builder ID", required = false, multiValued = false)
    private String id;

    protected Object execute() throws Exception {
        init();

        // not logged in
        if (!checkifEnabledRemotes()) {
            return null;
        }

        // do we have the ID ?
        if (id == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No ID has been defined. It should be a project, runner or builder ID");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // processId is beginning with a r --> runner ID
        if (id.startsWith("r")) {
            displayRunner();
        } else if (id.startsWith("b")) {
            displayBuilder();
        } else if (id.startsWith("p")) {
            displayProject();
        } else {
            // invalid id
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("Invalid identifier");
            buffer.fg(Ansi.Color.DEFAULT);
            System.out.println(buffer.toString());
        }

        return null;

    }


    protected void displayRunner() {
        List<UserRunnerStatus> matchingStatuses = getMultiRemoteCodenvy().findRunners(id);
        UserRunnerStatus foundStatus = checkOnlyOne(matchingStatuses, id, "runner", "runners");

        // not found, errors already printed
        if (foundStatus == null) {
            return;
        }

        System.out.println(foundStatus);

    }

    protected void displayBuilder() {
        List<UserBuilderStatus> matchingStatuses = getMultiRemoteCodenvy().findBuilders(id);
        UserBuilderStatus foundStatus = checkOnlyOne(matchingStatuses, id, "builder", "builders");

        // not found, errors already printed
        if (foundStatus == null) {
            return;
        }

        System.out.println(foundStatus);

    }


    protected void displayProject() {
        UserProject foundProject = getMultiRemoteCodenvy().getProject(id);

        // not found, errors already printed
        if (foundProject == null) {
            return;
        }

        System.out.println(foundProject);

    }
}

