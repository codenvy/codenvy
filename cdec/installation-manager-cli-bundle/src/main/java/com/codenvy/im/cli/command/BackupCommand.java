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
package com.codenvy.im.cli.command;

import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.managers.BackupConfig;
import com.codenvy.im.response.BackupInfo;
import com.codenvy.im.response.BackupResponse;
import com.codenvy.im.response.ResponseCode;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * @author Dmytro Nochevnov
 */
@Command(scope = "codenvy", name = "backup", description = "Backup Codenvy")
public class BackupCommand extends AbstractIMCommand {

    @Argument(name = "directory", description = "Relative path to directory with backup. Default location is ~/.codenvy/backups/", required = false, multiValued = false, index = 0)
    private String backupDirectory;

    @Override
    protected void doExecuteCommand() throws Exception {
        String artifactToBackup = CDECArtifact.NAME;
        BackupConfig config = new BackupConfig().setArtifactName(artifactToBackup);

        if (backupDirectory != null && !backupDirectory.isEmpty()) {
            config.setBackupDirectory(backupDirectory);
        }

        try {
            getConsole().showProgressor();

            BackupInfo backupInfo = getFacade().backup(config);

            BackupResponse backupResponse = new BackupResponse();
            backupResponse.setBackup(backupInfo);
            backupResponse.setStatus(ResponseCode.OK);

            getConsole().printResponseExitInError(backupResponse);
        } finally {
            getConsole().hideProgressor();
        }
    }
}
