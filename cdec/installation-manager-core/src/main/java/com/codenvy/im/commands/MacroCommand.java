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

import com.codenvy.im.agent.AgentException;
import com.codenvy.im.managers.NodeConfig;

import java.util.ArrayList;
import java.util.List;

/** @author Dmytro Nochevnov */
public class MacroCommand implements Command {
    private final String        description;
    private final List<Command> commands;

    public MacroCommand(List<Command> commands, String description) {
        this.commands = commands;
        this.description = description;
    }

    /** {@inheritDoc} */
    @Override
    public String execute() throws CommandException {
        StringBuilder results = new StringBuilder();
        for (Command command : commands) {
            String result = command.execute();
            results.append(result).append("\n");
        }
        return results.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return commands.toString();
    }

    /** Create command which will be executed on several remote nodes. */
    public static Command createCommand(String command, List<NodeConfig> nodes) throws AgentException {
        return createCommand(command, null, nodes);
    }

    /**
     * Factory method. Creates MacroCommand which includes list of identical commands
     * with {@link com.codenvy.im.agent.SecureShellAgent} to be applied at every remote node in the given nodes list.
     */
    public static Command createCommand(String command, String description, List<NodeConfig> nodes) throws AgentException {
        final List<Command> commands = new ArrayList<>(nodes.size());
        for (NodeConfig node : nodes) {
            commands.add(SimpleCommand.createCommand(command, node));
        }

        return new MacroCommand(commands, description);
    }

    public List<Command> getCommands() {   // for testing propose
        return commands;
    }
}
