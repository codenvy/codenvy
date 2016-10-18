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
package com.codenvy.workspace;

import com.codenvy.machine.backup.WorkspaceIdHashLocationFinder;
import com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Component to launch cleanUp workspace files script.
 *
 * @author Alexander Andrienko
 */
@Singleton
public class WorkspaceFilesCleanUpScriptExecutor implements WorkspaceFilesCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceFilesCleanUpScriptExecutor.class);

    private final WorkspaceIdHashLocationFinder workspaceIdHashLocationFinder;
    private final File                          backupsRootDir;
    private final int                           cleanUpTimeOut;
    private final String                        workspaceCleanUpScript;

    @Inject
    public WorkspaceFilesCleanUpScriptExecutor(WorkspaceIdHashLocationFinder workspaceIdHashLocationFinder,
                                               @Named("che.user.workspaces.storage") File backupsRootDir,
                                               @Named("workspace.projects_storage.cleanup.script_path") String workspaceCleanUpScript,
                                               @Named("workspace.projects_storage.cleanup.timeout_seconds") int cleanUpTimeOut) {
        this.workspaceIdHashLocationFinder = workspaceIdHashLocationFinder;
        this.backupsRootDir = backupsRootDir;
        this.workspaceCleanUpScript = workspaceCleanUpScript;
        this.cleanUpTimeOut = cleanUpTimeOut;
    }

    /**
     * Execute workspace cleanUp script.
     *
     * @param workspace
     *         to cleanUp files.
     * @throws IOException
     *         in case I/O error.
     * @throws ServerException
     *         in case internal server error.
     */
    @Override
    public void clear(Workspace workspace) throws IOException, ServerException {
        File wsFolder = workspaceIdHashLocationFinder.calculateDirPath(backupsRootDir, workspace.getId());
        CommandLine commandLine = new CommandLine(workspaceCleanUpScript, wsFolder.getAbsolutePath());

        try {
            execute(commandLine.asArray(), cleanUpTimeOut);
        } catch (InterruptedException | TimeoutException e) {
            throw new ServerException(format("Failed to delete workspace files by path: '%s' for workspace with id: '%s'",
                                             wsFolder.getAbsolutePath(), workspace.getId()), e);
        }
    }

    @VisibleForTesting
    void execute(String[] commandLine, int timeout) throws TimeoutException, IOException, InterruptedException {
        final ListLineConsumer outputConsumer = new ListLineConsumer();
        Process process = ProcessUtil.executeAndWait(commandLine, timeout, SECONDS, outputConsumer);

        if (process.exitValue() != 0) {
            LOG.error(outputConsumer.getText());
            throw new IOException("Process failed. Exit code " + process.exitValue());
        }
    }
}
