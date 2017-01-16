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

import com.codenvy.cli.command.builtin.util.ascii.AsciiArray;
import com.codenvy.client.Codenvy;
import com.codenvy.client.CodenvyException;
import com.codenvy.client.model.Factory;
import com.codenvy.client.model.User;
import com.codenvy.client.model.factory.FactoryCreator;
import com.codenvy.client.model.factory.FactoryProject;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * List Factories command.
 * This command will list the factories
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "list-factories", description = "List factories")
public class ListFactoriesCommand extends AbsCommand {

    @Option(name = "--remote", description = "Name of the remote codenvy")
    private String remoteName;


    /**
     * Prints the current projects per workspace
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

        if (isProgressEnabled()) {
            new ConsoleReader().resetPromptLine("Retrieving factories...", "", 0);
        }

        User user = codenvy.user().current().execute();
        List<String> factoriesID = codenvy.factory().list(user).execute();

        if (isProgressEnabled()) {
            new ConsoleReader().resetPromptLine("", "", 0);
        }

        List<String> factoriesName = new ArrayList<>();
        List<String> factoriesVersion = new ArrayList<>();
        List<String> factoriesCreated = new ArrayList<>();

        int count = 0;
        for (String factoryId : factoriesID) {
            try {
                if (isProgressEnabled()) {
                    count++;
                    String percent = (count * 100) / factoriesID.size() + "/100";
                    new ConsoleReader().resetPromptLine("Collecting factory data...", percent, percent.length());
                }

                Factory factory = codenvy.factory().get(factoryId).execute();
                FactoryProject factoryProject = factory.project();
                if (factoryProject != null) {
                    factoriesName.add(factoryProject.name());
                } else {
                    factoriesName.add("---");
                }
                factoriesVersion.add(factory.getV());
                FactoryCreator creator = factory.creator();
                if (creator != null) {
                    Date date = new Date(creator.created());
                    factoriesCreated.add(date.toString());
                } else {
                    factoriesCreated.add("...");
                }
            } catch (CodenvyException e) {
                e.printStackTrace();
                factoriesName.add("---");
                factoriesVersion.add("---");
                factoriesName.add("---");
            }
        }
        if (isProgressEnabled()) {
            new ConsoleReader().resetPromptLine("", "", 0);
        }


        AsciiArray asciiArray = buildAsciiArray().withColumns(factoriesID, factoriesName, factoriesVersion, factoriesCreated).withTitle("ID", "Name", "Version", "Created");

        System.out.println(asciiArray.toAscii());

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



}
