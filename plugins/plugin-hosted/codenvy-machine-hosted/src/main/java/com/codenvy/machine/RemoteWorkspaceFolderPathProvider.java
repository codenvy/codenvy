/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.machine;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides path to workspace folder on the machines nodes.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class RemoteWorkspaceFolderPathProvider implements WorkspaceFolderPathProvider {
    private final Path projectsFolderPath;

    @Inject
    public RemoteWorkspaceFolderPathProvider(@Named("machine.project.location") String machineProjectsDir) throws IOException {
        Path folder = Paths.get(machineProjectsDir);
        if (Files.notExists(folder)) {
            // TODO do not do that after moving to codenvy in docker
            Files.createDirectory(folder);
        }
        if (!Files.isDirectory(folder)) {
            throw new IOException("Projects folder " +
                                  folder.toAbsolutePath() +
                                  " is invalid. Check machine.project.location configuration property.");

        }
        projectsFolderPath = folder.toAbsolutePath();
    }

    @Override
    public String getPath(@Assisted("workspace") String workspaceId) throws IOException {
        return projectsFolderPath.resolve(workspaceId).toString();
    }
}
