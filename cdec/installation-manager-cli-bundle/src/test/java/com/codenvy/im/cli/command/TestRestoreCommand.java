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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnnov */
public class TestRestoreCommand extends AbstractTestCommand {
    private AbstractIMCommand spyCommand;

    private BackupConfig testBackupConfig;

    private String testBackupFile = "test/backup/directory/backup.tar.gz";
    private String testArtifact   = CDECArtifact.NAME;

    @BeforeMethod
    public void initMocks() throws IOException {
        spyCommand = spy(new RestoreCommand());
        performBaseMocks(spyCommand, true);
    }


    @Test
    public void testRestore() throws Exception {
        testBackupConfig = new BackupConfig().setArtifactName(testArtifact)
                                             .setBackupFile(testBackupFile);

        doReturn(new BackupInfo()).when(mockFacade).restore(testBackupConfig);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("backup", testBackupFile);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();

        assertEquals(output, "{\n" +
                             "  \"backup\" : { },\n" +
                             "  \"status\" : \"OK\"\n" +
                             "}\n");
    }

    @Test
    public void testRestoreThrowsError() throws Exception {
        testBackupConfig = new BackupConfig().setArtifactName(testArtifact)
                                             .setBackupFile(testBackupFile);

        String expectedOutput = "{\n"
                                + "  \"message\" : \"Server Error Exception\",\n"
                                + "  \"status\" : \"ERROR\"\n"
                                + "}";

        doThrow(new RuntimeException("Server Error Exception"))
            .when(mockFacade).restore(testBackupConfig);

        CommandInvoker commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
        commandInvoker.argument("backup", testBackupFile);

        CommandInvoker.Result result = commandInvoker.invoke();
        String output = result.disableAnsi().getOutputStream();
        assertEquals(output, expectedOutput + "\n");
    }
}
