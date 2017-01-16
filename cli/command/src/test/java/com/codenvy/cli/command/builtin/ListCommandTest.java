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

import com.codenvy.cli.command.builtin.util.ascii.FormatterMode;
import com.codenvy.client.dummy.DummyCodenvyClient;
import com.codenvy.client.dummy.project.DummyProject;
import com.codenvy.client.dummy.workspace.DummyWorkspace;

import org.apache.felix.service.command.CommandSession;
import org.testng.annotations.Test;

import static com.codenvy.cli.command.builtin.util.ascii.FormatterMode.CSV;
import static java.lang.String.format;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test of the list command
 * 
 * @author Florent Benoit
 */
public class ListCommandTest extends AbsCommandTest {


    protected CommandInvoker getInvoker() {
        ListCommand listCommand = new ListCommand();


        DummyCodenvyClient codenvyClient = new DummyCodenvyClient();

        // prepare
        CommandSession commandSession = prepare(listCommand, codenvyClient);



        return new CommandInvoker(listCommand, commandSession);
    }


    /**
     * Expect it works when there are no workspaces
     */
    @Test
    public void testNoWorkspace() throws Exception {

        CommandInvoker.Result result = getInvoker().invoke();
        assertTrue(result.disableAnsi().getOutputStream().contains("No projects"));

    }

    /**
     * Expect it works when there is one workspace without projects
     */
    @Test
    public void testOneWorkspaceNoProjects() throws Exception {

        CommandInvoker commandInvoker = getInvoker();
        DummyWorkspace dummyWorkspace = commandInvoker.getCodenvyClient().newWorkspaceBuilder("WORKSPACE1").build();
        commandInvoker.getDefaultRemoteCodenvy().workspace().registerWorkspace(dummyWorkspace);

        CommandInvoker.Result result = commandInvoker.invoke();
        assertTrue(result.disableAnsi().getOutputStream().contains("No projects"));

    }


    /**
     * Expect it works when there is one workspace with one project
     */
    @Test
    public void testOneWorkspaceOneProjectDefault() throws Exception {

        CommandInvoker commandInvoker = getInvoker();
        DummyWorkspace workspace1 = commandInvoker.getCodenvyClient().newWorkspaceBuilder("WORKSPACE1").build();
        DummyProject project1 = commandInvoker.getCodenvyClient().newProjectBuilder(workspace1, "project1").withType(
                "java").withVisibility("public").build();
        commandInvoker.getDefaultRemoteCodenvy().workspace().registerWorkspace(workspace1);
        commandInvoker.getDefaultRemoteCodenvy().project().registerProject(project1);

        String project1Id = getProjectId(project1);

        // use CSV format
        doReturn(CSV).when(commandInvoker.getCommandSession()).get(FormatterMode.class.getName());

        CommandInvoker.Result result = commandInvoker.invoke();

        assertEquals(result.disableAnsi().getOutputStream(), format("ID,REMOTE,WORKSPACE,PROJECT,TYPE,PRIVACY%n" +
                                                                    project1Id + ",default," + workspace1.name() + "," + project1.name() + "," + project1.type() + "," + project1.visibility() + "%n" +
                                                                    "%n"));

    }


    /**
     * Expect it works when there is one workspace with one project
     */
    @Test
    public void testOneWorkspaceOneProjectVerbose() throws Exception {

        CommandInvoker commandInvoker = getInvoker().option("-v", true);

        DummyWorkspace workspace1 = commandInvoker.getCodenvyClient().newWorkspaceBuilder("WORKSPACE1").build();
        DummyProject project1 = commandInvoker.getCodenvyClient().newProjectBuilder(workspace1, "project1").withType("java").withVisibility("public").build();
        commandInvoker.getDefaultRemoteCodenvy().workspace().registerWorkspace(workspace1);
        commandInvoker.getDefaultRemoteCodenvy().project().registerProject(project1);

        String project1Id = getProjectId(project1);

        // use CSV format
        doReturn(CSV).when(commandInvoker.getCommandSession()).get(FormatterMode.class.getName());

        CommandInvoker.Result result = commandInvoker.invoke();

        assertEquals(result.disableAnsi().getOutputStream(), format("ID,REMOTE,WORKSPACE,PROJECT,TYPE,PRIVACY,PERM,BUILDERS,RUNNERS%n" +
                                                                    project1Id + ",default," + workspace1.name() + "," + project1.name() + "," + project1.type() + "," + project1.visibility()  + ",,none,none%n" +
                                                                    "%n"));

    }
}
