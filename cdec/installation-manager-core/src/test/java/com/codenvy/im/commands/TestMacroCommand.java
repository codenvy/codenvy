/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.im.commands;

import com.codenvy.im.BaseTest;
import com.codenvy.im.agent.AgentException;
import com.codenvy.im.managers.NodeConfig;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

/**
 * @author Dmytro Nochevnov
 */
public class TestMacroCommand {

    @Test
    public void testDescription() {
        MacroCommand testCommand = new MacroCommand(Collections.<Command>emptyList(), "macro");
        assertEquals(testCommand.getDescription(), "macro");
    }

    @Test
    public void testToString() {
        List<Command> commands = new ArrayList<Command>() {{
            add(new DummyCommand("command1"));
            add(new DummyCommand("command2"));
        }};

        MacroCommand testCommand = new MacroCommand(commands, "macro");
        assertEquals(testCommand.toString(), "[command1, command2]");
    }

    @Test
    public void testExecution() throws CommandException {
        List<Command> commands = new ArrayList<Command>() {{
            add(new DummyCommand("command1"));
            add(new DummyCommand("command2"));
        }};

        MacroCommand testCommand = new MacroCommand(commands, "macro");

        String result = testCommand.execute();
        assertEquals(result, "command1\ncommand2\n");
    }

    @Test(expectedExceptions = CommandException.class, expectedExceptionsMessageRegExp = "exception")
    public void testInnerCommandException() throws CommandException {
        final DummyCommand spyDummyCommand = spy(new DummyCommand("command1"));
        doThrow(new CommandException("exception", new IOException())).when(spyDummyCommand).execute();

        MacroCommand testCommand = new MacroCommand(new ArrayList<Command>() {{
            add(spyDummyCommand);
        }}, "macro");

        testCommand.execute();
    }

    @Test
    public void testCreateShellCommandsForEachNode() throws AgentException {
        String command = "command";
        String description = "description";
        List<NodeConfig> nodes = ImmutableList.of(
                new NodeConfig(NodeConfig.NodeType.API, "localhost"),
                new NodeConfig(NodeConfig.NodeType.DATA, "127.0.0.1")
                                                 );

        String user = System.getProperty("user.name");
        String expectedCommand = format("[" +
                                        "{'command'='command', 'agent'='{'host'='localhost', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, " +
                                        "{'command'='command', 'agent'='{'host'='127.0.0.1', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}" +
                                        "]", user);

        Command testMacroCommand = MacroCommand.createCommand(command, description, nodes);
        assertEquals(testMacroCommand.toString(), expectedCommand);
    }

    @Test
    public void testCreateShellCommandForNodeList() throws AgentException {
        String expectedCommandString = format("[" +
                                              "{'command'='testCommand', 'agent'='{'host'='localhost', 'port'='22', 'user'='%1$s', 'identity'='[~/" +
                                              ".ssh/id_rsa]'}'}, " +
                                              "{'command'='testCommand', 'agent'='{'host'='127.0.0.1', 'port'='22', 'user'='%1$s', 'identity'='[~/" +
                                              ".ssh/id_rsa]'}'}" +
                                              "]",
                                              BaseTest.SYSTEM_USER_NAME);

        List<NodeConfig> nodes = ImmutableList.of(
                new NodeConfig(NodeConfig.NodeType.API, "localhost"),
                new NodeConfig(NodeConfig.NodeType.DATA, "127.0.0.1")
                                                 );

        Command testCommand = MacroCommand.createCommand("testCommand", nodes);
        assertEquals(testCommand.toString(), expectedCommandString);
    }

    class DummyCommand implements Command {
        private String description;

        public DummyCommand(String description) {
            this.description = description;
        }

        @Override
        public String execute() throws CommandException {
            return description;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
