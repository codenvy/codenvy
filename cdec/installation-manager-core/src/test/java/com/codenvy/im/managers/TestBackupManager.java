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
package com.codenvy.im.managers;

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.commands.Command;
import com.codenvy.im.commands.SimpleCommand;
import com.codenvy.im.utils.TarUtils;
import com.codenvy.im.utils.Version;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.String.format;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/** @author Dmytro Nochevnov */
public class TestBackupManager {

    @Mock
    private ConfigManager mockConfigManager;
    @Mock
    private CDECArtifact  mockCdecArtifact;
    @Mock
    private Command       mockCommand;

    private BackupManager spyManager;

    private static final String ORIGIN_BACKUP_NAME_TIME_FORMAT = BackupConfig.BACKUP_NAME_TIME_FORMAT;

    private static final Path   TEST_DEFAULT_BACKUP_DIRECTORY = Paths.get("target/backups");
    private static final Path   TEST_BASE_TMP_DIRECTORY       = Paths.get("target/tmp_backup");
    private static final String TEST_BACKUP_NAME_TIME_FORMAT  = "dd-MMM-yyyy";

    private static final String TEST_VERSION = "1.0.0";

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        FileUtils.deleteDirectory(TEST_DEFAULT_BACKUP_DIRECTORY.toFile());
        FileUtils.deleteDirectory(TEST_BASE_TMP_DIRECTORY.toFile());

        BackupConfig.BACKUP_NAME_TIME_FORMAT = TEST_BACKUP_NAME_TIME_FORMAT;

        spyManager = spy(new BackupManager(TEST_DEFAULT_BACKUP_DIRECTORY.toString()));
        doReturn(mockCdecArtifact).when(spyManager).getArtifact(CDECArtifact.NAME);

