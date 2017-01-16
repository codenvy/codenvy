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

import com.codenvy.im.agent.Agent;
import com.codenvy.im.agent.AgentException;
import com.codenvy.im.agent.LocalAgent;
import com.codenvy.im.agent.SecureShellAgent;
import com.codenvy.im.managers.NodeConfig;
import com.codenvy.im.utils.InjectorBootstrap;

import org.eclipse.che.commons.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import static java.lang.String.format;

/** @author Dmytro Nochevnov */
public class SimpleCommand implements Command {
    protected final String description;
    protected final String command;
    protected final Agent  agent;

    private static final Logger LOG = Logger.getLogger(SimpleCommand.class.getSimpleName());
    private final boolean logCommand;

    public SimpleCommand(String command, Agent agent, String description) {
        this(command, agent, description, true);

    }

    public SimpleCommand(String command, Agent agent, String description, boolean logCommand) {
        this.agent = agent;
        this.description = description;
        this.command = command;
        this.logCommand = logCommand;
    }

    /** Factory method to create command which will be executed on the current computer. */
    public static SimpleCommand createCommand(String command) {
        return new SimpleCommand(command, new LocalAgent(), null);
    }

    /** Factory method to create command which will be executed on the current computer without logging info about command at start of executing. */
    public static SimpleCommand createCommandWithoutLogging(String command) {
        return new SimpleCommand(command, new LocalAgent(), null, false);
    }

    /** Factory method to create command which will be executed on remote host. */
    protected static SimpleCommand createCommand(String command,
                                                 final String host,
                                                 final int port,
                                                 final String user,
                                                 final Path privateKeyFilePath,
                                                 boolean logCommand) throws AgentException {
        return new SimpleCommand(command,
                                 new SecureShellAgent(host, port, user, privateKeyFilePath),
                                 null,
                                 logCommand);
    }

    /** Factory method to create command which will be executed on remote node. */
    public static Command createCommand(String command, NodeConfig node) throws AgentException {
        return SimpleCommand.createCommand(command,
                                           node.getHost(),
                                           node.getPort(),
                                           node.getUser(),
                                           node.getPrivateKeyFile(),
                                           true);
    }

    /** Factory method to create command which will be executed on remote node without logging info about command at start of executing. */
    public static Command createCommandWithoutLogging(String command, NodeConfig node) throws AgentException {
        return SimpleCommand.createCommand(command,
                                           node.getHost(),
                                           node.getPort(),
                                           node.getUser(),
                                           node.getPrivateKeyFile(),
                                           false);
    }

    /** {@inheritDoc} */
    @Override
    public String execute() throws CommandException {
        try {
            if (logCommand) {
                LOG.info(toString());
            }

            return agent.execute(command);
        } catch (IOException e) {
            throw makeCommandException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return format("{'command'='%s', 'agent'='%s'}", command, agent);
    }

    protected CommandException makeCommandException(Exception e) {
        String errorMessage = "Command execution fail.";
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            errorMessage += format(" Error: %s", e.getMessage());
        }

        return new CommandException(errorMessage, e);
    }

    /**
     * Utility method. Creates and executes the command.
     * Null will be returned if command fails or empty value is fetched.
     */
    @Nullable
    public static String fetchValue(Class commandClazz) {
        try {
            Command command = (Command)InjectorBootstrap.INJECTOR.getInstance(commandClazz);
            String value = command.execute().trim();
            if (value.isEmpty()) {
                return null;
            }
            return value;
        } catch (CommandException e) {
            return null;
        }
    }
}
