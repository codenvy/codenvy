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
package com.codenvy.im.commands.decorators;

import com.codenvy.im.agent.AgentException;
import com.codenvy.im.commands.Command;
import com.codenvy.im.commands.CommandException;
import com.codenvy.im.commands.CommandLibrary;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.managers.NodeConfig;
import com.codenvy.im.utils.InjectorBootstrap;
import com.google.inject.Key;
import com.google.inject.name.Names;

import org.eclipse.che.commons.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/** @author Dmytro Nochevnov */
public class PuppetErrorInterrupter implements Command {
    public static final int READ_LOG_TIMEOUT_MILLIS = 200;
    public static final int SELECTION_BYTES_NUMBER  = 8192;

    /** minimum number of similar errors at the same node to interrupt installation manager */
    public final static int  MIN_ERROR_EVENTS_TO_INTERRUPT_IM = Integer.parseInt(
        InjectorBootstrap.INJECTOR.getInstance(Key.get(String.class, Names.named("installation-manager.min_puppet_errors_to_interrupt_im"))));
    public static final Path PATH_TO_PUPPET_LOG               = Paths.get("/var/log/puppet/puppet-agent.log");

    private final Command          command;
    private final List<NodeConfig> nodes;
    private final ConfigManager    configManager;

    private static final Logger LOG = Logger.getLogger(PuppetErrorInterrupter.class.getSimpleName());

    private FutureTask<String> task;

    /** Map PuppetError{node, type, date} -> Set<logLine>  */
    protected Map<PuppetError, Set<LocalDateTime>> registeredErrors = new HashMap<>();   // protected access for testing propose

    public PuppetErrorInterrupter(Command command, List<NodeConfig> nodes, ConfigManager configManager) {
        this.command = command;
        this.nodes = nodes;
        this.configManager = configManager;
    }

    public PuppetErrorInterrupter(Command command, ConfigManager configManager) {
        this(command, (List<NodeConfig>) null, configManager);
    }

    public PuppetErrorInterrupter(Command command, NodeConfig node, ConfigManager configManager) {
        this(command, Collections.singletonList(node), configManager);
    }

