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
import com.codenvy.im.commands.CommandLibrary;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.managers.NodeConfig;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codenvy.im.commands.decorators.PuppetErrorInterrupter.READ_LOG_TIMEOUT_MILLIS;
import static com.codenvy.im.commands.decorators.PuppetErrorReport.Constants;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/** @author Dmytro Nochevnov */
public abstract class BaseTestPuppetErrorInterrupter {
    static final int MOCK_COMMAND_TIMEOUT_MILLIS = READ_LOG_TIMEOUT_MILLIS * 16;

    static final Path BASE_TMP_DIRECTORY   = Paths.get("target/tmp").toAbsolutePath();
    static final Path REPORT_TMP_DIRECTORY = Paths.get("target/tmp/report").toAbsolutePath();
    static final Path TEST_TMP_DIRECTORY   = Paths.get("target/tmp/test").toAbsolutePath();
    static final Path LOG_TMP_DIRECTORY    = Paths.get("target/tmp/log").toAbsolutePath();

    static final Constants ORIGIN_REPORT_CONSTANTS = PuppetErrorReport.getConstants();

    @Mock
    Command mockCommand;

    @Mock
    ConfigManager mockConfigManager;

    PuppetErrorInterrupter spyInterrupter;

    Constants spyReportConstants;

    String logWithoutErrorMessages =
        "2015-06-08 14:53:53 test puppet-master[5409]: Compiled catalog for test.com in environment production in 0.13 seconds\n"
        + "2015-06-08 14:53:55 test puppet-agent[22276]: Finished catalog run in 1.98 seconds\n"
        + "2015-06-08 15:17:31 test systemd[1]: Time has been changed\n"
        + "2015-06-08 15:17:40 test puppet-master[5409]: Compiled catalog for test.com in environment production in 0.13 seconds\n"
        + "2015-06-08 15:17:42 test puppet-agent[22754]: Finished catalog run in 1.83 seconds\n"
        + "2015-06-08 15:22:40 test puppet-master[5409]: Compiled catalog for test.com in environment production in 0.23 seconds\n"
        + "2015-06-08 15:22:42 test puppet-agent[23240]: Finished catalog run in 1.95 seconds\n"
        + "2015-06-08 15:27:40 test puppet-master[5409]: Compiled catalog for test.com in environment production in 0.12 seconds\n"
        + "2015-06-08 15:27:42 test puppet-agent[23713]: Finished catalog run in 2.01 seconds\n"
        + "2015-06-08 15:51:51 test systemd[1]: Time has been changed\n"
        + "2015-06-08 15:51:57 test puppet-master[5409]: Compiled catalog for test.com in environment production in 0.13 seconds\n"
        + "2015-06-08 15:52:00 test puppet-agent[24198]: Finished catalog run in 2.04 seconds\n"
        + "2015-06-08 15:56:57 test puppet-master[5409]: Compiled catalog for test.com in environment production in 0.13 seconds\n"
        + "2015-06-08 15:56:59 test puppet-agent[24672]: Finished catalog run in 1.67 seconds\n";

    @BeforeMethod
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        FileUtils.deleteDirectory(BASE_TMP_DIRECTORY.toFile());

        createDirectory(BASE_TMP_DIRECTORY);
        createDirectory(REPORT_TMP_DIRECTORY);
        createDirectory(LOG_TMP_DIRECTORY);
        createDirectory(TEST_TMP_DIRECTORY);

        spyInterrupter = getSpyInterrupter();
        when(spyInterrupter.useSudo()).thenReturn(false);   // prevents asking sudo password when running the tests locally

        // create local puppet log file
        Path puppetLogFile = LOG_TMP_DIRECTORY.resolve(spyInterrupter.getPuppetLogFile().getFileName());
        FileUtils.write(puppetLogFile.toFile(), logWithoutErrorMessages);
        when(spyInterrupter.getPuppetLogFile()).thenReturn(puppetLogFile);

        // create IM CLI client log
        Path imLogFile = TEST_TMP_DIRECTORY.resolve(ORIGIN_REPORT_CONSTANTS.getCliLog().getFileName());
        FileUtils.write(imLogFile.toFile(), "");

        // create constants
        spyReportConstants = spy(new Constants());
        doReturn(REPORT_TMP_DIRECTORY).when(spyReportConstants).getBaseTmpDir();
        doReturn(imLogFile).when(spyReportConstants).getCliLog();
        doReturn(false).when(spyReportConstants).useSudo();   // prevents asking sudo password when running the tests locally

        PuppetErrorReport.setConstants(spyReportConstants);

