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

import com.codenvy.cli.command.builtin.helper.WaitingAction;
import com.codenvy.cli.command.builtin.helper.WaitingActionCondition;
import com.codenvy.cli.command.builtin.helper.WaitingActionConditionState;
import com.codenvy.cli.command.builtin.model.DefaultUserBuilderStatus;
import com.codenvy.cli.command.builtin.model.UserBuilderStatus;
import com.codenvy.cli.command.builtin.model.UserProjectReference;
import com.codenvy.client.Request;
import com.codenvy.client.model.BuilderState;
import com.codenvy.client.model.BuilderStatus;
import com.codenvy.client.model.Link;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.ProjectReference;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import static com.codenvy.client.model.BuilderState.CANCELLED;
import static com.codenvy.client.model.BuilderState.FAILED;
import static com.codenvy.client.model.BuilderState.IN_PROGRESS;
import static com.codenvy.client.model.BuilderState.SUCCESSFUL;
import static java.lang.String.format;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Allows to build a given project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "build", description = "Build a project")
public class BuildCommand extends AbsCommand {

    @Argument(name = "project-id", description = "Specify the project ID to use", required = true, multiValued = false)
    private String projectID;

    @Option(name = "--fg", description = "Run foreground", required = false)
    private boolean foreground;

    @Option(name = "--bg", description = "Run background", required = false)
    private boolean background;

    /**
     * Execute the command
     */
    @Override
    protected Object execute() throws Exception {
        init();

        // not logged in
        if (!checkifEnabledRemotes()) {
            return null;
        }

        // do we have the projectID ?
        if (projectID == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No projectID has been set");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // get project for the given shortID
        UserProjectReference project = getMultiRemoteCodenvy().getProjectReference(projectID);

        if (project == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No matching project for identifier '").a(projectID).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }


        final ProjectReference projectToBuild = project.getInnerReference();
        // first check if the project has a builder
        Project projectDescription = project.getCodenvy().project().getProject(projectToBuild.workspaceId(), projectToBuild).execute();
        if (projectDescription != null) {
            if (projectDescription.builders() == null || projectDescription.builders().defaultBuilder() == null) {
                Ansi buffer = Ansi.ansi();
                buffer.fg(RED);
                buffer.a("The selected project '").a(projectDescription.name()).a("' with ID '").a(projectID)
                      .a("' has no builder defined so this project can't be built.");
                buffer.reset();
                System.out.println(buffer.toString());
                return null;
            }
        }


        // Ok now we have the project
        final BuilderStatus builderStatus = project.getCodenvy().builder().build(projectToBuild).execute();

        UserBuilderStatus userBuilderStatus = new DefaultUserBuilderStatus(builderStatus, project);

        // ok so now we've started the run
        if (background) {
            useBackGround(userBuilderStatus);
        } else {
            useForeGround(userBuilderStatus);
        }

        return null;
    }

    /**
     * Build has been launched, just inform the user that process has been launched.
     * @param userBuilderStatus the builder status
     */
    protected void useBackGround(UserBuilderStatus userBuilderStatus) {
        Ansi buffer = Ansi.ansi();
        buffer.a("Build task for project ").a(INTENSITY_BOLD).a(userBuilderStatus.getProject().name()).a(INTENSITY_BOLD_OFF)
              .a("' has been submitted with builder ID ").a(INTENSITY_BOLD).a(userBuilderStatus.shortId()).a(INTENSITY_BOLD_OFF)
              .a(System.lineSeparator());
        System.out.println(buffer.toString());
        System.out.println(userBuilderStatus);

    }


    /**
     * Build has been launched, as we're in foreground mode, we need to wait that the process start
     * @param userBuilderStatus the runner status
     */
    protected void useForeGround(UserBuilderStatus userBuilderStatus) {

        WaitingActionCondition<BuilderStatus> condition = new BuilderStatusWaitingActionCondition(userBuilderStatus);

        // now we have to wait that the process is updated
        Request<BuilderStatus> request = userBuilderStatus.getProject().getCodenvy().builder()
                                              .status(userBuilderStatus.getProject().getInnerReference(), userBuilderStatus.getInnerStatus().taskId());
        WaitingAction<BuilderStatus> waitingAction = new WaitingAction<>("Build task waiting for a remote builder...", "Build finished.", request, condition);

        BuilderStatus executedStatus = waitingAction.execute();

        if (executedStatus == null) {
            System.out.println("Unable to find updated status");
            return;
        }

        //ok process has been launched
        UserBuilderStatus newStatus =  new DefaultUserBuilderStatus(executedStatus, userBuilderStatus.getProject());

        // print logs if not cancelled
        if (CANCELLED != newStatus.getInnerStatus().status()) {
            String logs = newStatus.getProject().getCodenvy().builder()
                                   .logs(newStatus.getProject().getInnerReference(), newStatus.getInnerStatus().taskId()).execute();
            System.out.println("Logs:");
            System.out.println(logs);
        }

        // it is now running
        if (SUCCESSFUL == newStatus.getInnerStatus().status()) {
            String link = "";
            Link artifactLink = newStatus.getInnerStatus().getDownloadLink();
            if (artifactLink != null) {
                link = artifactLink.href();
            }
            System.out.println(format("Project %s has been successfully built.%nArtifact URL is '%s'", newStatus.getProject().name(), link));
        } else if (FAILED == newStatus.getInnerStatus().status()) {
            System.out.println(format("Project %s has a build failure", newStatus.getProject().name()));
        } else if (CANCELLED == newStatus.getInnerStatus().status()) {
            System.out.println(format("Project %s has been cancelled", newStatus.getProject().name()));
        }

        Ansi buffer = Ansi.ansi();
        buffer.a("Builder ID is ").a(INTENSITY_BOLD).a(userBuilderStatus.shortId()).a(INTENSITY_BOLD_OFF);
        System.out.println(buffer.toString());



    }


    private static class BuilderStatusWaitingActionCondition implements WaitingActionCondition<BuilderStatus> {
        private boolean inProgress = false;
        private BuilderState currentState;

        public BuilderStatusWaitingActionCondition(UserBuilderStatus userBuilderStatus) {
            this.currentState = userBuilderStatus.getInnerStatus().status();
        }

        @Override
        public void check(WaitingActionConditionState<BuilderStatus> checker) {
            BuilderStatus current = checker.current();
            if (!inProgress && IN_PROGRESS == current.status()) {
                currentState = IN_PROGRESS;
                inProgress = true;
                checker.updatedText("Build is in progress ...");
            }
            if (currentState != current.status()) {
                checker.setComplete();
            }

        }
    }

}

