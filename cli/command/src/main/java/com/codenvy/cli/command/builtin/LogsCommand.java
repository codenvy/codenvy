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
import com.codenvy.cli.command.builtin.model.UserRunnerStatus;
import com.codenvy.client.model.BuilderState;
import com.codenvy.client.model.RunnerState;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import java.util.List;

import static com.codenvy.cli.command.builtin.MultiRemoteCodenvy.checkOnlyOne;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Allows to print the logs of a given runner/builder
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "logs", description = "Display output logs for a runner or builder")
public class LogsCommand extends AbsCommand {

    /**
     * Runner or builder ID
     */
    @Argument(name = "id", description = "Specify the runner/builder ID", required = false, multiValued = false)
    private String processID;

    /**
     * Execute the current command
     */
    protected Object execute() throws Exception {
        init();

        // not logged in
        if (!checkifEnabledRemotes()) {
            return null;
        }

        // do we have the process ID ?
        if (processID == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No ID has been defined. It should be a runner or builder ID");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // processId is beginning with a r --> runner ID
        if (processID.startsWith("r")) {
            displayRunnerLog();
        } else if (processID.startsWith("b")) {
            displayBuilderLog();
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
     * Display the runner log
     */
    protected void displayRunnerLog() {

        List<UserRunnerStatus> matchingStatuses = getMultiRemoteCodenvy().findRunners(processID);

        UserRunnerStatus foundStatus = checkOnlyOne(matchingStatuses, processID, "runner", "runners");

        // not found, errors already printed
        if (foundStatus == null) {
            return;
        }

        RunnerState state = foundStatus.getInnerStatus().status();

        if (state == RunnerState.NEW) {
            Ansi buffer = Ansi.ansi();
            buffer.a("Logs are not yet available as the runner has not yet started");
            buffer.reset();
            System.out.println(buffer.toString());
            return;
        }

        // not in a valid state
        if (state != RunnerState.RUNNING && state != RunnerState.STOPPED) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("Logs are only available in RUNNING or STOPPED state. Current state is ").a(state);
            buffer.reset();
            System.out.println(buffer.toString());
            return;
        }


        // Now, print the log
        String log = foundStatus.getProject().getCodenvy().runner().logs(foundStatus.getProject().getInnerReference(), foundStatus.getInnerStatus().processId()).execute();
        System.out.println(log);
    }


    /**
     * Display the builder log.
     */
    protected void displayBuilderLog() {

        List<UserBuilderStatus> matchingStatuses = getMultiRemoteCodenvy().findBuilders(processID);

        UserBuilderStatus foundStatus = checkOnlyOne(matchingStatuses, processID, "builder", "builders");
        // not found, errors already printed
        if (foundStatus == null) {
            return;
        }

        BuilderState state = foundStatus.getInnerStatus().status();

        // not in a valid state
        if (state == BuilderState.IN_QUEUE) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("Logs are not available in IN_QUEUE state");
            buffer.reset();
            System.out.println(buffer.toString());
            return;
        }


        // Now, print the log
        String log = foundStatus.getProject().getCodenvy().builder().logs(foundStatus.getProject().getInnerReference(), foundStatus.getInnerStatus().taskId()).execute();
        System.out.println(log);

    }

}

