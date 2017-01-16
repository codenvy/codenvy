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

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import java.io.File;

import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Allows to pull a given project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "pull", description = "Update project sync point directory created by clone-local")
public class PullCommand extends AbsPushPullCommand {

    @Argument(name = "codenvy-directory", description = "Specify the directory of a codenvy project")
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

        // try to see if we have a Codenvy project in the directory
        File directoryToGet;
        if (directory != null) {
            directoryToGet = new File(directory);
        } else {
            directoryToGet = new File(new File("").getAbsolutePath());
        }


        String projectId = getProjectFromDirectory(directoryToGet);

        // do we have the projectID ?
        if (projectId == null) {
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


        // Ok now we have the project, checkout it
        System.out.println(String.format("Pulling project %s into %s", project.name(), directoryToGet.getAbsolutePath()));
        pull(project, directoryToGet);

        override(project, directoryToGet);

        return null;
    }

}

