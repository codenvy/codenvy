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
@Command(scope = "codenvy", name = "restore", description = "Restore Codenvy")
public class RestoreCommand extends AbstractIMCommand {

    @Argument(name = "backup", description = "Relative path to backup file. There should be <backup_name>.tar.gz file with the compressed " +
                                             "<backup_name>.tar file with the same backup name. For example: backup file " +
                                             "codenvy_backup_19-Mar-2015_16-52-06.tar.gz which consists of codenvy_backup_19-Mar-2015_16-52-06" +
                                             ".tar", required = true, multiValued = false, index = 0)
    private String backup;

    @Override
    protected void doExecuteCommand() throws Exception {
        try {
            BackupConfig config = new BackupConfig();
            config.setArtifactName(CDECArtifact.NAME);
            config.setBackupFile(backup);

            getConsole().showProgressor();

            BackupInfo backupInfo = getFacade().restore(config);

            BackupResponse backupResponse = new BackupResponse();
            backupResponse.setBackup(backupInfo);
            backupResponse.setStatus(ResponseCode.OK);

            getConsole().printResponseExitInError(backupResponse);
        } finally {
            getConsole().hideProgressor();
        }
    }
}