        // prepare Codenvy Config
        doReturn(getInstallType()).when(mockConfigManager).detectInstallationType();
        doReturn(new Config(ImmutableMap.of(
            Config.HOST_URL, "localhost",
            Config.ADMIN_LDAP_USER_NAME, "admin",
            Config.ADMIN_LDAP_PASSWORD, "password"
        )))
            .when(mockConfigManager).loadInstalledCodenvyConfig();
    }

    abstract public PuppetErrorInterrupter getSpyInterrupter();

    abstract public InstallType getInstallType();

    abstract public PuppetError getTestPuppetError();

    @AfterMethod
    public void tearDown() throws InterruptedException, IOException {
        PuppetErrorReport.setConstants(ORIGIN_REPORT_CONSTANTS);

        deleteDirectory(BASE_TMP_DIRECTORY.toFile());
    }

    protected void assertLocalErrorReport(String errorMessage, String expectedContentOfLogFile) throws IOException, InterruptedException {
        Pattern errorReportInfoPattern = Pattern.compile("target/reports/error_report_.*.tar.gz");
        Matcher pathToReportMatcher = errorReportInfoPattern.matcher(errorMessage);
        assertTrue(pathToReportMatcher.find());

        Path report = Paths.get(pathToReportMatcher.group());
        assertNotNull(report);
        assertTrue(exists(report));

        CommandLibrary.createUnpackCommand(report, TEST_TMP_DIRECTORY).execute();
        Path puppetLogFile = TEST_TMP_DIRECTORY.resolve(spyInterrupter.getPuppetLogFile().getFileName());
        assertTrue(exists(puppetLogFile));
        String puppetLogFileContent = FileUtils.readFileToString(puppetLogFile.toFile());
        assertEquals(puppetLogFileContent, expectedContentOfLogFile);

        Path imLogfile = TEST_TMP_DIRECTORY.resolve(spyReportConstants.getCliLog().getFileName());
        assertTrue(exists(imLogfile));
    }


    protected void assertNodeErrorReport(String errorMessage, String expectedContentOfLogFile, NodeConfig testNode)
        throws IOException, InterruptedException {
        Pattern errorReportInfoPattern = Pattern.compile("target/reports/error_report_.*.tar.gz");
        Matcher pathToReportMatcher = errorReportInfoPattern.matcher(errorMessage);
        assertTrue(pathToReportMatcher.find());

        Path report = Paths.get(pathToReportMatcher.group());
        assertNotNull(report);
        assertTrue(exists(report));

        CommandLibrary.createUnpackCommand(report, TEST_TMP_DIRECTORY).execute();
        Path puppetLogFile =
            TEST_TMP_DIRECTORY.resolve(testNode.getType().toString().toLowerCase()).resolve(spyInterrupter.getPuppetLogFile().getFileName());
        assertTrue(exists(puppetLogFile));

        String logFileContent = FileUtils.readFileToString(puppetLogFile.toFile());
        assertEquals(logFileContent, expectedContentOfLogFile);
    }

    protected void checkPuppetErrors(String puppetLog, PuppetError expectedError) {
        List<String> lines = Arrays.asList(puppetLog.split("\n"));

        PuppetError error = null;
        for (String line : lines) {
            error = spyInterrupter.checkPuppetError(getTestNode(), line);
            if (error != null) {
                break;
            }
        }

        assertEquals(error, expectedError);
    }

    @Test(dataProvider = "getDataToCheckPuppetError")
    public void testCheckPuppetError(String puppetLog, PuppetError expectedError) {
        checkPuppetErrors(puppetLog, expectedError);
    }

    @DataProvider
    public Object[][] getDataToCheckPuppetError() {
        return new Object[][] {
            {// 1 batch of error messages caused by the same error "Dependency Exec[import_base_schema] has failures: true"
             "2015-10-07 16:44:57 +0100 Puppet (err): Command exceeded timeout\n"
             + "Wrapped exception:\n"
             + "execution expired\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_schema]/returns (err): change from notrun to 0 failed: Command exceeded timeout\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_images] (notice): Dependency Exec[import_base_schema] has failures: true\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_images] (warning): Skipping because of failed dependencies\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_data] (notice): Dependency Exec[import_base_schema] has failures: true\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_data] (warning): Skipping because of failed dependencies\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[disable_listen_80_port] (notice): Dependency Exec[import_base_schema] has failures: true\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[disable_listen_80_port] (warning): Skipping because of failed dependencies\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/File[/etc/zabbix/zabbix_java_gateway.conf]/content (notice): \n"
             + "--- /etc/zabbix/zabbix_java_gateway.conf\t2014-11-02 08:56:09.000000000 +0000",
             null},

            // 5 batches of 5 different errors
            {"2015-07-30 13:13:34 +0100 /File[/var/lib/puppet/lib] (err): Failed to generate additional resources using 'eval_generate': getaddrinfo: Name or service not known\n"
             + "2015-07-30 13:13:34 +0100 /File[/var/lib/puppet/lib] (err): Could not evaluate: Could not retrieve file metadata for puppet://puppet/plugins: getaddrinfo: Name or service not known\n"
             + "Wrapped exception:\n"
             + "getaddrinfo: Name or service not known\n"
             + "2015-07-29 16:12:52 +0100 /Stage[main]/Third_party::Openldap_servers::Package/Package[openldap-servers] (notice): Dependency Package[openldap] has failures: true\n"
             + "2015-07-30 16:14:35 +0100 Puppet (err): Could not retrieve catalog from remote server: getaddrinfo: Name or service not known\n"
             + "2015-07-29 16:15:52 +0100 /Stage[main]/Third_party::another (notice): Dependency Package[another] has failures: true\n"
             + "2015-07-29 16:16:52 +0100 /Stage[main]/Third_party::yet-another (notice): Dependency Package[yet-another] has failures: true\n",
             null},

            // 3 batches of error messages caused by the same error "Dependency Exec[import_base_schema] has failures: true"
            {"2015-10-07 16:44:57 +0100 Puppet (err): Command exceeded timeout\n"
             + "Wrapped exception:\n"
             + "execution expired\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_schema]/returns (err): change from notrun to 0 failed: Command exceeded timeout\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_images] (notice): Dependency Exec[import_base_schema] has failures: true\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_images] (warning): Skipping because of failed dependencies\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_data] (notice): Dependency Exec[import_base_schema] has failures: true\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_data] (warning): Skipping because of failed dependencies\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[disable_listen_80_port] (notice): Dependency Exec[import_base_schema] has failures: true\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[disable_listen_80_port] (warning): Skipping because of failed dependencies\n"
             + "2015-10-07 16:44:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/File[/etc/zabbix/zabbix_java_gateway.conf]/content (notice): \n"
             + "--- /etc/zabbix/zabbix_java_gateway.conf\t2014-11-02 08:56:09.000000000 +0000"
             + "2015-10-07 16:45:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_schema]/returns (err): change from notrun to 0 failed: Command exceeded timeout\n"
             + "2015-10-07 16:45:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_images] (notice): Dependency Exec[import_base_schema] has failures: true\n"
             + "2015-10-07 16:45:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_images] (warning): Skipping because of failed dependencies\n"
             + "2015-10-07 16:45:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_data] (notice): Dependency Exec[import_base_schema] has failures: true\n"
             + "2015-10-07 16:45:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_data] (warning): Skipping because of failed dependencies\n"
             + "2015-10-07 16:45:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[disable_listen_80_port] (notice): Dependency Exec[import_base_schema] has failures: true\n"
             + "2015-10-07 16:45:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[disable_listen_80_port] (warning): Skipping because of failed dependencies\n"
             + "2015-10-07 16:45:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/File[/etc/zabbix/zabbix_java_gateway.conf]/content (notice): \n"
             + "--- /etc/zabbix/zabbix_java_gateway.conf\t2014-11-02 08:56:09.000000000 +0000"
             + "2015-10-07 16:46:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_schema]/returns (err): change from notrun to 0 failed: Command exceeded timeout\n"
             + "2015-10-07 16:46:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_images] (notice): Dependency Exec[import_base_schema] has failures: true\n"
             + "2015-10-07 16:46:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_images] (warning): Skipping because of failed dependencies\n"
             + "2015-10-07 16:46:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_data] (notice): Dependency Exec[import_base_schema] has failures: true\n"
             + "2015-10-07 16:46:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[import_base_data] (warning): Skipping because of failed dependencies\n"
             + "2015-10-07 16:46:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[disable_listen_80_port] (notice): Dependency Exec[import_base_schema] has failures: true\n"
             + "2015-10-07 16:46:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/Exec[disable_listen_80_port] (warning): Skipping because of failed dependencies\n"
             + "2015-10-07 16:46:57 +0100 /Stage[main]/Third_party::Zabbix::Server_config/File[/etc/zabbix/zabbix_java_gateway.conf]/content (notice): \n",
             getTestPuppetError()}
        };
    }

    abstract public NodeConfig getTestNode();

}
