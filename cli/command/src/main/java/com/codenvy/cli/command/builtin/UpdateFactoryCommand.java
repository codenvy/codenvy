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

import com.codenvy.client.Codenvy;

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

import static java.lang.String.format;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Update Factory command.
 * This command will update a factory with the given factory.
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "update-factory", description = "Update a factory")
public class UpdateFactoryCommand extends AbsCommand {

    @Argument(name = "id", description = "Factory ID", required = true, index = 0)
    private String factoryID;

    @Argument(name = "file", description = "Factory JSON file", required = true, index = 1)
    private String jsonFilePath;

    @Option(name = "--remote", description = "Name of the remote codenvy")
    private String remoteName;


    /**
     * Update the given factory
     */
    protected Object execute() throws IOException {
        init();

        // check remote
        if (!getSelectedRemote()) {
            return null;
        }


        Codenvy codenvy = getMultiRemoteCodenvy().getCodenvy(remoteName);
        if (codenvy == null) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a(format("Please login into the remote %s", remoteName));
            buffer.reset();
            System.out.println(buffer.toString());
            return null;
        }

        codenvy.factory().update(factoryID, getContentFile(new File(jsonFilePath))).execute();
        System.out.println(String.format("The factory with ID %s has been successfully updated", factoryID));

        return null;
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


    /**
     * Get content of the given file
     * @param jsonFile the file containing all the data
     * @return the content link
     */
    protected String getContentFile(File jsonFile) {
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
        return content.toString();
    }



}