        doReturn(CDECArtifact.NAME).when(mockCdecArtifact).getName();
        doReturn(Optional.of(Version.valueOf("1.0.0"))).when(mockCdecArtifact).getInstalledVersion();
    }

    @AfterMethod
    public void tearDown() throws IOException {
        BackupConfig.BACKUP_NAME_TIME_FORMAT = ORIGIN_BACKUP_NAME_TIME_FORMAT;
    }

    @Test
    public void testBackupCodenvy() throws IOException {
        BackupConfig initialBackupConfig = new BackupConfig().setArtifactName("codenvy")
                                                             .setBackupDirectory(TEST_DEFAULT_BACKUP_DIRECTORY.toString());

        BackupConfig expectedBackupConfig = initialBackupConfig.clone()
                                                               .setBackupFile(initialBackupConfig.generateBackupFilePath().toString())
                                                               .setArtifactVersion(TEST_VERSION);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                BackupConfig backupConfig = (BackupConfig)invocationOnMock.getArguments()[0];
                return SimpleCommand.createCommand(
                        format("echo '' > %s", backupConfig.getBackupFile().toString()));  // create empty backup file for testing propose
            }
        }).when(mockCdecArtifact).getBackupCommand(expectedBackupConfig);

        BackupConfig result = spyManager.backup(initialBackupConfig);

        expectedBackupConfig.setBackupFile(BackupConfig.addGzipExtension(Paths.get(expectedBackupConfig.getBackupFile())).toString());  // expect backup file name with added gzip extension
        assertEquals(result, expectedBackupConfig);
        assertTrue(Files.exists(Paths.get(result.getBackupFile())));
    }

    @Test(expectedExceptions = BackupException.class,
          expectedExceptionsMessageRegExp = "error")
    public void testBackupException() throws IOException {
        BackupConfig initialBackupConfig = new BackupConfig().setArtifactName("codenvy")
                                                             .setBackupDirectory(TEST_DEFAULT_BACKUP_DIRECTORY.toString());

        BackupConfig expectedBackupConfig = initialBackupConfig.setBackupFile(initialBackupConfig.generateBackupFilePath().toString())
                                                               .setArtifactVersion(TEST_VERSION);

        doThrow(new IOException("error")).when(mockCdecArtifact).getBackupCommand(expectedBackupConfig);
        spyManager.backup(initialBackupConfig);
    }


    @Test(expectedExceptions = BackupException.class,
          expectedExceptionsMessageRegExp = "Artifact version is unavailable")
    public void testBackupNullArtifactVersionException() throws IOException {
        BackupConfig backupConfig = new BackupConfig().setArtifactName("codenvy");
        doReturn(Optional.empty()).when(mockCdecArtifact).getInstalledVersion();
        spyManager.backup(backupConfig);
    }

    @Test
    public void testRestoreCodenvy() throws IOException {
        Path backupFile = TEST_DEFAULT_BACKUP_DIRECTORY.resolve("testBackup.tar");
        Path compressedBackupFile = prepareCompressedBackup(backupFile);

        BackupConfig initialBackupConfig = new BackupConfig().setArtifactName("codenvy")
                                                             .setBackupFile(compressedBackupFile.toString());

        BackupConfig expectedBackupConfig = new BackupConfig().setArtifactName("codenvy");

        Path expectedBackupDirectory = expectedBackupConfig.obtainArtifactTempDirectory();
        expectedBackupConfig.setBackupDirectory(expectedBackupDirectory.toString());

        Path tempBackupFile = expectedBackupDirectory.resolve(backupFile.getFileName().toString());
        expectedBackupConfig.setBackupFile(tempBackupFile.toString());

        doReturn(mockCommand).when(mockCdecArtifact).getRestoreCommand(expectedBackupConfig);

        spyManager.restore(initialBackupConfig);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Backup file is unknown.")
    public void testRestoreNullBackupFilename() throws IOException {
        BackupConfig initialBackupConfig = new BackupConfig().setArtifactName("codenvy");

        spyManager.restore(initialBackupConfig);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Backup file is unknown.")
    public void testRestoreEmptyBackupFileName() throws IOException {
        BackupConfig initialBackupConfig = new BackupConfig().setArtifactName("codenvy")
                                                             .setBackupFile("");

        spyManager.restore(initialBackupConfig);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Backup file 'non-exists' doesn't exist.")
    public void testRestoreNonExistsBackup() throws IOException {
        BackupConfig initialBackupConfig = new BackupConfig().setArtifactName("codenvy")
                                                             .setBackupFile("non-exists");

        spyManager.restore(initialBackupConfig);
    }

    @Test(expectedExceptions = BackupException.class,
          expectedExceptionsMessageRegExp = "error")
    public void testRestoreIOException() throws IOException {
        Path compressedBackupFile = prepareCompressedBackup(TEST_DEFAULT_BACKUP_DIRECTORY.resolve("testBackup.tar"));
        BackupConfig initialBackupConfig = new BackupConfig().setArtifactName("codenvy")
                                                             .setBackupFile(compressedBackupFile.toString());

        doThrow(new IOException("error")).when(mockCdecArtifact).getRestoreCommand(any(BackupConfig.class));
        spyManager.restore(initialBackupConfig);
    }


    @Test(expectedExceptions = BackupException.class,
          expectedExceptionsMessageRegExp = "error")
    public void testRestoreBackupException() throws IOException {
        Path compressedBackupFile = prepareCompressedBackup(TEST_DEFAULT_BACKUP_DIRECTORY.resolve("testBackup.tar"));

        BackupConfig spyBackupConfig = spy(new BackupConfig().setArtifactName("codenvy")
                                                             .setBackupFile(compressedBackupFile.toString()));

        doReturn(spyBackupConfig).when(spyBackupConfig).clone();
        doThrow(new BackupException("error")).when(spyBackupConfig).loadConfigFromTempDir();

        spyManager.restore(spyBackupConfig);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "error")
    public void testRestoreIllegalArgumentException() throws IOException {
        Path compressedBackupFile = prepareCompressedBackup(TEST_DEFAULT_BACKUP_DIRECTORY.resolve("testBackup.tar"));

        BackupConfig spyBackupConfig = spy(new BackupConfig().setArtifactName("codenvy")
                                                             .setBackupFile(compressedBackupFile.toString()));

        doReturn(spyBackupConfig).when(spyBackupConfig).clone();
        doReturn(spyBackupConfig).when(spyBackupConfig).loadConfigFromTempDir();
        doThrow(new IllegalArgumentException("error")).when(spyManager).checkBackup(mockCdecArtifact, spyBackupConfig);

        spyManager.restore(spyBackupConfig);
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = "error")
    public void testRestoreIllegalStateException() throws IOException {
        Path compressedBackupFile = prepareCompressedBackup(TEST_DEFAULT_BACKUP_DIRECTORY.resolve("testBackup.tar"));

        BackupConfig spyBackupConfig = spy(new BackupConfig().setArtifactName("codenvy")
                                                             .setBackupFile(compressedBackupFile.toString()));

        doReturn(spyBackupConfig).when(spyBackupConfig).clone();
        doReturn(spyBackupConfig).when(spyBackupConfig).loadConfigFromTempDir();
        doThrow(new IllegalStateException("error")).when(spyManager).checkBackup(mockCdecArtifact, spyBackupConfig);

        spyManager.restore(spyBackupConfig);
    }

    @Test
    public void testGetArtifact() throws IOException {
        BackupManager manager = new BackupManager(TEST_DEFAULT_BACKUP_DIRECTORY.toString());
        Artifact result = manager.getArtifact("codenvy");
        assertEquals(result.getName(), "codenvy");
    }

    @Test
    public void testCheckBackupSuccessful() {
        BackupConfig checkingConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                              .setArtifactVersion(TEST_VERSION);

        spyManager.checkBackup(mockCdecArtifact, checkingConfig);
    }

    @Test(dataProvider = "CheckBackupIllegalArgumentExceptionData")
    public void testCheckBackupIllegalArgumentException(BackupConfig checkingConfig, String expectedExceptionMessage) {
        try {
            spyManager.checkBackup(mockCdecArtifact, checkingConfig);
        } catch(IllegalArgumentException e) {
            assertEquals(e.getMessage(), expectedExceptionMessage);
            return;
        }

        fail(format("Here should be IllegalArgumentException with message '%s'", expectedExceptionMessage));
    }

    @DataProvider(name = "CheckBackupIllegalArgumentExceptionData")
    public Object[][] GetCheckBackupIllegalArgumentExceptionData() {
        return new Object[][] {
            {new BackupConfig().setArtifactName("anotherArtifact"),
             "Backed up artifact 'anotherArtifact' doesn't equal to restoring artifact 'codenvy'"},

            {new BackupConfig().setArtifactName("codenvy").setArtifactVersion("0.0.1"),
             "Version of backed up artifact '0.0.1' doesn't equal to restoring version '1.0.0'"},
        };
    }

    @Test
    public void testCheckBackupIllegalStateException() throws IOException {
        BackupConfig checkingConfig = new BackupConfig().setArtifactName("codenvy");

        String expectedExceptionMessage = "It is impossible to get version of restoring artifact 'codenvy'";

        doReturn(Optional.empty()).when(mockCdecArtifact).getInstalledVersion();
        try {
            spyManager.checkBackup(mockCdecArtifact, checkingConfig);
            fail("Here should be IllegalStateException.");
        } catch(IllegalStateException e) {
            assertEquals(e.getMessage(), expectedExceptionMessage);
        }

        doThrow(new IOException()).when(mockCdecArtifact).getInstalledVersion();
        try {
            spyManager.checkBackup(mockCdecArtifact, checkingConfig);
            fail("Here should be IllegalStateException.");
        } catch(IllegalStateException e) {
            assertEquals(e.getMessage(), expectedExceptionMessage);
        }
    }

    private Path prepareCompressedBackup(Path backupFile) throws IOException {
        String backupConfigJson = "{\n"
                                  + "  \"artifactName\" : \"codenvy\",\n"
                                  + "  \"artifactVersion\" : \"1.0.0\"\n"
                                  + "}";
        Path backupConfigFile = backupFile.getParent().resolve(BackupConfig.BACKUP_CONFIG_FILE);
        FileUtils.writeStringToFile(backupConfigFile.toFile(), backupConfigJson);
        TarUtils.packFile(backupConfigFile, backupFile);

        Path compressedBackupFile = BackupConfig.addGzipExtension(backupFile);
        TarUtils.compressFile(backupFile, compressedBackupFile);
        return compressedBackupFile;
    }
}