    @Override
    public String execute() throws CommandException {
        LOG.info(toString());

        task = new FutureTask<>(command::execute);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(task);

        try {
            for (; ; ) {
                if (task.isDone()) {
                    try {
                        return task.get();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e.getMessage(), e);

                    } catch (ExecutionException e) {
                        Exception originException = (Exception)e.getCause();

                        if (originException instanceof CommandException) {
                            throw (CommandException)originException;

                        } else if (originException instanceof RuntimeException) {
                            throw (RuntimeException)originException;
                        }

                        throw new RuntimeException(originException.getMessage(), e);
                    }
                }

                listenToPuppetError();
            }
        } finally {
            executor.shutdown();
        }
    }

    private void listenToPuppetError() throws CommandException {
        lookingForPuppetError(null);

        if (nodes != null) {
            for (NodeConfig node : nodes) {
                lookingForPuppetError(node);
            }
        }
    }

    private void lookingForPuppetError(NodeConfig node) throws CommandException {
        List<String> lastLines = null;
        try {
            lastLines = readNLines(node);
        } catch (AgentException | CommandException e) {
            LOG.log(Level.WARNING, getRuntimeErrorMessage(node, e), e);    // ignore to don't interrupt installation process
        }

        try {
            if (lastLines == null) {
                Thread.sleep(READ_LOG_TIMEOUT_MILLIS);
            } else {
                for (String line : lastLines) {
                    PuppetError error = checkPuppetError(node, line);
                    if (error != null) {
                        task.cancel(false);

                        LOG.log(Level.SEVERE, getPuppetErrorMessageForLog(node, line));

                        Path errorReport = PuppetErrorReport.create(node, getPuppetLogFile());  // do it after the logging into the IM log

                        throw new PuppetErrorException(getMessageForImOutput(error, errorReport));
                    }
                }
            }
        } catch (PuppetErrorException pe) {
            throw pe;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    protected PuppetError checkPuppetError(NodeConfig node, String line) {
        PuppetError error = PuppetError.match(line, node);
        if (error == null) {
            return null;
        }

        LocalDateTime errorTime = PuppetError.getTimeTruncatedToMinutes(line).orElse(null);
        if (errorTime == null) {
            return null;
        }

        Set<LocalDateTime> times = registeredErrors.get(error);
        if (times == null) {
            times = new HashSet<>(Arrays.asList(errorTime));
        }

        times.add(errorTime);
        registeredErrors.put(error, times);

        if (times.size() >= MIN_ERROR_EVENTS_TO_INTERRUPT_IM) {
            return error;
        }

        return null;
    }

    protected List<String> readNLines(NodeConfig node) throws CommandException, AgentException {
        Command readFileCommand = createReadFileCommand(node);

        String allLines = readFileCommand.execute();

        if (allLines == null) {
            return Collections.emptyList();
        }

        String[] lines = allLines.split("[\n]+");
        return Arrays.asList(lines);
    }

    /**
     * If node != null : create command to read from certain node of multi-node codenvy
     * If node == null : create command to read from single-node codenvy
     */
    protected Command createReadFileCommand(@Nullable NodeConfig node) throws AgentException {
        if (node == null) {
            return CommandLibrary.createTailCommand(getPuppetLogFile(), SELECTION_BYTES_NUMBER, useSudo());
        } else {
            return CommandLibrary.createTailCommand(getPuppetLogFile(), SELECTION_BYTES_NUMBER, node, useSudo());
        }
    }

    private String getPuppetErrorMessageForLog(@Nullable NodeConfig node, String line) {
        return (node == null) ? format("Puppet error: '%s'", line) :
               format("Puppet error at the %s node '%s': '%s'", node.getType(), node.getHost(), line);
    }


    private String getMessageForImOutput(PuppetError error, Path errorReport) throws IOException {
        NodeConfig node = error.getNode();
        String shortErrorMessage = error.getShortMessage();

        String puppetErrorMessage = (node == null) ? format("Puppet error: '%s'", shortErrorMessage) :
                                    format("Puppet error at the %s node '%s': '%s'", node.getType(), node.getHost(), shortErrorMessage);

        Config codenvyConfig = configManager.loadInstalledCodenvyConfig();
        String hostUrl = codenvyConfig.getHostUrl();
        InstallType installType = configManager.detectInstallationType();
        String docsUrlToken = installType == InstallType.SINGLE_SERVER ? "single" : "multi";

        return puppetErrorMessage + format(". At the time puppet is continue Codenvy installation in background and is trying to fix this issue."
                                           +
                                           " Check administrator dashboard page http://%s/admin to verify installation success."
                                           + " If the installation eventually fails, contact support with error report %s."
                                           +
                                           " Installation & Troubleshooting Docs: http://docs.codenvy" +
                                           ".com/onpremises/installation-%s-node/#install-troubleshooting.",
                                           hostUrl,
                                           errorReport.toString(),
                                           docsUrlToken
                                          );
    }

    private String getRuntimeErrorMessage(@Nullable NodeConfig node, IOException e) {
        if (node == null) {
            return format("It is impossible to read puppet log locally: %s", e.getMessage());
        }

        return format("It is impossible to read puppet log at the node '%s': %s", node.getHost(), e.getMessage());
    }

    @Override
    public String getDescription() {
        return "Puppet error interrupter";
    }

    @Override
    public String toString() {
        if (nodes == null) {
            return format("PuppetErrorInterrupter{ %s }; looking on errors in file %s locally", command.toString(), getPuppetLogFile());
        }

        return format("PuppetErrorInterrupter{ %s }; looking on errors in file %s locally and at the nodes: %s", command.toString(), getPuppetLogFile(),
                      nodes.toString());
    }
    
    /** for testing propose */
    protected Path getPuppetLogFile() {
        return PATH_TO_PUPPET_LOG;
    }

    /** for testing propose */
    protected boolean useSudo() {
        return true;
    }

    public static boolean isReadPuppetLogCommand(String command) {
        return command.contains(PATH_TO_PUPPET_LOG.toString());
    }
}
