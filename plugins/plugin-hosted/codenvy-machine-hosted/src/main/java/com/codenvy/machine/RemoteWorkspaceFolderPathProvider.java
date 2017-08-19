/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.machine;

import com.google.inject.assistedinject.Assisted;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;

/**
 * Provides path to workspace folder on the machines nodes.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class RemoteWorkspaceFolderPathProvider implements WorkspaceFolderPathProvider {
  private final Path projectsFolderPath;

  @Inject
  public RemoteWorkspaceFolderPathProvider(
      @Named("machine.project.location") String machineProjectsDir) throws IOException {
    Path folder = Paths.get(machineProjectsDir);
    if (Files.notExists(folder)) {
      // TODO do not do that after moving to codenvy in docker
      Files.createDirectory(folder);
    }
    if (!Files.isDirectory(folder)) {
      throw new IOException(
          "Projects folder "
              + folder.toAbsolutePath()
              + " is invalid. Check machine.project.location configuration property.");
    }
    projectsFolderPath = folder.toAbsolutePath();
  }

  @Override
  public String getPath(@Assisted("workspace") String workspaceId) throws IOException {
    return projectsFolderPath.resolve(workspaceId).toString();
  }
}
