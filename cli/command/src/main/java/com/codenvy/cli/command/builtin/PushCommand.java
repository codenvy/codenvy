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

import jline.console.ConsoleReader;

import com.codenvy.cli.command.builtin.model.UserProjectReference;
import com.codenvy.cli.command.builtin.util.zip.ZipUtils;
import com.codenvy.client.model.ProjectReference;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Allows to push a given project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "push", description = "Push local project changes back to Codenvy")
public class PushCommand extends AbsPushPullCommand {

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
        File directoryToSend;
        if (directory != null) {
            directoryToSend = new File(directory);
        } else {
            directoryToSend = new File(new File("").getAbsolutePath());
        }


        String projectId = getProjectFromDirectory(directoryToSend);
        if (projectId == null) {
            return null;
        }

        // get project for the given shortID
        UserProjectReference project = getMultiRemoteCodenvy().getProjectReference(projectId);

        if (project == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(
                    "The project of the specified directory is no longer existing in the connected remote directories. Unable to continue.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }


        final ProjectReference projectToPush = project.getInnerReference();

        // ok now we perform push
        System.out.print("Pushing...");

        // create zip of the current archive
        final InputStream exportedZipInputStream = ZipUtils.getZipProjectStream(directoryToSend);

        // send it
        project.getCodenvy().project().importArchive(projectToPush.workspaceId(), projectToPush, exportedZipInputStream).execute();

        System.out.println(" done !");



        // now download the remote zip in order to compare if there are files to delete
        int deleteCount = 0;
        try (ZipInputStream zipInputStream = project.getCodenvy().project().exportResources(projectToPush, null).execute()) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {

                // path has been removed locally ?
                File path = new File(directoryToSend, zipEntry.getName());
                new ConsoleReader().resetPromptLine("Pruning remote files...", String.valueOf(deleteCount).concat("removed. ".concat(" comparing :") + zipEntry.getName()) , 0);
                if (!path.exists() && path.getParentFile().exists()) {
                    // remote resources on the remote side
                    project.getCodenvy().project().deleteResources(projectToPush, zipEntry.getName()).execute();
                    deleteCount++;

                }

                // read next entry
                zipEntry = zipInputStream.getNextEntry();
            }
            new ConsoleReader().resetPromptLine("Pruning remote files...", "done", 0);
            System.out.println();
        }


        return null;
    }


}

