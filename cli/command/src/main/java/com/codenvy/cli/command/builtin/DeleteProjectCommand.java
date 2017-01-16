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

import com.codenvy.cli.command.builtin.model.UserProjectReference;
import com.codenvy.client.model.ProjectReference;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Delete Project command.
 * This command will destroy a project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "delete-project", description = "Delete a project")
public class DeleteProjectCommand extends AbsCommand {

    @Argument(name = "project-id", description = "Specify the project ID to use", required = true, multiValued = false)
    private String projectId;


    /**
     * Prints the current projects per workspace
     */
    protected Object execute() {
        init();

        // not logged in
        if (!checkifEnabledRemotes()) {
            return null;
        }

        // do we have the projectID ?
        if (projectId == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No projectID has been set");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // get project for the given shortID
        UserProjectReference project = getMultiRemoteCodenvy().getProjectReference(projectId);

        if (project == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No matching project for identifier '").a(projectId).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        final ProjectReference projectToDestroy = project.getInnerReference();

        project.getCodenvy().project().deleteResources(projectToDestroy, "").execute();
        System.out.println(String.format("The project %s has been deleted", projectToDestroy.name()));


        return null;
    }





}
