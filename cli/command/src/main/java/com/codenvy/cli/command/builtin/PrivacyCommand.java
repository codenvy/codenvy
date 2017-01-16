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
import com.codenvy.client.model.Visibility;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.fusesource.jansi.Ansi;

import static java.lang.String.format;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Privacy command.
 * This command will change the privacy for a project
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "privacy", description = "Set privacy of a project")
public class PrivacyCommand extends AbsCommand {

    @Argument(name = "project-id", description = "Specify the project ID to use", required = true, multiValued = false)
    private String projectId;

    @Argument(name = "privacy", description = "Specify the visibility (private or public)", index = 1)
    private String visibilityString;


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

        final ProjectReference projectToChangePrivacy = project.getInnerReference();

        // only display privacy if flag is not et
        if (visibilityString == null) {
            System.out.println(format("Privacy for project %s is %s", project.name(), projectToChangePrivacy.visibility()));
            return null;
        }

        Visibility visibility;
        if ("public".equals(visibilityString)) {
            visibility = Visibility.PUBLIC;
        } else if ("private".equals(visibilityString)) {
            visibility = Visibility.PRIVATE;
        } else {
            System.out.println(format("Privacy should be either 'private' or 'public'. Found %s", visibilityString));
            return null;
        }

        // change it
        project.getCodenvy().project().switchVisibility(projectToChangePrivacy, visibility).execute();

        // Display new flag
        System.out.println(format("Privacy for project %s has been changed to %s", project.name(), visibility));

        return null;
    }





}
