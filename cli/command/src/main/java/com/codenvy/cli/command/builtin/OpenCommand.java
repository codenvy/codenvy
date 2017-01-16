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
import com.codenvy.cli.command.builtin.model.UserProjectReference;
import com.codenvy.cli.command.builtin.model.UserRunnerStatus;
import com.codenvy.client.model.Link;
import com.codenvy.client.model.RunnerState;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import static com.codenvy.cli.command.builtin.MultiRemoteCodenvy.checkOnlyOne;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Open command.
 * This command will open browser on the IDE URL of a given project, or access URL or download URL
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "open", description = "Starts a browser session to access a project, builder or runner")
public class OpenCommand extends AbsCommand {

    @Argument(name = "id", description = "Specify the project/builder/runner ID to use", required = true, multiValued = false)
    private String id;


    /**
     * Prints the current projects per workspace
     */
    protected Object execute() {
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
            openRunner();
        } else if (id.startsWith("b")) {
            openBuilder();
        } else if (id.startsWith("p")) {
            openProject();
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



    protected void openProject() {
        UserProjectReference project = getMultiRemoteCodenvy().getProjectReference(id);
        if (project == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No matching project for identifier '").a(id).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return;
        }

        String ideURL = project.getInnerReference().ideUrl();
        openURL(ideURL);
    }

    protected void openRunner() {
        java.util.List<UserRunnerStatus> matchingStatuses = getMultiRemoteCodenvy().findRunners(id);
        UserRunnerStatus foundStatus = checkOnlyOne(matchingStatuses, id, "runner", "runners");

        // not found, errors already printed
        if (foundStatus == null) {
            return;
        }

        if (foundStatus.getInnerStatus().status() != RunnerState.RUNNING) {
            System.out.println("The runner is not running. Open command is only available for a runner which is currently running");
            return;
        }

        Link link = foundStatus.getInnerStatus().getWebLink();
        if (link != null) {
            openURL(link.href());
        } else {
            System.out.println("No available link for this runner");
        }


    }

    protected void openBuilder() {
        java.util.List<UserBuilderStatus> matchingStatuses = getMultiRemoteCodenvy().findBuilders(id);
        UserBuilderStatus foundStatus = checkOnlyOne(matchingStatuses, id, "builder", "builders");

        // not found, errors already printed
        if (foundStatus == null) {
            return;
        }

        String downloadString = "download result";
        Link downloadLink = null;
        for (Link link : foundStatus.getInnerStatus().links()) {
            if (downloadString.equals(link.rel())) {
                downloadLink = link;
                break;
            }
        }
        if (downloadLink != null) {
            openURL(downloadLink.href());
        } else {
            System.out.println("No download link for this builder");
        }

    }




}
