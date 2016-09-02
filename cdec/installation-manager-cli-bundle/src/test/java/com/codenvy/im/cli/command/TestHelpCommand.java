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
package com.codenvy.im.cli.command;

import com.codenvy.cli.command.builtin.MultiRemoteCodenvy;
import com.codenvy.im.facade.IMArtifactLabeledFacade;

import org.apache.felix.service.command.CommandSession;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertTrue;

/** @author Anatoliy Bazko */
public class TestHelpCommand extends AbstractTestCommand {
    private AbstractIMCommand spyCommand;

    @Mock
    private IMArtifactLabeledFacade service;
    @Mock
    private CommandSession          commandSession;
    @Mock
    private MultiRemoteCodenvy      multiRemoteCodenvy;

    @BeforeMethod
    public void initMocks() throws IOException {
        MockitoAnnotations.initMocks(this);

        spyCommand = spy(new HelpCommand());
        spyCommand.facade = service;
        doReturn(multiRemoteCodenvy).when(spyCommand).getMultiRemoteCodenvy();
        doReturn("").when(multiRemoteCodenvy).listRemotes();

        performBaseMocks(spyCommand, true);
    }

    @Test
    public void testHelp() throws Exception {
        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, commandSession);
        CommandInvoker.Result result = commandInvoker.invoke();
        String output = removeAnsi(result.getOutputStream());

        assertTrue(output.contains("COMMANDS\n"
                                   + "add-node    Add new Codenvy node such as builder or runner\n"
                                   + "backup      Backup all Codenvy data\n"
                                   + "config      Get installation manager configuration\n"
                                   + "download    Download artifacts or print the list of installed ones\n"
                                   + "install     Install, update artifact or print the list of already installed ones\n"
                                   + "remove-node Remove Codenvy node\n"
                                   + "restore     Restore Codenvy data\n"
                                   + "version     Print the list of available latest versions and installed ones\n"
                                   + "\n"
                                   + "Use '[command] --help' for help on a specific command."),
                   "Actual output: " + output);
    }
}
