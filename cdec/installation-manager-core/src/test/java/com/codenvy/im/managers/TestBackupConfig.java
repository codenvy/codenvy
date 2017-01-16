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
package com.codenvy.im.managers;

import com.codenvy.im.artifacts.CDECArtifact;

import com.codenvy.im.utils.TarUtils;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/** @author Dmytro Nochevnov  */
public class TestBackupConfig {
    private static final Path ORIGIN_BASE_TMP_DIRECTORY = BackupConfig.BASE_TMP_DIRECTORY;

    private static final String TEST_DEFAULT_BACKUP_DIRECTORY = "target/backups/codenvy";
    private static final Path TEST_BASE_TMP_DIRECTORY = Paths.get("target/tmp_backup/codenvy");

    private static final String TEST_VERSION = "1.0.0";

    @BeforeMethod
    public void setup() throws IOException {
        FileUtils.deleteDirectory(new File(TEST_DEFAULT_BACKUP_DIRECTORY));
        FileUtils.deleteDirectory(TEST_BASE_TMP_DIRECTORY.toFile());

        BackupConfig.BASE_TMP_DIRECTORY = TEST_BASE_TMP_DIRECTORY;
    }

    @AfterMethod
    public void tearDown() {
        BackupConfig.BASE_TMP_DIRECTORY = ORIGIN_BASE_TMP_DIRECTORY;
    }

    @Test
    public void testEmptyInstance() {
        BackupConfig testConfig = new BackupConfig();
        assertEquals(testConfig.toString(), "{'artifactName':'null', " +
                                            "'artifactVersion':'null', " +
                                            "'backupDirectory':'null', " +
                                            "'backupFile':'null'}");

        assertNull(testConfig.getArtifactName());
        assertNull(testConfig.getBackupDirectory());
        assertNull(testConfig.getBackupFile());
    }

    @Test
    public void testEquals() throws InterruptedException {
        BackupConfig backupConfig = new BackupConfig();
        BackupConfig anotherBackupConfig = new BackupConfig();

        assertTrue(backupConfig.equals(anotherBackupConfig));

        backupConfig.setBackupDirectory(TEST_DEFAULT_BACKUP_DIRECTORY);
        anotherBackupConfig.setBackupDirectory(TEST_DEFAULT_BACKUP_DIRECTORY);
        assertTrue(backupConfig.equals(anotherBackupConfig));

        Path backupFile = backupConfig.generateBackupFilePath();
        backupConfig.setBackupFile(backupFile.toString());
        assertFalse(backupConfig.equals(anotherBackupConfig));

        anotherBackupConfig.setBackupFile(backupFile.toString());
        assertTrue(backupConfig.equals(anotherBackupConfig));
    }

    @Test
    public void testInstanceWithArtifact() {
        BackupConfig testConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME);
        assertEquals(testConfig.toString(), "{'artifactName':'codenvy', " +
                                            "'artifactVersion':'null', " +
                                            "'backupDirectory':'null', " +
                                            "'backupFile':'null'}");

