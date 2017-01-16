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

import com.codenvy.cli.command.builtin.model.DefaultUserRunnerStatus;
import com.codenvy.cli.command.builtin.model.UserProjectReference;
import com.codenvy.cli.command.builtin.model.UserRunnerStatus;
import com.codenvy.client.model.RunnerStatus;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.List;

import static com.codenvy.cli.command.builtin.MultiRemoteCodenvy.checkOnlyOne;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Stop command.
 * This command will stop a  given runner or all the runners of a given project.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "stop", description = "Stop one or more runner processes")
public class StopCommand extends AbsCommand {

    @Argument(name = "id", description = "Specify the runnerID or projectID to use", required = true, multiValued = false)
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

        // do we have the  ID ?
        if (id == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No ID has been defined. It should be a runner or project ID");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // processId is beginning with a r --> runner ID
        if (id.startsWith("r")) {
            stopRunnerProcess();
        } else if (id.startsWith("p")) {
            stopProjectProcesses();
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

    /**
     * Stop the given runner ID
     */
    protected void stopRunnerProcess() {
        java.util.List<UserRunnerStatus> matchingStatuses = getMultiRemoteCodenvy().findRunners(id);

        UserRunnerStatus foundStatus = checkOnlyOne(matchingStatuses, id, "runner", "runners");

        // not found, errors already printed
        if (foundStatus == null) {
            return;
        }

        UserRunnerStatus updatedStatus = stopStatus(foundStatus);

        // display the updated status
        System.out.println(updatedStatus);

    }

    /**
     * Stop the given status
     * @param status the given status
     * @return the updated status
     */
    protected UserRunnerStatus stopStatus(UserRunnerStatus status) {
        RunnerStatus updatedStatus = status.getProject().getCodenvy().runner().stop(status.getProject().getInnerReference(), status.getInnerStatus().processId()).execute();
        return new DefaultUserRunnerStatus(updatedStatus, status.getProject());

    }


    protected void stopProjectProcesses() {
        // needs to find all run processes of the project
        UserProjectReference foundProject = getMultiRemoteCodenvy().getProjectReference(id);

        // not found, errors already printed
        if (foundProject == null) {
            return;
        }

        List<String> runnersStopped = new ArrayList<>();
        List<UserRunnerStatus> statusList = getMultiRemoteCodenvy().getRunners(foundProject);
        if (statusList.isEmpty()) {
            System.out.println("No active runners for the given project.");
            return;
        }

        for (UserRunnerStatus runnerStatus : statusList) {
            stopStatus(runnerStatus);
            runnersStopped.add(runnerStatus.shortId());
        }

        System.out.println("Runners stopped: " + runnersStopped);

    }

}
