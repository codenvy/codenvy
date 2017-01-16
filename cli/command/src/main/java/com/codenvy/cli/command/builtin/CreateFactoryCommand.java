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
import com.codenvy.client.Codenvy;
import com.codenvy.client.model.Factory;
import com.codenvy.client.model.Link;
import com.codenvy.client.model.Project;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

import static java.lang.String.format;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Create Factory command.
 * This command will create a factory.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "create-factory", description = "Create a factory")
public class CreateFactoryCommand extends AbsCommand {

    /**
     * VCS Provider Name property.
     */
    private static final String VCS_PROVIDER_NAME = "vcs.provider.name";

    @Argument(name = "parameter", description = "ProjectID or path to the Codenvy Factory JSON file", required = true)
    private String parameter;


    @Option(name = "--remote", description = "Name of the remote codenvy")
    private String remoteName;

    /**
     * Invoke the factory link once it has been built
     */
    @Option(name = "--invoke")
    private boolean invoke;

    /**
     * Prints the current projects per workspace
     */
    protected Object execute() {
        init();

        // check remote
        if (!getSelectedRemote()) {
            return null;
        }

        // is that the parameter is a path ?
        File jsonFile = new File(parameter);
        String factoryLink = null;
        if (!jsonFile.exists()) {
            // try as projectID
            UserProjectReference projectReference = getMultiRemoteCodenvy().getProjectReference(parameter);
            if (projectReference == null) {
                Ansi buffer = Ansi.ansi();
                buffer.fg(RED);
                buffer.a("No matching project for identifier '").a(parameter).a("'.");
                buffer.reset();
                System.out.println(buffer.toString());
                return null;

            }
            factoryLink = createFactoryProject(projectReference);
        } else {
            // JSON file is here, use it.
            factoryLink = getEncodedFactory(jsonFile);
        }

        if (invoke && factoryLink != null) {
            openURL(factoryLink);
        } else if (factoryLink != null) {
            System.out.println("Factory URL: " + factoryLink);
        }

        return null;
    }

    /**
     * Creates a factory link for the given project.
     * @param projectReference the {@link com.codenvy.cli.command.builtin.model.UserProjectReference project reference on which we need to create the factory link}
     * @return the factory link
     */
    protected String createFactoryProject(UserProjectReference projectReference) {
        // get project attributes
        Project projectDescription = projectReference.getCodenvy().project().getProject(projectReference.getInnerReference().workspaceId(), projectReference.getInnerReference()).execute();
        if (projectDescription == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("No matching project for identifier '").a(parameter).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        // project is under VCS ?
        List<String> vcsList = projectDescription.attributes().get(VCS_PROVIDER_NAME);

        // Not under VCS so needs to initialize
        if (vcsList == null || vcsList.isEmpty()) {
            // need to init the repo
            projectReference.getCodenvy().git().init(projectReference.getInnerReference()).execute();
        }

        // Ok so now we have a project that is under VCS so want to export it
        String content = projectReference.getCodenvy().factory().export(projectReference.getInnerReference()).execute();

        // and then get factory from the project json content
        return getEncodedFactory(content, projectReference.getCodenvy());

    }


    protected boolean getSelectedRemote() {
        // no remote, use default
        if (remoteName == null) {
            remoteName = getMultiRemoteCodenvy().getDefaultRemoteName();
        } else {
            if (getMultiRemoteCodenvy().getRemote(remoteName) == null) {
                Ansi buffer = Ansi.ansi();
                buffer.fg(RED);
                buffer.a(format("The specified remote %s does not exists", remoteName));
                buffer.reset();
                System.out.println(buffer.toString());
                return false;
            }
        }
        return true;
    }



    protected String getEncodedFactory(String content, Codenvy codenvy) {

        if (codenvy == null) {
            codenvy = getMultiRemoteCodenvy().getCodenvy(remoteName);
        }
        if (codenvy == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(format("Please login into the remote %s", remoteName));
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }
        Factory factory = codenvy.factory().save(content).execute();

        // Search links
        String createProjectUrl = null;
        List<Link> links = factory.getLinks();
        for (Link link : links) {
            if ("create-project".equals(link.rel())) {
                createProjectUrl = link.href();
                break;
            }
        }
        return createProjectUrl;
    }

    /**
     * Build an encoded factory based on the given JSON file
     * @param jsonFile the file containing all the data
     * @return the factory link
     */
    protected String getEncodedFactory(File jsonFile) {
        StringBuilder content = new StringBuilder();
        try (InputStream readInputStream = new FileInputStream(jsonFile);
             Reader inputStreamReader = new InputStreamReader(readInputStream, Charset.defaultCharset());
             BufferedReader reader = new BufferedReader(inputStreamReader)
        ) {
            String line;
            while( ( line = reader.readLine() ) != null ) {
                content.append( line );
                content.append( System.lineSeparator() );
            }
        } catch (IOException e) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(format("Unable to load the content of the JSON file %s", jsonFile.getAbsolutePath()));
            buffer.reset();
            System.out.println(buffer.toString());
        }
        return getEncodedFactory(content.toString(), null);
    }






}