        assertTrue(testConfig.hashCode() != 0);
    }

    @Test
    public void testInstanceWithDirectory() {
        BackupConfig testConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                    .setBackupDirectory("testDirectory");
        assertEquals(testConfig.toString(), "{'artifactName':'codenvy', " +
                                            "'artifactVersion':'null', " +
                                            "'backupDirectory':'testDirectory', " +
                                            "'backupFile':'null'}");

        assertTrue(testConfig.hashCode() != 0);
    }

    @Test
    public void testInstanceWithFile() {
        BackupConfig testConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                    .setBackupFile("testFile");
        assertEquals(testConfig.toString(), "{'artifactName':'codenvy', " +
                                            "'artifactVersion':'null', " +
                                            "'backupDirectory':'null', " +
                                            "'backupFile':'testFile'}");

        assertTrue(testConfig.hashCode() != 0);
    }

    @Test
    public void testGetCurrentTime() {
        BackupConfig config = new BackupConfig();
        assertNotNull(config.getCurrentDate());
    }

    @Test
    public void testDefaultFields() {
        assertNotNull(ORIGIN_BASE_TMP_DIRECTORY);
        assertTrue(Files.exists(ORIGIN_BASE_TMP_DIRECTORY.getParent()));
    }

    @Test
    public void testGetArtifactTempDirectory() {
        BackupConfig testConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME);
        assertEquals(testConfig.obtainArtifactTempDirectory().toString(), "target/tmp_backup/codenvy/codenvy");
    }

    @Test
    public void testGetTempDirectory() {
        assertEquals(BackupConfig.getTempDirectory().toString(), "target/tmp_backup/codenvy");
    }

    @Test(dataProvider = "GetComponentTempPath")
    public void testGetComponentTempPath(String parentDir, BackupConfig.Component component, String expectedResult) {
        Path result = BackupConfig.getComponentTempPath(Paths.get(parentDir), component);
        assertEquals(result, Paths.get(expectedResult));
    }

    @DataProvider(name = "GetComponentTempPath")
    public static Object[][] GetComponentTempPath() {
        return new Object[][]{
            {"test", BackupConfig.Component.MONGO, "test/mongo"},
            {"test", BackupConfig.Component.LDAP, "test/ldap/ldap.ldif"},
            {"test", BackupConfig.Component.FS, "test/fs"}
        };
    }

    @Test
    public void testGenerateBackupFilePath() {
        DateFormat df = new SimpleDateFormat(BackupConfig.BACKUP_NAME_TIME_FORMAT);
        Date date = new Date(1278139510000L); // == '07/03/2010 9:45:10'

        BackupConfig spyTestConfig = spy(new BackupConfig().setBackupDirectory(TEST_DEFAULT_BACKUP_DIRECTORY));
        doReturn(date).when(spyTestConfig).getCurrentDate();
        assertEquals(spyTestConfig.generateBackupFilePath(), Paths.get("target/backups/codenvy/backup_" + df.format(date) + ".tar"));
    }

    @Test
    public void testAddGzipExtension() {
        Path result = BackupConfig.addGzipExtension(Paths.get("test/backup.tar"));
        assertEquals(result, Paths.get("test/backup.tar.gz"));
    }

    @Test
    public void testRemoveGzipExtension() {
        Path result = BackupConfig.removeGzipExtension(Paths.get("test/backup.tar.gz"));
        assertEquals(result, Paths.get("test/backup.tar"));
    }

    @Test
    public void testClone() {
        BackupConfig testConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                    .setArtifactVersion(TEST_VERSION)
                                                    .setBackupDirectory("someDir")
                                                    .setBackupFile("someDir/someFile");

        BackupConfig cloneTestConfig = testConfig.clone();
        testConfig.setBackupFile("anotherFile");

        assertEquals(cloneTestConfig.toString(), "{'artifactName':'codenvy', " +
                                                 "'artifactVersion':'1.0.0', " +
                                                 "'backupDirectory':'someDir', " +
                                                 "'backupFile':'someDir/someFile'}");
    }

    @Test
    public void shouldCreateConfigFile() throws IOException {
        BackupConfig testConfig = new BackupConfig().setArtifactName("codenvy")
                                                    .setArtifactVersion("1.0.0");
        Path tmpDir = testConfig.obtainArtifactTempDirectory();
        Files.createDirectories(tmpDir);

        testConfig.createConfigFileInTmpDir();
        Path configFile = tmpDir.resolve(BackupConfig.BACKUP_CONFIG_FILE);
        assertTrue(Files.exists(configFile));

        String storedTestConfigContent = FileUtils.readFileToString(configFile.toFile());
        assertEquals(storedTestConfigContent, "{\n"
                                              + "  \"artifactName\" : \"codenvy\",\n"
                                              + "  \"artifactVersion\" : \"1.0.0\"\n"
                                              + "}");
    }

    @Test
    public void testExtractConfigFromBackup() throws IOException {
        String testingBackup = getClass().getClassLoader().getResource("backups/empty_backup.tar").getPath();

        BackupConfig backupConfig = new BackupConfig().setArtifactName("codenvy")
                                                    .setArtifactVersion("1.0.0")
                                                    .setBackupFile(testingBackup);

        TarUtils.unpackAllFiles(Paths.get(testingBackup), backupConfig.obtainArtifactTempDirectory());

        BackupConfig testConfig = backupConfig.loadConfigFromTempDir();
        assertEquals(testConfig.toString(), "{" +
                                            "'artifactName':'codenvy', " +
                                            "'artifactVersion':'1.0.0', " +
                                            "'backupDirectory':'null', " +
                                            "'backupFile':'null'" +
                                            "}");
    }

    @Test(expectedExceptions = BackupException.class,
          expectedExceptionsMessageRegExp = "There was a problem with config of backup which should be placed in file 'backup_without_config.tar/backup.config.json'. "
                                              + "Error: File 'target/tmp_backup/codenvy/codenvy/backup.config.json' does not exist")
    public void testExtractAbsenceConfigFromBackupError() throws IOException {
        String testingBackup = getClass().getClassLoader().getResource("backups/backup_without_config.tar").getPath();
        BackupConfig backupConfig = new BackupConfig().setArtifactName("codenvy")
                                                      .setArtifactVersion("1.0.0")
                                                      .setBackupFile(testingBackup);
        TarUtils.unpackAllFiles(Paths.get(testingBackup), backupConfig.obtainArtifactTempDirectory());

        backupConfig.loadConfigFromTempDir();
    }

    @Test(expectedExceptions = BackupException.class,
          expectedExceptionsMessageRegExp = "There was a problem with config of backup which should be placed in file 'backup_empty_config.tar/backup.config.json'. "
                                            + "Error: com.fasterxml.jackson.databind.JsonMappingException: No content to map due to end-of-input(?s).*")
    public void testExtractEmptyConfigFromBackupError() throws IOException {
        String testingBackup = getClass().getClassLoader().getResource("backups/backup_empty_config.tar").getPath();
        BackupConfig backupConfig = new BackupConfig().setArtifactName("codenvy")
                                                      .setArtifactVersion("1.0.0")
                                                      .setBackupFile(testingBackup);
        TarUtils.unpackAllFiles(Paths.get(testingBackup), backupConfig.obtainArtifactTempDirectory());

        backupConfig.loadConfigFromTempDir();
    }
}
