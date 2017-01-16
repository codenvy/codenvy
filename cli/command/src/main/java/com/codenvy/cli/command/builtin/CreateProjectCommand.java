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
import com.codenvy.cli.command.builtin.model.UserWorkspace;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.nio.file.Path;

import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Allows to import a project into Codenvy
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "create-project", description = "Create a project")
public class CreateProjectCommand extends AbsCommand {


    @Argument(name = "param", description = "Local path, git URL, zip file, etc", required = true, index = 0)
    private String param;

    @Argument(name = "configFile", description = "Extra configuration for the given project to import", required = false, index = 1)
    private String configFile;

    @Option(name = "--name", description = "Name of the project")
    private String name;

    @Option(name = "--workspace", description = "Specify the workspace to use")
    private String workspace;

    @Option(name = "--remote", description = "Specify the remote to use")
    private String remote;

    @Option(name = "--open", description = "Open the project once created")
    private boolean openProject;

    @Option(name = "--importer", description = "The type of importer to use if an external URL is given")
    private String importer;

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


        UserWorkspace userWorkspace = getMultiRemoteCodenvy().getUserWorkspace(remote, workspace);
        if (userWorkspace == null) {
            return null;
        }

        Path configurationPath = null;
        // is that the parameter is a path ?
        if (configFile != null) {
            configurationPath = new File(configFile).toPath();
            if (!configurationPath.toFile().exists()) {
                Ansi buffer = Ansi.ansi();
                buffer.fg(RED);
                buffer.a("The configuration file '").a(configFile).a("' does not exists.");
                buffer.reset();
                System.out.println(buffer.toString());
                return null;
            }
        }


        UserProjectReference userProjectReference = getMultiRemoteCodenvy().importProject(userWorkspace, name, param, importer, configurationPath);

        if (userProjectReference != null) {
            System.out.println(String.format("Project '%s' has been created in workspace '%s' with project ID '%s'", userProjectReference.name(), userProjectReference.getWorkspace().name(), userProjectReference.shortId()));
            if (openProject) {
                openURL(userProjectReference.getInnerReference().ideUrl());
            }
        }
        return null;
    }



}

