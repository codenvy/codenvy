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

import com.codenvy.im.commands.Command;
import com.codenvy.im.commands.MacroCommand;
import com.codenvy.im.managers.NodeConfig;
import com.codenvy.im.utils.InjectorBootstrap;
import com.google.inject.Key;
import com.google.inject.name.Names;

import org.apache.commons.io.FileUtils;

import org.eclipse.che.commons.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codenvy.im.commands.CommandLibrary.createChmodCommand;
import static com.codenvy.im.commands.CommandLibrary.createCopyCommand;
import static com.codenvy.im.commands.CommandLibrary.createCopyFromRemoteToLocalCommand;
import static com.codenvy.im.commands.CommandLibrary.createPackCommand;
import static com.codenvy.im.commands.SimpleCommand.createCommand;
import static java.lang.String.format;

/** @author Dmytro Nochevnov */
public class PuppetErrorReport {
    private static final Logger LOG = Logger.getLogger(PuppetErrorReport.class.getSimpleName());

    private static Constants constants = new Constants();

    /**
     * @return path to created error report, or null in case of problems with report creation
     */
    @Nullable
    public static Path create(@Nullable final NodeConfig node, Path puppetLogFile) {
        final Path report = constants.getPathToErrorReport();

        try {
            // re-create local temp dir
            FileUtils.deleteQuietly(constants.getBaseTmpDir().toFile());
            Files.createDirectory(constants.getBaseTmpDir());

            // create report dir
            if (!Files.exists(report.getParent())) {
                Files.createDirectory(report.getParent());
            }

            Command createReportCommands = createConsolidateLogsCommand(report, node, puppetLogFile);
            createReportCommands.execute();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, format("Error report creation error: %s", e.getMessage()), e);
            return null;
        } finally {
            // remove temp dir
            FileUtils.deleteQuietly(constants.getBaseTmpDir().toFile());
        }

        return report;
    }

    /**
     * Given:
     * - path to report file
     * - node config
     *
     * @return command which:
     * I. If node == null:
     * - copy puppet log file into local temp dir
     * - copy installation manager logs
     * - pack temp dir into report file
     * <p/>
     * II. If node != null:
     * - create local temp dir for puppet log file from node with name = type_of_node
     * - create remote temp dir
     * - copy file with puppet logs into the remote temp dir
     * - change permission of file with puppet logs to the 666 to be able to copy it to local machine
     * - copy remote puppet log file into the local_temp_dir/{node_type}/ directory
     * - cleanup remote temp dir
     * - copy installation manager logs
     * - pack local temp dir into report file
     */
    private static Command createConsolidateLogsCommand(Path reportFile, @Nullable NodeConfig node, Path puppetLogFile) throws IOException {
        List<Command> commands = new ArrayList<>();

        if (node == null) {
            // copy puppet log file into temp dir
            commands.add(createCopyCommand(puppetLogFile, constants.getBaseTmpDir(), constants.useSudo()));

            // change permission of puppet log file with puppet logs to the 666 to be able to pack it
            Path copyPuppetLogFile = constants.getBaseTmpDir().resolve(puppetLogFile.getFileName());
            commands.add(createChmodCommand("666", copyPuppetLogFile, constants.useSudo()));

        } else {
            // create local temp dir for puppet log file from node with name = type_of_node
            Path tempDirForNodeLog = constants.getBaseTmpDir().resolve(node.getType().toString().toLowerCase());
            commands.add(createCommand(format("mkdir -p %s", tempDirForNodeLog)));

            // create remote temp dir
            Path remoteTempDir = Paths.get("/tmp/codenvy");
            commands.add(createCommand(format("mkdir -p %s", remoteTempDir), node));

            // copy file with puppet logs into the remote temp dir
            commands.add(createCopyCommand(puppetLogFile, remoteTempDir, node, constants.useSudo()));

            // change permission of file with puppet logs to the 666 to be able to copy it to local machine
            Path remoteLogFile = remoteTempDir.resolve(puppetLogFile.getFileName());
            commands.add(createChmodCommand("666", remoteLogFile, node, constants.useSudo()));

            // copy remote puppet log file into the local_temp_dir/{node_type}/ directory
            commands.add(createCopyFromRemoteToLocalCommand(remoteLogFile,
                                                            tempDirForNodeLog,
                                                            node));

            // cleanup remote temp dir
            commands.add(createCommand(format("rm -rf %s", remoteTempDir), node));
        }

        // copy installation manager logs
        if (Files.exists(constants.getCliLog())) {
            commands.add(createCopyCommand(constants.getCliLog(), constants.getBaseTmpDir()));
        }

        if (constants.getInstallationManagerServerLog() != null && Files.exists(constants.getInstallationManagerServerLog())) {
            commands.add(createCopyCommand(constants.getInstallationManagerServerLog(), constants.getBaseTmpDir()));
        }

        // pack local temp dir into report file
        commands.add(createPackCommand(constants.getBaseTmpDir(), reportFile, ".", false));

        return new MacroCommand(commands, "Commands to create error report");
    }

    protected static void setConstants(Constants newConstants) {
        constants = newConstants;
    }

    protected static Constants getConstants() {
        return constants;
    }

    protected static class Constants {
        private final Path   REPORT_DIR              = Paths.get(InjectorBootstrap.INJECTOR.getInstance(Key.get(String.class, Names.named("installation-manager.report_dir"))));
        private final String REPORT_NAME_TEMPLATE    = "error_report_%s.tar.gz";
        private final String REPORT_NAME_TIME_FORMAT = "dd-MMM-yyyy_HH-mm-ss";
        private final Path   BASE_TMP_DIR            = Paths.get(System.getProperty("java.io.tmpdir")).resolve("codenvy");

        protected Path getBaseTmpDir() {
            return BASE_TMP_DIR;
        }

        Path getCliLog() {
            return getBaseTmpDir().getParent().resolve("cli.log");
        }

        protected Path getInstallationManagerServerLog() {
            return (System.getProperty("catalina.base") != null) ?
                Paths.get(System.getProperty("catalina.base")).resolve("logs").resolve("catalina.out")
                : null;
        }

        private Path getPathToErrorReport() {
            DateFormat dateFormat = new SimpleDateFormat(REPORT_NAME_TIME_FORMAT);
            String currentTime = dateFormat.format(new Date());

            String fileName = format(REPORT_NAME_TEMPLATE, currentTime);

            return REPORT_DIR.resolve(fileName);
        }

        /** for testing propose */
        protected boolean useSudo() {
            return true;
        }
    }

}
