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
import com.codenvy.cli.command.builtin.util.metadata.CodenvyMetadata;
import com.codenvy.cli.command.builtin.util.zip.ZipUtils;
import com.codenvy.client.Response;
import com.codenvy.client.model.ProjectReference;

import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codenvy.cli.command.builtin.Constants.CODENVY_FOLDERNAME;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * @author Florent Benoit
 */
public abstract class AbsPushPullCommand extends AbsCommand {


    protected void pull(UserProjectReference project, File dest) throws IOException {

        ProjectReference projectToPull = project.getInnerReference();

        Response<ZipInputStream> response = project.getCodenvy().project().exportResources(projectToPull, null).response();

        long length = 0;
        Map<String, List<Object>> headers = response.getHeaders();
        if (headers != null) {
            List<Object> contentLength = headers.get("Content-Length");
            if (contentLength != null && contentLength.size() == 1) {
                length = Long.parseLong(contentLength.get(0).toString());
            }
        }

        String outLength = "";
        if (length > 0) {
            String fullLength = "";
            if (length < 1024) {
                fullLength = String.valueOf(length);
            } else if (length > 1024) {
                fullLength = String.valueOf(length / 1024).concat(" KB");
            } else if (length > 1048576) {
                fullLength = String.valueOf(length / 1048576).concat(" MB");
            }
            outLength = " / ".concat(fullLength);
        }


        try (ZipInputStream zipInputStream = response.getValue()) {
            byte[] buf = new byte[1024];
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            int total = 0;
            while (zipEntry != null) {


                File entryFile = new File(dest, zipEntry.getName());
                // Create directory
                if (zipEntry.isDirectory()) {
                    // create parent directories (with mkdirs)
                    if (!entryFile.exists() && !entryFile.mkdirs()) {
                        throw new IllegalStateException("Can not create directory " + entryFile + ", Check the write access.");
                    }
                    zipEntry = zipInputStream.getNextEntry();
                    continue;
                }
                // If it's a file, we must extract the file
                // Ensure that the directory exists.
                if (!entryFile.getParentFile().exists() && !entryFile.getParentFile().mkdirs()) {
                    throw new IllegalStateException("Unable to create directory" + entryFile.getParentFile());
                }


                int n;
                try (FileOutputStream fileoutputstream = new FileOutputStream(entryFile)) {

                    while ((n = zipInputStream.read(buf, 0, 1024)) > -1) {
                        fileoutputstream.write(buf, 0, n);
                    }
                }
                // get Compressed size (as this is what is downloaded)
                total += zipEntry.getCompressedSize();

                //
                String prettyTotal = "";
                if (total < 1024) {
                    prettyTotal = String.valueOf(total);
                } else if (total > 1024) {
                    prettyTotal = String.valueOf(total / 1024).concat(" KB");
                } else if (total > 1048576) {
                    prettyTotal = String.valueOf(total / 1048576).concat(" MB");
                }

                new ConsoleReader().resetPromptLine("Pulling...", prettyTotal.concat(outLength), 0);
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();

            }
        }
        System.out.println("done !                                ");
    }


    protected void override(UserProjectReference project, File dest) throws IOException {
        ProjectReference projectToPull = project.getInnerReference();
        // Now compare resources (if overriding)
        List<File> list = new ArrayList<>();
        ZipUtils.getAllFiles(dest, list);
        int size = list.size();
        int count = 0;
        for (File f : list) {
            count++;
            String progress = String.valueOf((100 * count) / size).concat(" %");
            new ConsoleReader().resetPromptLine("Pruning local files...", progress, 0);
            if (f.isDirectory()) {
                continue;
            }

            // Compute local path
            int i = f.getAbsolutePath().indexOf(project.name());
            String localPath = f.getAbsolutePath().substring(i + project.name().length() + 1);

            if (localPath.startsWith(Constants.CODENVY_FOLDERNAME)) {
                continue;
            }

            // check file exists on the remote side
            if (!project.getCodenvy().project().hasFile(projectToPull, localPath).execute().booleanValue()) {
                if (!f.delete()) {
                    System.out.println("Unable to remove local file " + f);
                }
            }
        }
        if (!list.isEmpty()) {
            System.out.println("done !        ");
        }
    }

    protected String getProjectFromDirectory(File directory) {

        // directory doesn't exists
        if (!directory.exists()) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("The specified path '").a(directory.getAbsolutePath()).a("' doesn't exists.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // path is not a directory
        if (!directory.isDirectory()) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("The specified path '").a(directory.getAbsolutePath()).a("' is not a directory.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // codenvy folder
        File codenvyFolder = new File(directory, CODENVY_FOLDERNAME);
        if (!codenvyFolder.exists() || !codenvyFolder.isDirectory()) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("The specified directory '").a(directory.getAbsolutePath()).a("' is not a codenvy project directory (created with clone-local command).");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        try {
            CodenvyMetadata codenvyMetadata = new CodenvyMetadata(directory);
            return codenvyMetadata.getProjectId();
        } catch (Exception e) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("The specified directory '").a(directory.getAbsolutePath()).a(
                    "' has not been pulled by the CLI. Unable to manage it.");
            buffer.reset();
            System.out.println(buffer.toString());
        }
        return null;
    }

}
