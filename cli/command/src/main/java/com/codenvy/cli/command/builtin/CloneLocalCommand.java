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
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import java.io.File;

import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Allows to pull a given project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "clone-local", description = "Clone a remote Codenvy project to a local directory")
public class CloneLocalCommand extends AbsPushPullCommand {

    @Option(name = "--override", description = "Override existing directory")
    private boolean override;

    @Argument(name = "project-id", description = "Specify the project ID to use", required = true, multiValued = false, index = 0)
    private String projectId;

    @Argument(name = "dest-directory", description = "Specify the directory for pulling the project", index = 1)
    private String directory;

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


        final ProjectReference projectToPull = project.getInnerReference();

        File dest;
        if (directory == null) {
            // using current directory
            dest = new File(projectToPull.name());
        } else {
            File expectedDirectory = new File(directory);
            if (!expectedDirectory.exists()) {
                System.out.println(
                        String.format("Cannot pull project %s into %s as directory does not exist", projectToPull.name(), directory));
                return null;
            }

            dest = new File(expectedDirectory, projectToPull.name());
        }

        if (!override && dest.exists()) {
            System.out.println(
                    String.format("Cannot pull project %s into %s as directory already exists. Use --override option", projectToPull.name(),
                                  dest.getAbsolutePath()));
            return null;
        }


        // Ok now we have the project, checkout it
        System.out.println(String.format("Cloning project %s into %s", projectToPull.name(), dest.getAbsolutePath()));

        // pull
        pull(project, dest);

        // create metadata
        getMultiRemoteCodenvy().storeMetadata(project, dest);

        // Now compare resources (if overriding)
        if (override) {
            override(project, dest);
        }

        return null;
    }


}

