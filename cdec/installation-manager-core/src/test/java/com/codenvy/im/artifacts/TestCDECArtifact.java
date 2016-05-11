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
package com.codenvy.im.artifacts;

import com.codenvy.im.BaseTest;
import com.codenvy.im.agent.AgentException;
import com.codenvy.im.artifacts.helper.CDECArtifactHelper;
import com.codenvy.im.artifacts.helper.CDECMultiServerHelper;
import com.codenvy.im.artifacts.helper.CDECSingleServerHelper;
import com.codenvy.im.commands.Command;
import com.codenvy.im.commands.CommandException;
import com.codenvy.im.commands.MacroCommand;
import com.codenvy.im.managers.BackupConfig;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.managers.NodeManager;
import com.codenvy.im.managers.PropertiesNotFoundException;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.OSUtils;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.nio.file.Files.createFile;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
public class TestCDECArtifact extends BaseTest {
    public static final String INITIAL_OS_VERSION = OSUtils.VERSION;
    public static final String SYSTEM_USER_NAME   = System.getProperty("user.name");

    public static final String BASE_TMP_DIR      = "target/" + TestCDECArtifact.class.getSimpleName();
    public static final String TMP_CODENVY       = BASE_TMP_DIR + "/tmp/codenvy";
    public static final String ETC_PUPPET        = BASE_TMP_DIR + "/etc/puppet";
    public static final String CODENVY_3_VERSION = "3.0.0";
    public static final String CODENVY_4_VERSION = "4.0.0-beta-2";

    public static Integer k;

    @Mock
    private HttpTransport mockTransport;
    @Mock
    private Command       mockCommand;
    @Mock
    private NodeManager   mockNodeManager;

    private CDECArtifactHelper spyCDECSingleServerHelper;
    private CDECArtifactHelper spyCDECMultiServerHelper;
    private CDECArtifact       spyCdecArtifact;
    private ConfigManager      spyConfigManager;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);

        spyConfigManager = spy(new ConfigManager(UPDATE_API_ENDPOINT, ETC_PUPPET, mockTransport));
        spyCdecArtifact = spy(new CDECArtifact(UPDATE_API_ENDPOINT, DOWNLOAD_DIR, ASSEMBLY_PROPERTIES, mockTransport, spyConfigManager, mockNodeManager));
        spyCDECSingleServerHelper = spy(new CDECSingleServerHelper(spyCdecArtifact, spyConfigManager));
        spyCDECMultiServerHelper = spy(new CDECMultiServerHelper(spyCdecArtifact, spyConfigManager));

        doReturn(ImmutableList.of(Paths.get(ETC_PUPPET, Config.MULTI_SERVER_CUSTOM_CONFIG_PP),
                                  Paths.get(ETC_PUPPET, Config.MULTI_SERVER_BASE_CONFIG_PP)).iterator())
                .when(spyConfigManager).getCodenvyPropertiesFiles(InstallType.MULTI_SERVER);
        doReturn(ImmutableList.of(Paths.get(TMP_CODENVY, Config.MULTI_SERVER_CUSTOM_CONFIG_PP),
                                  Paths.get(TMP_CODENVY, Config.MULTI_SERVER_BASE_CONFIG_PP)).iterator())
                .when(spyConfigManager).getCodenvyPropertiesFiles(TMP_CODENVY, InstallType.MULTI_SERVER);

        doReturn(ImmutableList.of(Paths.get(TMP_CODENVY, Config.SINGLE_SERVER_BASE_CONFIG_PP),
                                  Paths.get(TMP_CODENVY, Config.SINGLE_SERVER_PP)).iterator())
                .when(spyConfigManager).getCodenvyPropertiesFiles(TMP_CODENVY, InstallType.SINGLE_SERVER);

        // doAnswer is needed to test getting commands to update CDEC config where spyConfigManager.getCodenvyPropertiesFiles() method is being called twice
        doAnswer(inv -> ImmutableList.of(Paths.get(ETC_PUPPET, Config.SINGLE_SERVER_BASE_CONFIG_PP),
                                         Paths.get(ETC_PUPPET, Config.SINGLE_SERVER_PP)).iterator())
                .when(spyConfigManager).getCodenvyPropertiesFiles(InstallType.SINGLE_SERVER);

        FileUtils.forceMkdir(Paths.get(ETC_PUPPET, Config.SINGLE_SERVER_BASE_CONFIG_PP).getParent().toFile());
        createFile(Paths.get(ETC_PUPPET, Config.SINGLE_SERVER_BASE_CONFIG_PP));
        createFile(Paths.get(ETC_PUPPET, Config.SINGLE_SERVER_PP));

        FileUtils.forceMkdir(Paths.get(ETC_PUPPET, Config.MULTI_SERVER_CUSTOM_CONFIG_PP).getParent().toFile());
        createFile(Paths.get(ETC_PUPPET, Config.MULTI_SERVER_CUSTOM_CONFIG_PP));
        createFile(Paths.get(ETC_PUPPET, Config.MULTI_SERVER_BASE_CONFIG_PP));

        doReturn(spyCDECSingleServerHelper).when(spyCdecArtifact).getHelper(InstallType.SINGLE_SERVER);
        doReturn(spyCDECMultiServerHelper).when(spyCdecArtifact).getHelper(InstallType.MULTI_SERVER);

        doReturn(TMP_CODENVY).when(spyCDECSingleServerHelper).getTmpCodenvyDir();
        doReturn(TMP_CODENVY).when(spyCDECMultiServerHelper).getTmpCodenvyDir();
        FileUtils.writeStringToFile(Paths.get(TMP_CODENVY, "patches", InstallType.SINGLE_SERVER.toString().toLowerCase(), "patch_before_update.sh").toFile(),
                                    "echo -n \"$test_property1\"");
        FileUtils.writeStringToFile(Paths.get(TMP_CODENVY, "patches", InstallType.MULTI_SERVER.toString().toLowerCase(), "patch_before_update.sh").toFile(),
                                    "echo -n \"$test_property1\"");

        doReturn(ETC_PUPPET).when(spyCDECSingleServerHelper).getPuppetDir();
        doReturn(ETC_PUPPET).when(spyCDECMultiServerHelper).getPuppetDir();
        FileUtils.writeStringToFile(Paths.get(ETC_PUPPET, "patches", InstallType.SINGLE_SERVER.toString().toLowerCase(), "patch_after_update.sh").toFile(),
                                    "echo -n \"$test_property1\"");
        FileUtils.writeStringToFile(Paths.get(ETC_PUPPET, "patches", InstallType.MULTI_SERVER.toString().toLowerCase(), "patch_after_update.sh").toFile(),
                                    "echo -n \"$test_property1\"");

        OSUtils.VERSION = "7";
    }

    @Test
    public void testGetInstallSingleServerInfo() throws Exception {
        InstallOptions options = new InstallOptions();
        options.setInstallType(InstallType.SINGLE_SERVER);
        options.setStep(0);

        List<String> info = spyCdecArtifact.getInstallInfo(InstallType.SINGLE_SERVER);
        assertNotNull(info);
        assertTrue(info.size() > 1);
    }

    @Test
    public void testGetInstallMultiServerInfo() throws Exception {
        List<String> info = spyCdecArtifact.getInstallInfo(InstallType.MULTI_SERVER);
        assertNotNull(info);
        assertTrue(info.size() > 1);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Codenvy On-Prem can be installed on CentOS 7 or RHEL 7 only")
    public void testGetInstallOnWrongOsVersion6() throws Exception {
        OSUtils.VERSION = "6";
        spyCdecArtifact.getInstallCommand(null, null, null);
    }

    @Test
    public void testGetInstallSingleServerCommandOsVersion7() throws Exception {
        InstallOptions options = new InstallOptions();
        options.setInstallType(InstallType.SINGLE_SERVER);
        options.setConfigProperties(ImmutableMap.of("some property", "some value", Config.HOST_URL, "host_url"));

        int steps = spyCdecArtifact.getInstallInfo(InstallType.SINGLE_SERVER).size();
        for (int i = 0; i < steps; i++) {
            options.setStep(i);
            Command command = spyCdecArtifact.getInstallCommand(null, Paths.get("some path"), options);
            assertNotNull(command);
        }
    }

    @Test
    public void testInstallPuppetCommandsForSingleServer() throws IOException {
        final String installVersionStr = "4.0.0";
        Version versionToInstall = Version.valueOf(installVersionStr);
        Path pathToBinaries = Paths.get("some path");

        InstallOptions options = new InstallOptions();
        options.setInstallType(InstallType.SINGLE_SERVER);
        options.setConfigProperties(ImmutableMap.of("some property", "some value", Config.HOST_URL, "host_url"));

        // check install puppet commands
        List<Command> commands = ((MacroCommand) spyCdecArtifact.getInstallCommand(versionToInstall, pathToBinaries, options.setStep(1))).getCommands();
        assertEquals(commands.size(), 8);
        assertEquals(commands.toString(), "[{'command'='if ! sudo grep -Eq \"127.0.0.1.*puppet\" /etc/hosts; then\n"
                                          + " echo '\n"
                                          + "127.0.0.1 puppet' | sudo tee --append /etc/hosts > /dev/null\n"
                                          + "fi', 'agent'='LocalAgent'}, {'command'='if ! sudo grep -Eq \" host_url$\" /etc/hosts; then\n"
                                          + "  echo \"\n"
                                          + "127.0.0.1 host_url\" | sudo tee --append /etc/hosts > /dev/null\n"
                                          + "fi', 'agent'='LocalAgent'}, "
                                          + "{'command'='yum clean all', 'agent'='LocalAgent'}, "
                                          + "{'command'='if [ \"`yum list installed | grep puppetlabs-release`\" == \"\" ]; then  sudo -E yum -y -q install https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm; fi', 'agent'='LocalAgent'}, "
                                          + "{'command'=' sudo -E yum -y -q install puppet-server-3.8.6-1.el7.noarch', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo systemctl enable puppetmaster', 'agent'='LocalAgent'}, "
                                          + "{'command'=' sudo -E yum -y -q install puppet-3.8.6-1.el7.noarch', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo systemctl enable puppet', 'agent'='LocalAgent'}]");

        // test installing puppet with proxy settings
        final String testHttpProxy = "http://proxy.net:8080";
        final String testHttpsProxy = "https://proxy.net:8080";
        final ImmutableMap<String, Object> environment = ImmutableMap.of(Config.HTTP_PROXY, testHttpProxy,
                                                                         Config.HTTPS_PROXY, testHttpsProxy);
        doReturn(environment).when(spyConfigManager).getEnvironment();
        commands = ((MacroCommand) spyCdecArtifact.getInstallCommand(versionToInstall, pathToBinaries, options.setStep(1))).getCommands();
        assertEquals(commands.size(), 8);
        assertEquals(commands.toString(), "[{'command'='if ! sudo grep -Eq \"127.0.0.1.*puppet\" /etc/hosts; then\n"
                                          + " echo '\n"
                                          + "127.0.0.1 puppet' | sudo tee --append /etc/hosts > /dev/null\n"
                                          + "fi', 'agent'='LocalAgent'}, "
                                          + "{'command'='if ! sudo grep -Eq \" host_url$\" /etc/hosts; then\n"
                                          + "  echo \"\n"
                                          + "127.0.0.1 host_url\" | sudo tee --append /etc/hosts > /dev/null\n"
                                          + "fi', 'agent'='LocalAgent'}, "
                                          + "{'command'='yum clean all', 'agent'='LocalAgent'}, "
                                          + "{'command'='if [ \"`yum list installed | grep puppetlabs-release`\" == \"\" ]; then export http_proxy=\"http://proxy.net:8080\"; export https_proxy=\"https://proxy.net:8080\";  sudo -E yum -y -q install https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm; fi', 'agent'='LocalAgent'}, "
                                          + "{'command'='export http_proxy=\"http://proxy.net:8080\"; export https_proxy=\"https://proxy.net:8080\";  sudo -E yum -y -q install puppet-server-3.8.6-1.el7.noarch', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo systemctl enable puppetmaster', 'agent'='LocalAgent'}, "
                                          + "{'command'='export http_proxy=\"http://proxy.net:8080\"; export https_proxy=\"https://proxy.net:8080\";  sudo -E yum -y -q install puppet-3.8.6-1.el7.noarch', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo systemctl enable puppet', 'agent'='LocalAgent'}]");
    }

    @Test
    public void testInstallPuppetCommandsForMultiServer() throws Exception {
        final String installVersionStr = "4.0.0";
        Version versionToInstall = Version.valueOf(installVersionStr);
        Path pathToBinaries = Paths.get("some path");

        prepareMultiNodeEnv(spyConfigManager, mockTransport);

        InstallOptions options = new InstallOptions();
        options.setConfigProperties(getTestMultiNodeProperties());
        options.setInstallType(InstallType.MULTI_SERVER);

        // check install puppet commands
        List<Command> commands = ((MacroCommand) spyCdecArtifact.getInstallCommand(versionToInstall, pathToBinaries, options.setStep(1))).getCommands();
        assertEquals(commands.size(), 10);
        assertEquals(commands.toString(), format("[{'command'='yum clean all', 'agent'='LocalAgent'}, "
                                          + "{'command'='if [ \"`yum list installed | grep puppetlabs-release`\" == \"\" ]; then  sudo -E yum -y -q install https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm; fi', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo yum -y -q install puppet-server-3.8.6-1.el7.noarch', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo systemctl enable puppetmaster', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo yum -y -q install puppet-3.8.6-1.el7.noarch', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo systemctl enable puppet', 'agent'='LocalAgent'}, [{'command'='yum clean all', 'agent'='{'host'='data.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='yum clean all', 'agent'='{'host'='analytics.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='yum clean all', 'agent'='{'host'='api.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}], [{'command'='if [ \"`yum list installed | grep puppetlabs-release`\" == \"\" ]; then sudo yum -y -q install https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm; fi', 'agent'='{'host'='data.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='if [ \"`yum list installed | grep puppetlabs-release`\" == \"\" ]; then sudo yum -y -q install https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm; fi', 'agent'='{'host'='analytics.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='if [ \"`yum list installed | grep puppetlabs-release`\" == \"\" ]; then sudo yum -y -q install https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm; fi', 'agent'='{'host'='api.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}], [{'command'='sudo yum -y -q install puppet-3.8.6-1.el7.noarch', 'agent'='{'host'='data.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='sudo yum -y -q install puppet-3.8.6-1.el7.noarch', 'agent'='{'host'='analytics.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='sudo yum -y -q install puppet-3.8.6-1.el7.noarch', 'agent'='{'host'='api.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}], [{'command'='sudo systemctl enable puppet', 'agent'='{'host'='data.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='sudo systemctl enable puppet', 'agent'='{'host'='analytics.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='sudo systemctl enable puppet', 'agent'='{'host'='api.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}]]", SYSTEM_USER_NAME));

        // test installing puppet with proxy settings
        final String testHttpProxy = "http://proxy.net:8080";
        final String testHttpsProxy = "https://proxy.net:8080";
        final ImmutableMap<String, Object> environment = ImmutableMap.of(Config.HTTP_PROXY, testHttpProxy,
                                                                         Config.HTTPS_PROXY, testHttpsProxy);
        doReturn(environment).when(spyConfigManager).getEnvironment();
        commands = ((MacroCommand) spyCdecArtifact.getInstallCommand(versionToInstall, pathToBinaries, options.setStep(1))).getCommands();
        assertEquals(commands.size(), 10);
        assertEquals(commands.toString(), format("[{'command'='yum clean all', 'agent'='LocalAgent'}, "
                                          + "{'command'='if [ \"`yum list installed | grep puppetlabs-release`\" == \"\" ]; then export http_proxy = http://proxy.net:8080; export https_proxy = https://proxy.net:8080;  sudo -E yum -y -q install https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm; fi', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo yum -y -q install puppet-server-3.8.6-1.el7.noarch', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo systemctl enable puppetmaster', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo yum -y -q install puppet-3.8.6-1.el7.noarch', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo systemctl enable puppet', 'agent'='LocalAgent'}, [{'command'='yum clean all', 'agent'='{'host'='data.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='yum clean all', 'agent'='{'host'='analytics.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='yum clean all', 'agent'='{'host'='api.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}], [{'command'='if [ \"`yum list installed | grep puppetlabs-release`\" == \"\" ]; then sudo yum -y -q install https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm; fi', 'agent'='{'host'='data.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='if [ \"`yum list installed | grep puppetlabs-release`\" == \"\" ]; then sudo yum -y -q install https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm; fi', 'agent'='{'host'='analytics.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='if [ \"`yum list installed | grep puppetlabs-release`\" == \"\" ]; then sudo yum -y -q install https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm; fi', 'agent'='{'host'='api.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}], [{'command'='sudo yum -y -q install puppet-3.8.6-1.el7.noarch', 'agent'='{'host'='data.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='sudo yum -y -q install puppet-3.8.6-1.el7.noarch', 'agent'='{'host'='analytics.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='sudo yum -y -q install puppet-3.8.6-1.el7.noarch', 'agent'='{'host'='api.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}], [{'command'='sudo systemctl enable puppet', 'agent'='{'host'='data.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='sudo systemctl enable puppet', 'agent'='{'host'='analytics.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, "
                                          + "{'command'='sudo systemctl enable puppet', 'agent'='{'host'='api.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}]]", SYSTEM_USER_NAME));
    }


    @Test
    public void testConfigurePuppetAgentForSingleServer() throws IOException {
        final String installVersionStr = "4.0.0";
        Version versionToInstall = Version.valueOf(installVersionStr);
        Path pathToBinaries = Paths.get("some path");

        InstallOptions options = new InstallOptions();
        options.setInstallType(InstallType.SINGLE_SERVER);
        options.setConfigProperties(ImmutableMap.of("some property", "some value", Config.HOST_URL, "host_url"));

        // check install puppet commands
        List<Command> commands = ((MacroCommand) spyCdecArtifact.getInstallCommand(versionToInstall, pathToBinaries, options.setStep(4))).getCommands();
        assertEquals(commands.size(), 4);
        assertEquals(commands.toString(), "[{'command'='if sudo test -f /etc/puppet/puppet.conf; then     if ! sudo test -f /etc/puppet/puppet.conf.back; then         sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back;     else         sudo cp /etc/puppet/puppet.conf.back /etc/puppet/puppet.conf;     fi fi', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo sed -i \"s/\\[main\\]/\\[main\\]\\n  dns_alt_names = puppet,host_url\\n/g\" /etc/puppet/puppet.conf', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo sed -i \"s/\\[agent\\]/[master]\\n  certname = host_url\\n\\n\\[agent\\]\\n  show_diff = true\\n  pluginsync = true\\n  report = true\\n  default_schedules = false\\n  certname = host_url\\n  runinterval = 300\\n  configtimeout = 600\\n  server = host_url\\n/g\" /etc/puppet/puppet.conf', 'agent'='LocalAgent'}, "
                                          + "{'command'='sudo sh -c 'echo -e \"\\nPUPPET_EXTRA_OPTS=--logdest /var/log/puppet/puppet-agent.log\\n\" >> /etc/sysconfig/puppetagent'', 'agent'='LocalAgent'}]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetInstallSingleServerCommandError() throws Exception {
        InstallOptions options = new InstallOptions();
        options.setConfigProperties(Collections.<String, String>emptyMap());
        options.setInstallType(InstallType.SINGLE_SERVER);
        options.setStep(Integer.MAX_VALUE);

        spyCdecArtifact.getInstallCommand(null, Paths.get("some path"), options);
    }

    @Test
    public void testGetInstallMultiServerCommandsForMultiServer() throws Exception {
        InstallOptions options = new InstallOptions();
        options.setInstallType(InstallType.MULTI_SERVER);
        options.setConfigProperties(ImmutableMap.of("site_host_name", "site.example.com",
                                                    "puppet_master_host_name", "puppet-master.example.com"));

        int steps = spyCdecArtifact.getInstallInfo(InstallType.MULTI_SERVER).size();
        for (int i = 0; i < steps; i++) {
            options.setStep(i);
            Command command = spyCdecArtifact.getInstallCommand(null, Paths.get("some path"), options);
            assertNotNull(command);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Site node config not found.")
    public void testGetInstallMultiServerCommandsForMultiServerError() throws Exception {
        InstallOptions options = new InstallOptions();
        options.setInstallType(InstallType.MULTI_SERVER);
        options.setConfigProperties(ImmutableMap.of("some property", "some value",
                                                    "puppet_master_host_name", "puppet-master.example.com"));

        int steps = spyCdecArtifact.getInstallInfo(InstallType.MULTI_SERVER).size();
        for (int i = 0; i < steps; i++) {
            options.setStep(i);
            Command command = spyCdecArtifact.getInstallCommand(null, Paths.get("some path"), options);
            assertNotNull(command);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Step number .* is out of install range")
    public void testGetInstallMultiServerCommandUnexistedStepError() throws Exception {
        InstallOptions options = new InstallOptions();
        options.setConfigProperties(Collections.<String, String>emptyMap());
        options.setInstallType(InstallType.MULTI_SERVER);
        options.setStep(Integer.MAX_VALUE);

        spyCdecArtifact.getInstallCommand(null, Paths.get("some path"), options);
    }

    @Test
    public void testGetUpdateSingleServerCommand() throws Exception {
        prepareSingleNodeEnv(spyConfigManager, mockTransport);

        InstallOptions options = new InstallOptions();
        options.setConfigProperties(new HashMap() {{
            put("some property", "some value");
            put(Config.HOST_URL, "host_url");
        }});
        options.setInstallType(InstallType.SINGLE_SERVER);

        final String updateVersion = "4.0.0";
        Version versionToUpdate = Version.valueOf(updateVersion);
        Path pathToBinaries = Paths.get("some path");
        assertEquals(spyCdecArtifact.getUpdateCommand(versionToUpdate, pathToBinaries, options.setStep(0)).toString(),
                     format("[{'command'='rm -rf %1$s; mkdir %1$s; unzip -o some path -d %1$s; ', 'agent'='LocalAgent'}, "
                            + "{'command'='/bin/systemctl status crond.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop crond.service; fi; ', 'agent'='LocalAgent'}, "
                            + "{'command'='/bin/systemctl status puppet.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop puppet.service; fi; ', 'agent'='LocalAgent'}, "
                            + "{'command'='/bin/systemctl status codenvy.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop codenvy.service; fi; ', 'agent'='LocalAgent'}]",
                            TMP_CODENVY));

        List<Command> commands = ((MacroCommand) spyCdecArtifact.getUpdateCommand(versionToUpdate, pathToBinaries, options.setStep(1))).getCommands();
        assertEquals(commands.size(), 8);
        k = 0;

        assertTrue(commands.get(k).toString().matches(format("\\{'command'='sudo cp %1$s/manifests/nodes/single_server/base_config.pp %1$s/manifests/nodes/single_server/base_config.pp.back ; sudo cp %1$s/manifests/nodes/single_server/base_config.pp %1$s/manifests/nodes/single_server/base_config.pp.back.\\d+ ; ', 'agent'='LocalAgent'\\}", ETC_PUPPET)),
                                                      "Actual command: " + commands.get(k++).toString());

        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/single_server/base_config.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|YOUR_DNS_NAME|host_url|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/single_server/base_config.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/single_server/base_config.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$host_url *= *\"[^\"]*\"|$host_url = \"host_url\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/single_server/base_config.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/single_server/base_config.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$some property *= *\"[^\"]*\"|$some property = \"some value\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/single_server/base_config.pp', 'agent'='LocalAgent'}", TMP_CODENVY));

        assertTrue(commands.get(k).toString().matches(format("\\{'command'='sudo cp %1$s/manifests/nodes/single_server/single_server.pp %1$s/manifests/nodes/single_server/single_server.pp.back ; sudo cp %1$s/manifests/nodes/single_server/single_server.pp %1$s/manifests/nodes/single_server/single_server.pp.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}", ETC_PUPPET)),
                   "Actual command: " + commands.get(k++).toString());

        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/single_server/single_server.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|YOUR_DNS_NAME|host_url|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/single_server/single_server.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/single_server/single_server.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$host_url *= *\"[^\"]*\"|$host_url = \"host_url\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/single_server/single_server.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/single_server/single_server.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$some property *= *\"[^\"]*\"|$some property = \"some value\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/single_server/single_server.pp', 'agent'='LocalAgent'}", TMP_CODENVY));

        doReturn(ImmutableList.of(Paths.get(TMP_CODENVY, Config.SINGLE_SERVER_BASE_CONFIG_PP),
                                  Paths.get(TMP_CODENVY, Config.SINGLE_SERVER_PP)).iterator())
            .when(spyConfigManager).getCodenvyPropertiesFiles(TMP_CODENVY, InstallType.SINGLE_SERVER);
        assertEquals(spyCdecArtifact.getUpdateCommand(versionToUpdate, pathToBinaries, options.setStep(2)).toString(),
                     format("[{'command'='sudo cat %1$s/patches/single_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$host_url|host_url|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/single_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat %1$s/patches/single_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$some property|some value|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/single_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat %1$s/patches/single_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$PATH_TO_MANIFEST|%1$s/manifests/nodes/single_server/base_config.pp|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/single_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='bash %1$s/patches/single_server/patch_before_update.sh', 'agent'='LocalAgent'}]", TMP_CODENVY));

        options.setConfigProperties(new HashMap() {{
            put("some property", "some value");
            put(Config.HOST_URL, "host_url");
        }});
        assertEquals(spyCdecArtifact.getUpdateCommand(versionToUpdate, pathToBinaries, options.setStep(3)).toString(),
                     format("[{'command'='sudo rm -rf %1$s/files; sudo rm -rf %1$s/modules; sudo rm -rf %1$s/patches; sudo cp -rf %2$s/* %1$s; sudo rm -rf %2$s', 'agent'='LocalAgent'}, " 
                            + "{'command'='/bin/systemctl status puppet.service; if [ $? -ne 0 ]; then   sudo /bin/systemctl start puppet.service; fi; ', 'agent'='LocalAgent'}]", ETC_PUPPET, TMP_CODENVY));

        assertEquals(spyCdecArtifact.getUpdateCommand(versionToUpdate, pathToBinaries, options.setStep(4)).toString(),
                     "PuppetErrorInterrupter{ Expected to be installed 'codenvy' of the version '4.0.0' }; looking on errors in file /var/log/puppet/puppet-agent.log locally");

        assertEquals(spyCdecArtifact.getUpdateCommand(versionToUpdate, pathToBinaries, options.setStep(5)).toString(),
                     format("[{'command'='sudo cat %1$s/patches/single_server/patch_after_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$host_url|host_url|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/single_server/patch_after_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat %1$s/patches/single_server/patch_after_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$some property|some value|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/single_server/patch_after_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='bash %1$s/patches/single_server/patch_after_update.sh', 'agent'='LocalAgent'}]", ETC_PUPPET, TMP_CODENVY));
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Codenvy On-Prem can be updated on CentOS 7 only")
    public void testGetUpdateCommandOnWrongOS6() throws Exception {
        OSUtils.VERSION = "6";
        spyCdecArtifact.getUpdateCommand(null, null, null);
    }

    @Test
    public void testGetUpdateMultiServerCommand() throws Exception {
        prepareMultiNodeEnv(spyConfigManager, mockTransport);

        InstallOptions options = new InstallOptions();
        options.setConfigProperties(getTestMultiNodeProperties());
        options.setInstallType(InstallType.MULTI_SERVER);

        final String updateVersion = "4.0.0";
        Version versionToUpdate = Version.valueOf(updateVersion);
        Path pathToBinaries = Paths.get("some path");
        assertEquals(spyCdecArtifact.getUpdateCommand(versionToUpdate, pathToBinaries, options.setStep(0)).toString(),
                     format("[{'command'='rm -rf %1$s; mkdir %1$s; unzip -o some path -d %1$s; ', 'agent'='LocalAgent'}, "
                            + "{'command'='/bin/systemctl status crond.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop crond.service; fi; ', 'agent'='LocalAgent'}, "
                            + "{'command'='/bin/systemctl status puppet.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop puppet.service; fi; ', 'agent'='LocalAgent'}, "
                            + "{'command'='/bin/systemctl status codenvy.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop codenvy.service; fi; ', 'agent'='LocalAgent'}]",
                     TMP_CODENVY));

        List<Command> commands = ((MacroCommand) spyCdecArtifact.getUpdateCommand(versionToUpdate, pathToBinaries, options.setStep(1))).getCommands();
        assertEquals(commands.size(), 18);
        k = 0;

        assertTrue(commands.get(k).toString().matches(format("\\{'command'='sudo cp %1$s/manifests/nodes/multi_server/custom_configurations.pp %1$s/manifests/nodes/multi_server/custom_configurations.pp.back ; sudo cp %1$s/manifests/nodes/multi_server/custom_configurations.pp %1$s/manifests/nodes/multi_server/custom_configurations.pp.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}", ETC_PUPPET)),
                                                      "Actual command: " + commands.get(k++).toString());

        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/custom_configurations.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$data_host_name *= *\"[^\"]*\"|$data_host_name = \"data.example.com\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/custom_configurations.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/custom_configurations.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$puppet_master_host_name *= *\"[^\"]*\"|$puppet_master_host_name = \"master.example.com\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/custom_configurations.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/custom_configurations.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$host_url *= *\"[^\"]*\"|$host_url = \"hostname\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/custom_configurations.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/custom_configurations.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$api_host_name *= *\"[^\"]*\"|$api_host_name = \"api.example.com\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/custom_configurations.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/custom_configurations.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$analytics_host_name *= *\"[^\"]*\"|$analytics_host_name = \"analytics.example.com\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/custom_configurations.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/custom_configurations.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$version *= *\"[^\"]*\"|$version = \"3.3.0\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/custom_configurations.pp', 'agent'='LocalAgent'}", TMP_CODENVY));

        assertTrue(commands.get(k).toString().matches(format("\\{'command'='sudo cp %1$s/manifests/nodes/multi_server/base_configurations.pp %1$s/manifests/nodes/multi_server/base_configurations.pp.back ; sudo cp %1$s/manifests/nodes/multi_server/base_configurations.pp %1$s/manifests/nodes/multi_server/base_configurations.pp.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}", ETC_PUPPET)),
                                                      "Actual command: " + commands.get(k++).toString());

        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/base_configurations.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$data_host_name *= *\"[^\"]*\"|$data_host_name = \"data.example.com\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/base_configurations.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/base_configurations.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$puppet_master_host_name *= *\"[^\"]*\"|$puppet_master_host_name = \"master.example.com\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/base_configurations.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/base_configurations.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$host_url *= *\"[^\"]*\"|$host_url = \"hostname\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/base_configurations.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/base_configurations.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$api_host_name *= *\"[^\"]*\"|$api_host_name = \"api.example.com\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/base_configurations.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/base_configurations.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$analytics_host_name *= *\"[^\"]*\"|$analytics_host_name = \"analytics.example.com\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/base_configurations.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/base_configurations.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$version *= *\"[^\"]*\"|$version = \"3.3.0\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/base_configurations.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/nodes.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|api.example.com|api.example.com|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/nodes.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/nodes.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|data.example.com|data.example.com|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/nodes.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/nodes.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|analytics.example.com|analytics.example.com|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/nodes.pp', 'agent'='LocalAgent'}", TMP_CODENVY));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/nodes.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|puppet-master.example.com|master.example.com|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/nodes.pp', 'agent'='LocalAgent'}", TMP_CODENVY));

        assertEquals(spyCdecArtifact.getUpdateCommand(versionToUpdate, pathToBinaries, options.setStep(2)).toString(),
                     format("[{'command'='sudo cat %1$s/patches/multi_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$data_host_name|data.example.com|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat %1$s/patches/multi_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$puppet_master_host_name|master.example.com|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat %1$s/patches/multi_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$host_url|hostname|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat %1$s/patches/multi_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$api_host_name|api.example.com|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat %1$s/patches/multi_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$PATH_TO_MANIFEST|%1$s/manifests/nodes/single_server/base_config.pp|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat %1$s/patches/multi_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$analytics_host_name|analytics.example.com|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='sudo cat %1$s/patches/multi_server/patch_before_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$version|3.3.0|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}, "
                            + "{'command'='bash %1$s/patches/multi_server/patch_before_update.sh', 'agent'='LocalAgent'}]", TMP_CODENVY));

        options.setConfigProperties(getTestMultiNodeProperties());
        assertEquals(spyCdecArtifact.getUpdateCommand(versionToUpdate, pathToBinaries, options.setStep(3)).toString(),
                     format("[{'command'='sudo rm -rf %1$s/files; sudo rm -rf %1$s/modules; sudo rm -rf %1$s/patches; sudo cp -rf %2$s/* %1$s; sudo rm -rf %2$s', 'agent'='LocalAgent'}, {'command'='/bin/systemctl status puppet.service; if [ $? -ne 0 ]; then   sudo /bin/systemctl start puppet.service; fi; ', 'agent'='LocalAgent'}]", ETC_PUPPET, TMP_CODENVY));

        assertEquals(spyCdecArtifact.getUpdateCommand(versionToUpdate, pathToBinaries, options.setStep(4)).toString(),
                     "PuppetErrorInterrupter{ Expected to be installed 'codenvy' of the version '" + updateVersion + "' }; looking on errors in file /var/log/puppet/puppet-agent.log locally");

        assertEquals(spyCdecArtifact.getUpdateCommand(versionToUpdate, pathToBinaries, options.setStep(5)).toString(),
                     format("[{'command'='sudo cat %1$s/patches/multi_server/patch_after_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$data_host_name|data.example.com|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_after_update.sh', 'agent'='LocalAgent'}, "
                     + "{'command'='sudo cat %1$s/patches/multi_server/patch_after_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$puppet_master_host_name|master.example.com|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_after_update.sh', 'agent'='LocalAgent'}, "
                     + "{'command'='sudo cat %1$s/patches/multi_server/patch_after_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$host_url|hostname|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_after_update.sh', 'agent'='LocalAgent'}, "
                     + "{'command'='sudo cat %1$s/patches/multi_server/patch_after_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$api_host_name|api.example.com|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_after_update.sh', 'agent'='LocalAgent'}, "
                     + "{'command'='sudo cat %1$s/patches/multi_server/patch_after_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$analytics_host_name|analytics.example.com|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_after_update.sh', 'agent'='LocalAgent'}, "
                     + "{'command'='sudo cat %1$s/patches/multi_server/patch_after_update.sh | sed ':a;N;$!ba;s/\\n/~n/g' | sed 's|$version|3.3.0|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp %1$s/patches/multi_server/patch_after_update.sh', 'agent'='LocalAgent'}, "
                     + "{'command'='bash %1$s/patches/multi_server/patch_after_update.sh', 'agent'='LocalAgent'}]", ETC_PUPPET, TMP_CODENVY));
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Step number .* is out of update range")
    public void testGetUpdateCommandNonexistentStepError() throws Exception {
        prepareSingleNodeEnv(spyConfigManager, mockTransport);

        InstallOptions options = new InstallOptions();
        options.setConfigProperties(Collections.<String, String>emptyMap());
        options.setInstallType(InstallType.SINGLE_SERVER);
        options.setStep(Integer.MAX_VALUE);

        spyCdecArtifact.getUpdateCommand(Version.valueOf("1.0.0"), Paths.get("some path"), options);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Only update to the Codenvy of the same installation type is supported")
    public void testGetUpdateInfoFromSingleToMultiServerError() throws Exception {
        prepareSingleNodeEnv(spyConfigManager, mockTransport);

        InstallOptions options = new InstallOptions();
        options.setConfigProperties(Collections.<String, String>emptyMap());
        options.setInstallType(InstallType.MULTI_SERVER);
        options.setStep(0);

        spyCdecArtifact.getUpdateInfo(InstallType.MULTI_SERVER);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Only update to the Codenvy of the same installation type is supported")
    public void testGetUpdateInfoFromMultiToSingleServerError() throws Exception {
        prepareMultiNodeEnv(spyConfigManager, mockTransport);

        spyCdecArtifact.getUpdateInfo(InstallType.SINGLE_SERVER);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Only update to the Codenvy of the same installation type is supported")
    public void testGetUpdateCommandFromSingleToMultiServerError() throws Exception {
        prepareSingleNodeEnv(spyConfigManager, mockTransport);

        InstallOptions options = new InstallOptions();
        options.setConfigProperties(Collections.<String, String>emptyMap());
        options.setInstallType(InstallType.MULTI_SERVER);
        options.setStep(0);

        spyCdecArtifact.getUpdateCommand(Version.valueOf("1.0.0"), Paths.get("some path"), options);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Only update to the Codenvy of the same installation type is supported")
    public void testGetUpdateCommandFromMultiToSingleServerError() throws Exception {
        prepareMultiNodeEnv(spyConfigManager, mockTransport);

        InstallOptions options = new InstallOptions();
        options.setConfigProperties(Collections.<String, String>emptyMap());
        options.setInstallType(InstallType.SINGLE_SERVER);
        options.setStep(0);

        spyCdecArtifact.getUpdateCommand(Version.valueOf("1.0.0"), Paths.get("some path"), options);
    }

    @Test
    public void testGetBackup3SingleServerCommand() throws Exception {
        prepareSingleNodeEnv(spyConfigManager, mockTransport);

        BackupConfig backupConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                      .setBackupDirectory("some_dir");

        backupConfig.setBackupFile(backupConfig.generateBackupFilePath().toString());
        Command backupCommand = spyCdecArtifact.getBackupCommand(backupConfig);
        assertNotNull(backupCommand);

        List<Command> commands = ((MacroCommand) backupCommand).getCommands();
        assertEquals(commands.size(), 20);

        assertEquals(commands.get(0).toString(), "{'command'='rm -rf /tmp/codenvy/codenvy', 'agent'='LocalAgent'}");
        assertEquals(commands.get(1).toString(), "{'command'='mkdir -p /tmp/codenvy/codenvy', 'agent'='LocalAgent'}");
        assertEquals(commands.get(2).toString(), "{'command'='/bin/systemctl status puppet.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop puppet.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(3).toString(), "{'command'='/bin/systemctl status crond.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop crond.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(4).toString(), "Repeat 2 times: {'command'='/bin/systemctl status codenvy.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop codenvy.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(5).toString(), "{'command'='/bin/systemctl status slapd.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop slapd.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(6).toString(), "{'command'='mkdir -p /tmp/codenvy/codenvy/ldap', 'agent'='LocalAgent'}");
        assertEquals(commands.get(7).toString(), "{'command'='sudo slapcat > /tmp/codenvy/codenvy/ldap/ldap.ldif', 'agent'='LocalAgent'}");
        assertEquals(commands.get(8).toString(), "{'command'='mkdir -p /tmp/codenvy/codenvy/ldap_admin', 'agent'='LocalAgent'}");
        assertEquals(commands.get(9).toString(), "{'command'='sudo slapcat -b 'null' > /tmp/codenvy/codenvy/ldap_admin/ldap.ldif', 'agent'='LocalAgent'}");
        assertEquals(commands.get(10).toString(), "{'command'='mkdir -p /tmp/codenvy/codenvy/mongo', 'agent'='LocalAgent'}");
        assertEquals(commands.get(11).toString(), "{'command'='/usr/bin/mongodump -unull -pnull -o /tmp/codenvy/codenvy/mongo --authenticationDatabase admin --quiet', 'agent'='LocalAgent'}");
        assertEquals(commands.get(12).toString(), "{'command'='rm -rf /tmp/codenvy/codenvy/mongo/admin', 'agent'='LocalAgent'}");
        assertTrue(commands.get(13).toString().matches("\\{'command'='if test -f some_dir/codenvy_backup_.*.tar; then    tar -C /tmp/codenvy/codenvy  -rf some_dir/codenvy_backup_.*.tar .;else    tar -C /tmp/codenvy/codenvy  -cf some_dir/codenvy_backup_.*.tar .;fi;', 'agent'='LocalAgent'\\}"), "Actual command: " + commands.get(13).toString());
        assertTrue(commands.get(14).toString().matches("\\{'command'='if sudo test -f some_dir/codenvy_backup_.*.tar; then    sudo tar -C /home/codenvy/codenvy-data  -rf some_dir/codenvy_backup_.*.tar fs/.;else    sudo tar -C /home/codenvy/codenvy-data  -cf some_dir/codenvy_backup_.*.tar fs/.;fi;', 'agent'='LocalAgent'\\}"), "Actual command: " + commands.get(14).toString());
        assertEquals(commands.get(15).toString(), "{'command'='/bin/systemctl status slapd.service; if [ $? -ne 0 ]; then   sudo /bin/systemctl start slapd.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(16).toString(), "{'command'='/bin/systemctl status codenvy.service; if [ $? -ne 0 ]; then   sudo /bin/systemctl start codenvy.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(17).toString(), "{'command'='/bin/systemctl status puppet.service; if [ $? -ne 0 ]; then   sudo /bin/systemctl start puppet.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(18).toString(), "Wait until artifact 'codenvy' becomes alive");
        assertEquals(commands.get(19).toString(), "{'command'='rm -rf /tmp/codenvy/codenvy', 'agent'='LocalAgent'}");
    }

    @Test
    public void testGetBackupCodenvy4SingleServerCommand() throws Exception {
        prepareSingleNodeEnv(spyConfigManager, mockTransport);
        doReturn(new Config(ImmutableMap.of("host_url", "hostname",
                                            Config.VERSION, CODENVY_4_VERSION)))
            .when(spyConfigManager).loadInstalledCodenvyConfig();

        BackupConfig backupConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                      .setBackupDirectory("some_dir");

        backupConfig.setBackupFile(backupConfig.generateBackupFilePath().toString());
        Command backupCommand = spyCdecArtifact.getBackupCommand(backupConfig);
        assertNotNull(backupCommand);

        List<Command> commands = ((MacroCommand) backupCommand).getCommands();
        assertEquals(commands.size(), 18);
    }

    @Test
    public void testGetRestore3SingleServerCommand() throws Exception {
        prepareSingleNodeEnv(spyConfigManager, mockTransport);

        Path testingBackup = Paths.get(getClass().getClassLoader().getResource("backups/full_backup.tar").getPath());

        BackupConfig backupConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                      .setBackupFile(testingBackup.toString())
                                                      .setBackupDirectory(testingBackup.getParent().toString());

        Command restoreCommand = spyCdecArtifact.getRestoreCommand(backupConfig);
        assertNotNull(restoreCommand);

        List<Command> commands = ((MacroCommand) restoreCommand).getCommands();
        assertEquals(commands.size(), 22);

        assertEquals(commands.get(0).toString(), "{'command'='/bin/systemctl status puppet.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop puppet.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(1).toString(), "{'command'='/bin/systemctl status crond.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop crond.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(2).toString(), "{'command'='/bin/systemctl status codenvy.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop codenvy.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(3).toString(), "{'command'='/bin/systemctl status slapd.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop slapd.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(4).toString(), "{'command'='sudo rm -rf /var/lib/ldap', 'agent'='LocalAgent'}");
        assertEquals(commands.get(5).toString(), "{'command'='sudo mkdir -p /var/lib/ldap', 'agent'='LocalAgent'}");
        assertEquals(commands.get(6).toString(), "{'command'='sudo slapadd -q </tmp/codenvy/codenvy/ldap/ldap.ldif', 'agent'='LocalAgent'}");
        assertEquals(commands.get(7).toString(), "{'command'='sudo chown -R ldap:ldap /var/lib/ldap', 'agent'='LocalAgent'}");
        assertEquals(commands.get(8).toString(), "{'command'='sudo rm -rf /var/lib/ldapcorp', 'agent'='LocalAgent'}");
        assertEquals(commands.get(9).toString(), "{'command'='sudo mkdir -p /var/lib/ldapcorp', 'agent'='LocalAgent'}");
        assertEquals(commands.get(10).toString(), "{'command'='sudo slapadd -q -b 'null' </tmp/codenvy/codenvy/ldap_admin/ldap.ldif', 'agent'='LocalAgent'}");
        assertEquals(commands.get(11).toString(), "{'command'='sudo chown -R ldap:ldap /var/lib/ldapcorp', 'agent'='LocalAgent'}");
        assertEquals(commands.get(12).toString(), "{'command'='/usr/bin/mongo -u null -p null --authenticationDatabase admin --quiet --eval 'db.getMongo().getDBNames().forEach(function(d){if (d!=\"admin\") db.getSiblingDB(d).dropDatabase()})'', 'agent'='LocalAgent'}");
        assertEquals(commands.get(13).toString(), "{'command'='/usr/bin/mongorestore -unull -pnull /tmp/codenvy/codenvy/mongo --authenticationDatabase admin --drop --quiet', 'agent'='LocalAgent'}");
        assertEquals(commands.get(14).toString(), "{'command'='sudo rm -rf /home/codenvy/codenvy-data/fs', 'agent'='LocalAgent'}");
        assertEquals(commands.get(15).toString(), "{'command'='sudo cp -r /tmp/codenvy/codenvy/fs /home/codenvy/codenvy-data', 'agent'='LocalAgent'}");
        assertEquals(commands.get(16).toString(), "{'command'='sudo chown -R codenvy:codenvy /home/codenvy/codenvy-data/fs', 'agent'='LocalAgent'}");
        assertEquals(commands.get(17).toString(), "{'command'='/bin/systemctl status slapd.service; if [ $? -ne 0 ]; then   sudo /bin/systemctl start slapd.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(18).toString(), "{'command'='/bin/systemctl status codenvy.service; if [ $? -ne 0 ]; then   sudo /bin/systemctl start codenvy.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(19).toString(), "{'command'='/bin/systemctl status puppet.service; if [ $? -ne 0 ]; then   sudo /bin/systemctl start puppet.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(20).toString(), "Wait until artifact 'codenvy' becomes alive");
        assertEquals(commands.get(21).toString(), "{'command'='rm -rf /tmp/codenvy/codenvy', 'agent'='LocalAgent'}");
    }

    @Test
    public void testGetRestore4SingleServerCommand() throws Exception {
        prepareSingleNodeEnv(spyConfigManager, mockTransport);
        doReturn(new Config(ImmutableMap.of("host_url", "hostname",
                                            Config.VERSION, CODENVY_4_VERSION)))
            .when(spyConfigManager).loadInstalledCodenvyConfig();


        Path testingBackup = Paths.get(getClass().getClassLoader().getResource("backups/full_backup.tar").getPath());

        BackupConfig backupConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                      .setBackupFile(testingBackup.toString())
                                                      .setBackupDirectory(testingBackup.getParent().toString());

        Command restoreCommand = spyCdecArtifact.getRestoreCommand(backupConfig);
        assertNotNull(restoreCommand);

        List<Command> commands = ((MacroCommand) restoreCommand).getCommands();
        assertEquals(commands.size(), 18);
    }

    @Test
    public void testGetRestore3MultiServerCommandFromFullBackup() throws Exception {
        Path testingBackup = Paths.get(getClass().getClassLoader().getResource("backups/full_backup.tar").getPath());

        prepareMultiNodeEnv(spyConfigManager, mockTransport);

        BackupConfig backupConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                      .setBackupFile(testingBackup.toString())
                                                      .setBackupDirectory(testingBackup.getParent().toString());

        Command result = spyCdecArtifact.getRestoreCommand(backupConfig);
        assertNotNull(result);
        assertTrue(result.toString().contains("sudo rm -rf /home/codenvy/codenvy-data/data/fs"), "Actual result: " + result);  // check presence of restore FS commands
        assertTrue(result.toString().contains("mongorestore"), "Actual result: " + result);  // check presence of restore MONGO and MONGO_ANALYTICS commands
        assertTrue(result.toString().contains("sudo slapadd -q"), "Actual result: " + result);  // check presence of restore LDAP commands

        List<Command> commands = ((MacroCommand) result).getCommands();
        assertEquals(commands.size(), 43);
    }

    @Test
    public void testGetRestore4MultiServerCommandFromFullBackup() throws Exception {
        Path testingBackup = Paths.get(getClass().getClassLoader().getResource("backups/full_backup.tar").getPath());

        prepareMultiNodeEnv(spyConfigManager, mockTransport);

        doReturn(new Config(new HashMap<String, String>() {{
            put("api_host_name", "api.example.com");
            put("data_host_name", "data.example.com");
            put("analytics_host_name", "analytics.example.com");
            put("host_url", "hostname");
            put(Config.PUPPET_MASTER_HOST_NAME, "master.example.com");
            put(Config.VERSION, CODENVY_4_VERSION);
        }})).when(spyConfigManager).loadInstalledCodenvyConfig();


        BackupConfig backupConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                      .setBackupFile(testingBackup.toString())
                                                      .setBackupDirectory(testingBackup.getParent().toString());

        Command result = spyCdecArtifact.getRestoreCommand(backupConfig);
        assertNotNull(result);
        assertTrue(result.toString().contains("sudo rm -rf /home/codenvy/codenvy-data/data/fs"), "Actual result: " + result);  // check presence of restore FS commands
        assertTrue(result.toString().contains("mongorestore"), "Actual result: " + result);  // check presence of restore MONGO and MONGO_ANALYTICS commands
        assertTrue(result.toString().contains("sudo slapadd -q"), "Actual result: " + result);  // check presence of restore LDAP commands


        List<Command> commands = ((MacroCommand) result).getCommands();
        assertEquals(commands.size(), 37);
    }

    @Test
    public void testGetRestoreMultiServerCommandFromEmptyBackup() throws Exception {
        Path testingBackup = Paths.get(getClass().getClassLoader().getResource("backups/empty_backup.tar").getPath());

        prepareMultiNodeEnv(spyConfigManager, mockTransport);

        BackupConfig backupConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                      .setBackupFile(testingBackup.toString())
                                                      .setBackupDirectory(testingBackup.getParent().toString());

        Command result = spyCdecArtifact.getRestoreCommand(backupConfig);
        assertNotNull(result);
        assertFalse(result.toString().contains("sudo rm -rf /home/codenvy/codenvy-data/data/fs"), "Actual result: " + result);  // check absence of restore FS commands
        assertFalse(result.toString().contains("mongorestore"), "Actual result: " + result);  // check absence of restore MONGO and MONGO_ANALYTICS commands
        assertFalse(result.toString().contains("sudo slapadd -q"), "Actual result: " + result);  // check absence of restore LDAP commands
    }

    @Test
    public void testUpdateCodenvyConfig() throws IOException {
        String newHostName = "c";
        Map<String, String> properties = ImmutableMap.of(Config.HOST_URL, newHostName);

        String oldHostName = "a";
        Config testConfig = new Config(ImmutableMap.of(Config.HOST_URL, oldHostName, "property2", "b"));
        doReturn(testConfig).when(spyConfigManager).loadInstalledCodenvyConfig();

        doReturn(InstallType.MULTI_SERVER).when(spyConfigManager).detectInstallationType();

        doReturn(mockCommand).when(spyCDECMultiServerHelper).getUpdateConfigCommand(testConfig, properties);

        spyCdecArtifact.updateConfig(properties);
        verify(spyCDECMultiServerHelper).getUpdateConfigCommand(testConfig, properties);
        verify(mockNodeManager).updatePuppetConfig(oldHostName, newHostName);
    }

    @Test
    public void testChangeNonexistentCodenvyConfigProperty() throws IOException {
        Map<String, String> properties = ImmutableMap.of("property2", "a", "property3", "b", "property4", "c");

        Config testConfig = new Config(ImmutableMap.of("property1", "a"));
        doReturn(testConfig).when(spyConfigManager).loadInstalledCodenvyConfig();

        doReturn(InstallType.MULTI_SERVER).when(spyConfigManager).detectInstallationType();

        doReturn(mockCommand).when(spyCDECMultiServerHelper).getUpdateConfigCommand(testConfig, properties);

        try {
            spyCdecArtifact.updateConfig(properties);
        } catch (PropertiesNotFoundException e) {
            assertEquals(e.getMessage(), "Properties not found");
            assertEquals(e.getProperties(), new ArrayList<>(properties.keySet()));
            return;
        }

        fail("There should be PropertiesNotFoundExceptions thrown");
    }


    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "error")
    public void testChangeCodenvyConfigWhenCommandException() throws IOException {
        Map<String, String> properties = ImmutableMap.of("a", "c");

        Config testConfig = new Config(ImmutableMap.of("a", "c"));
        doReturn(testConfig).when(spyConfigManager).loadInstalledCodenvyConfig();
        doReturn(InstallType.MULTI_SERVER).when(spyConfigManager).detectInstallationType();

        doThrow(new CommandException("error", new AgentException("error"))).when(mockCommand).execute();
        doReturn(mockCommand).when(spyCDECMultiServerHelper).getUpdateConfigCommand(testConfig, properties);

        spyCdecArtifact.updateConfig(properties);
    }

    @Test
    public void testGetChangeSingleServerCodenvy3HostnameCommand() throws IOException {
        String oldHostName = "old";
        String newHostName = "new";
        ImmutableMap<String, String> currentCodenvyProperties = ImmutableMap.of(Config.HOST_URL, oldHostName,
                                                                                Config.VERSION, CODENVY_3_VERSION);
        Config testConfig = new Config(currentCodenvyProperties);
        Map<String, String> properties = ImmutableMap.of(Config.HOST_URL, newHostName);

        doReturn(Paths.get(ETC_PUPPET, Config.SINGLE_SERVER_PP)).when(spyConfigManager).getPuppetConfigFile(Config.SINGLE_SERVER_PP);
        doReturn(Paths.get(ETC_PUPPET, Config.SINGLE_SERVER_BASE_CONFIG_PP)).when(spyConfigManager).getPuppetConfigFile(
                Config.SINGLE_SERVER_BASE_CONFIG_PP);
        doReturn(Optional.of(Version.valueOf("1.0.0"))).when(spyCdecArtifact).getInstalledVersion();

        CDECSingleServerHelper testHelper = new CDECSingleServerHelper(spyCdecArtifact, spyConfigManager);

        MacroCommand updatePuppetConfigCommand = (MacroCommand) testHelper.getUpdatePuppetConfigCommand(testConfig, oldHostName, newHostName);
        List<Command> commands = updatePuppetConfigCommand.getCommands();
        k = 0;
        assertEquals(commands.size(), 8);
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo sed -i 's/node \"old\"/node \"new\"/g' %1$s/manifests/nodes/single_server/base_config.pp', 'agent'='LocalAgent'}", ETC_PUPPET));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo sed -i 's/node \"old\"/node \"new\"/g' %1$s/manifests/nodes/single_server/single_server.pp', 'agent'='LocalAgent'}", ETC_PUPPET));

        assertTrue(commands.get(k).toString().matches("\\{'command'='sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back ; sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}"),
                   "Actual command: " + commands.get(k++).toString());
        assertEquals(commands.get(k++).toString(), "{'command'='sudo sed -i 's/certname = old/certname = new/g' /etc/puppet/puppet.conf', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo sed -i 's/server = old/server = new/g' /etc/puppet/puppet.conf', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo grep \"dns_alt_names = .*,new.*\" /etc/puppet/puppet.conf; if [ $? -ne 0 ]; then sudo sed -i 's/dns_alt_names = .*/&,new/' /etc/puppet/puppet.conf; fi', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo systemctl restart puppet', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='while true; do\n"
                                                   + "  COUNTER=0\n"
                                                   + "  sudo puppet cert list --all | grep '\"new\"' &>/dev/null\n"
                                                   + "  hasCertificate=$?\n"
                                                   + "  if [[ $hasCertificate == 0 ]]; then\n"
                                                   + "     sudo systemctl restart puppetmaster\n"
                                                   + "     break\n"
                                                   + "  fi\n"
                                                   + "  sleep 10\n"
                                                   + "  let COUNTER=COUNTER+1\n"
                                                   + "  if [[ $COUNTER -eq 30 ]]; then\n"
                                                   + "     echo 'Puppet agent error' >&2\n"
                                                   + "     exit 1\n"
                                                   + "  fi\n"
                                                   + "done\n"
                                                   + "', 'agent'='LocalAgent'}");

        MacroCommand updateCodenvyConfigCommand = (MacroCommand) testHelper.getUpdateConfigCommand(testConfig, properties);
        commands = updateCodenvyConfigCommand.getCommands();
        assertEquals(commands.size(), 6);
        k = 0;
        assertTrue(commands.get(k).toString().matches(
                format("\\{'command'='sudo cp %1$s/manifests/nodes/single_server/base_config.pp %1$s/manifests/nodes/single_server/base_config.pp.back ; " +
                "sudo cp %1$s/manifests/nodes/single_server/base_config.pp %1$s/manifests/nodes/single_server/base_config.pp.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}", ETC_PUPPET)),
                   commands.get(k++).toString());

        assertEquals(commands.get(k++).toString(),
                     format("{'command'='sudo cat %1$s/manifests/nodes/single_server/base_config.pp " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed 's|$host_url *= *\"[^\"]*\"|$host_url = \"new\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp %1$s/manifests/nodes/single_server/base_config.pp', 'agent'='LocalAgent'}", ETC_PUPPET));

        assertTrue(commands.get(k).toString().matches(
                format("\\{'command'='sudo cp %1$s/manifests/nodes/single_server/single_server.pp %1$s/manifests/nodes/single_server/single_server.pp.back ; " +
                "sudo cp %1$s/manifests/nodes/single_server/single_server.pp %1$s/manifests/nodes/single_server/single_server.pp.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}", ETC_PUPPET)),
                   commands.get(k++).toString());

        assertEquals(commands.get(k++).toString(),
                     format("{'command'='sudo cat %1$s/manifests/nodes/single_server/single_server.pp " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed 's|$host_url *= *\"[^\"]*\"|$host_url = \"new\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp %1$s/manifests/nodes/single_server/single_server.pp', 'agent'='LocalAgent'}", ETC_PUPPET));

        assertEquals(commands.get(k++).toString(), "{'command'='testFile=\"/home/codenvy/codenvy-data/cloud-ide-local-configuration/general.properties\"; while true; do     if sudo grep \"api.endpoint=http://new/api\" ${testFile}; then break; fi;     sleep 5; done; "
                                                   + "sleep 15; # delay to involve into start of rebooting api server', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "Wait until artifact 'codenvy' becomes alive");
    }

    @Test
    public void testGetChangeSingleServerCodenvy4HostnameCommand() throws IOException {
        String oldHostName = "old";
        String newHostName = "new";
        ImmutableMap<String, String> currentCodenvyProperties = ImmutableMap.of(Config.HOST_URL, oldHostName,
                                                                                Config.VERSION, CODENVY_4_VERSION);
        Config testConfig = new Config(currentCodenvyProperties);
        Map<String, String> properties = ImmutableMap.of(Config.HOST_URL, newHostName);

        doReturn(Paths.get(ETC_PUPPET, Config.SINGLE_SERVER_PP)).when(spyConfigManager).getPuppetConfigFile(Config.SINGLE_SERVER_PP);
        doReturn(Paths.get(ETC_PUPPET, Config.SINGLE_SERVER_BASE_CONFIG_PP)).when(spyConfigManager).getPuppetConfigFile(
            Config.SINGLE_SERVER_BASE_CONFIG_PP);
        doReturn(Optional.of(Version.valueOf("1.0.0"))).when(spyCdecArtifact).getInstalledVersion();

        CDECSingleServerHelper testHelper = new CDECSingleServerHelper(spyCdecArtifact, spyConfigManager);

        MacroCommand updatePuppetConfigCommand = (MacroCommand) testHelper.getUpdatePuppetConfigCommand(testConfig, oldHostName, newHostName);
        List<Command> commands = updatePuppetConfigCommand.getCommands();
        assertEquals(commands.size(), 10);
        k = 0;
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo sed -i 's/node \"old\"/node \"new\"/g' %1$s/manifests/nodes/single_server/base_config.pp', 'agent'='LocalAgent'}", ETC_PUPPET));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo sed -i 's/\\^node\\\\d+\\\\.old\\$/\\^node\\\\d+\\\\.new\\$/g' %1$s/manifests/nodes/single_server/base_config.pp', 'agent'='LocalAgent'}", ETC_PUPPET));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo sed -i 's/node \"old\"/node \"new\"/g' %1$s/manifests/nodes/single_server/single_server.pp', 'agent'='LocalAgent'}", ETC_PUPPET));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo sed -i 's/\\^node\\\\d+\\\\.old\\$/\\^node\\\\d+\\\\.new\\$/g' %1$s/manifests/nodes/single_server/single_server.pp', 'agent'='LocalAgent'}", ETC_PUPPET));

        assertTrue(commands.get(k).toString().matches("\\{'command'='sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back ; sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back.[0-9]* ; ', 'agent'='LocalAgent'\\}"),
                   "Actual command: " + commands.get(k++).toString());
        assertEquals(commands.get(k++).toString(), "{'command'='sudo sed -i 's/certname = old/certname = new/g' /etc/puppet/puppet.conf', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo sed -i 's/server = old/server = new/g' /etc/puppet/puppet.conf', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo grep \"dns_alt_names = .*,new.*\" /etc/puppet/puppet.conf; if [ $? -ne 0 ]; then sudo sed -i 's/dns_alt_names = .*/&,new/' /etc/puppet/puppet.conf; fi', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo systemctl restart puppet', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='while true; do\n"
                                                   + "  COUNTER=0\n"
                                                   + "  sudo puppet cert list --all | grep '\"new\"' &>/dev/null\n"
                                                   + "  hasCertificate=$?\n"
                                                   + "  if [[ $hasCertificate == 0 ]]; then\n"
                                                   + "     sudo systemctl restart puppetmaster\n"
                                                   + "     break\n"
                                                   + "  fi\n"
                                                   + "  sleep 10\n"
                                                   + "  let COUNTER=COUNTER+1\n"
                                                   + "  if [[ $COUNTER -eq 30 ]]; then\n"
                                                   + "     echo 'Puppet agent error' >&2\n"
                                                   + "     exit 1\n"
                                                   + "  fi\n"
                                                   + "done\n"
                                                   + "', 'agent'='LocalAgent'}");

        MacroCommand updateCodenvyConfigCommand = (MacroCommand) testHelper.getUpdateConfigCommand(testConfig, properties);
        commands = updateCodenvyConfigCommand.getCommands();
        assertEquals(commands.size(), 6);
        k = 0;
        assertTrue(commands.get(k).toString().matches(
            format("\\{'command'='sudo cp %1$s/manifests/nodes/single_server/base_config.pp %1$s/manifests/nodes/single_server/base_config.pp.back ; " +
            "sudo cp %1$s/manifests/nodes/single_server/base_config.pp %1$s/manifests/nodes/single_server/base_config.pp.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}", ETC_PUPPET)),
                   commands.get(k++).toString());

        assertEquals(commands.get(k++).toString(),
                     format("{'command'='sudo cat %1$s/manifests/nodes/single_server/base_config.pp " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed 's|$host_url *= *\"[^\"]*\"|$host_url = \"new\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp %1$s/manifests/nodes/single_server/base_config.pp', 'agent'='LocalAgent'}", ETC_PUPPET));

        assertTrue(commands.get(k).toString().matches(
            format("\\{'command'='sudo cp %1$s/manifests/nodes/single_server/single_server.pp %1$s/manifests/nodes/single_server/single_server.pp.back ; " +
            "sudo cp %1$s/manifests/nodes/single_server/single_server.pp %1$s/manifests/nodes/single_server/single_server.pp.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}", ETC_PUPPET)),
                   commands.get(k++).toString());

        assertEquals(commands.get(k++).toString(),
                     format("{'command'='sudo cat %1$s/manifests/nodes/single_server/single_server.pp " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed 's|$host_url *= *\"[^\"]*\"|$host_url = \"new\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp %1$s/manifests/nodes/single_server/single_server.pp', 'agent'='LocalAgent'}", ETC_PUPPET));

        assertEquals(commands.get(k++).toString(), "{'command'='testFile=\"/home/codenvy/codenvy-data/cloud-ide-local-configuration/general.properties\"; while true; do     if sudo grep \"api.endpoint=http://new/api\" ${testFile}; then break; fi;     sleep 5; done; "
                                                   + "sleep 15; # delay to involve into start of rebooting api server', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "Wait until artifact 'codenvy' becomes alive");
    }


    @Test
    public void testGetChangeSingleServerConfigCommandWhenHostUrlUnchanged() throws IOException {
        Config testConfig = new Config(ImmutableMap.of(Config.LDAP_HOST, "old", Config.HOST_URL, "old"));
        Map<String, String> properties = ImmutableMap.of(Config.LDAP_HOST, "new", Config.HOST_URL, "old");

        doReturn(Paths.get(ETC_PUPPET, Config.SINGLE_SERVER_PP)).when(spyConfigManager).getPuppetConfigFile(Config.SINGLE_SERVER_PP);
        doReturn(Paths.get(ETC_PUPPET, Config.SINGLE_SERVER_BASE_CONFIG_PP)).when(spyConfigManager).getPuppetConfigFile(
                Config.SINGLE_SERVER_BASE_CONFIG_PP);
        doReturn(Optional.of(Version.valueOf("1.0.0"))).when(spyCdecArtifact).getInstalledVersion();

        CDECSingleServerHelper testHelper = new CDECSingleServerHelper(spyCdecArtifact, spyConfigManager);

        Command command = testHelper.getUpdateConfigCommand(testConfig, properties);

        List<Command> commands = ((MacroCommand) command).getCommands();
        assertEquals(commands.size(), 8);
        k = 0;
        assertTrue(commands.get(k).toString().matches(
                format("\\{'command'='sudo cp %1$s/manifests/nodes/single_server/base_config.pp %1$s/manifests/nodes/single_server/base_config.pp.back ; " +
                "sudo cp %1$s/manifests/nodes/single_server/base_config.pp %1$s/manifests/nodes/single_server/base_config.pp.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}", ETC_PUPPET)),
                   commands.get(k++).toString());

        assertEquals(commands.get(k++).toString(),
                     format("{'command'='sudo cat %1$s/manifests/nodes/single_server/base_config.pp " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed 's|$ldap_host *= *\"[^\"]*\"|$ldap_host = \"new\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp %1$s/manifests/nodes/single_server/base_config.pp', 'agent'='LocalAgent'}", ETC_PUPPET));

        assertEquals(commands.get(k++).toString(),
                     format("{'command'='sudo cat %1$s/manifests/nodes/single_server/base_config.pp " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed 's|$host_url *= *\"[^\"]*\"|$host_url = \"old\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp %1$s/manifests/nodes/single_server/base_config.pp', 'agent'='LocalAgent'}", ETC_PUPPET));

        assertTrue(commands.get(k).toString().matches(
                format("\\{'command'='sudo cp %1$s/manifests/nodes/single_server/single_server.pp %1$s/manifests/nodes/single_server/single_server.pp.back ; " +
                "sudo cp %1$s/manifests/nodes/single_server/single_server.pp %1$s/manifests/nodes/single_server/single_server.pp.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}", ETC_PUPPET)),
                   commands.get(k++).toString());

        assertEquals(commands.get(k++).toString(),
                     format("{'command'='sudo cat %1$s/manifests/nodes/single_server/single_server.pp " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed 's|$ldap_host *= *\"[^\"]*\"|$ldap_host = \"new\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp %1$s/manifests/nodes/single_server/single_server.pp', 'agent'='LocalAgent'}", ETC_PUPPET));

        assertEquals(commands.get(k++).toString(),
                     format("{'command'='sudo cat %1$s/manifests/nodes/single_server/single_server.pp " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed 's|$host_url *= *\"[^\"]*\"|$host_url = \"old\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp %1$s/manifests/nodes/single_server/single_server.pp', 'agent'='LocalAgent'}", ETC_PUPPET));

        assertEquals(commands.get(k++).toString(), "{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "Wait until artifact 'codenvy' becomes alive");
    }


    @Test
    public void testGetChangeMultiServerConfigCommand() throws IOException {
        Map<String, String> properties = ImmutableMap.of(Config.HOST_URL, "a");
        Config testConfig = new Config(ImmutableMap.of(Config.HOST_URL, "c",
                                                       "api_host_name", "api.dev.com",
                                                       "data_host_name", "data.dev.com"));

        doReturn(Paths.get(ETC_PUPPET, Config.MULTI_SERVER_BASE_CONFIG_PP)).when(spyConfigManager).getPuppetConfigFile(
                Config.MULTI_SERVER_BASE_CONFIG_PP);
        doReturn(Paths.get(ETC_PUPPET, Config.MULTI_SERVER_CUSTOM_CONFIG_PP)).when(spyConfigManager).getPuppetConfigFile(
                Config.MULTI_SERVER_CUSTOM_CONFIG_PP);
        doReturn(Optional.of(Version.valueOf("1.0.0"))).when(spyCdecArtifact).getInstalledVersion();

        Command command = spyCDECMultiServerHelper.getUpdateConfigCommand(testConfig, properties);
        List<Command> commands = ((MacroCommand) command).getCommands();
        assertEquals(commands.size(), 8);
        k = 0;
        assertTrue(commands.get(k).toString().matches(format("\\{'command'='sudo cp %1$s/manifests/nodes/multi_server/custom_configurations.pp %1$s/manifests/nodes/multi_server/custom_configurations.pp.back ; sudo cp %1$s/manifests/nodes/multi_server/custom_configurations.pp %1$s/manifests/nodes/multi_server/custom_configurations.pp.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}", ETC_PUPPET)),
                   commands.get(k++).toString());

        assertEquals(commands.get(k++).toString(),
                     format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/custom_configurations.pp " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed 's|$host_url *= *\"[^\"]*\"|$host_url = \"a\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/custom_configurations.pp', 'agent'='LocalAgent'}", ETC_PUPPET));

        assertTrue(commands.get(k).toString().matches(format("\\{'command'='sudo cp %1$s/manifests/nodes/multi_server/base_configurations.pp %1$s/manifests/nodes/multi_server/base_configurations.pp.back ; sudo cp %1$s/manifests/nodes/multi_server/base_configurations.pp %1$s/manifests/nodes/multi_server/base_configurations.pp.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}", ETC_PUPPET)),
                   "Actual result: " + commands.get(k++).toString());

        assertEquals(commands.get(k++).toString(),
                     format("{'command'='sudo cat %1$s/manifests/nodes/multi_server/base_configurations.pp " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed 's|$host_url *= *\"[^\"]*\"|$host_url = \"a\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp %1$s/manifests/nodes/multi_server/base_configurations.pp', 'agent'='LocalAgent'}", ETC_PUPPET));
        assertEquals(commands.get(k++).toString(), "{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='{'host'='data.dev.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='{'host'='api.dev.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(commands.get(k++).toString(), "Wait until artifact 'codenvy' becomes alive");
    }

    @Test
    public void testGetReinstallCodenvyCommandSingleServer() throws IOException {
        Map<String, String> properties = ImmutableMap.of(Config.VERSION, "1.0.0");

        Config config = new Config(properties);
        doReturn(config).when(spyConfigManager).loadInstalledCodenvyConfig(InstallType.SINGLE_SERVER);
        doReturn(config).when(spyConfigManager).loadInstalledCodenvyConfig();
        doReturn(InstallType.SINGLE_SERVER).when(spyConfigManager).detectInstallationType();

        Command command = spyCdecArtifact.getReinstallCommand();
        List<Command> commands = ((MacroCommand) command).getCommands();
        assertEquals(commands.size(), 5);
        k = 0;
        assertEquals(commands.get(k++).toString(), "{'command'='sudo rm -rf /home/codenvy/archives', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo rm -rf /home/codenvy-im/archives', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='/bin/systemctl status codenvy.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop codenvy.service; fi; ', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "PuppetErrorInterrupter{ Wait until artifact 'codenvy' becomes alive }; looking on errors in file /var/log/puppet/puppet-agent.log locally");
    }

    @Test
    public void testGetReinstallCodenvyCommandMultiServer() throws IOException {
        Map<String, String> properties = ImmutableMap.of(Config.VERSION, "1.0.0",
                                                         Config.HOST_URL, "host",
                                                         "api_host_name", "api.dev.com",
                                                         "puppet_master_host_name", "master.dev.com",
                                                         "node_ssh_user_name", SYSTEM_USER_NAME
                                                         );

        Config config = new Config(properties);
        doReturn(config).when(spyConfigManager).loadInstalledCodenvyConfig(InstallType.MULTI_SERVER);
        doReturn(config).when(spyConfigManager).loadInstalledCodenvyConfig();
        doReturn(InstallType.MULTI_SERVER).when(spyConfigManager).detectInstallationType();
        doReturn(Optional.empty()).when(spyCdecArtifact).fetchAssemblyVersion();

        Command command = spyCdecArtifact.getReinstallCommand();
        List<Command> commands = ((MacroCommand) command).getCommands();
        assertEquals(commands.size(), 6);
        assertEquals(commands.get(0).toString(), format("{'command'='sudo rm -rf /home/codenvy/archives', 'agent'='{'host'='api.dev.com', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(commands.get(1).toString(), "{'command'='sudo rm -rf /home/codenvy-im/archives', 'agent'='LocalAgent'}");
        assertEquals(commands.get(2).toString(), format("{'command'='/bin/systemctl status codenvy.service; if [ $? -eq 0 ]; then   sudo /bin/systemctl stop codenvy.service; fi; ', 'agent'='{'host'='api.dev.com', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(commands.get(3).toString(), format("{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='{'host'='api.dev.com', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(commands.get(4).toString(), "{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='LocalAgent'}");

        assertEquals(commands.get(5).toString(), format("PuppetErrorInterrupter{ Wait until artifact 'codenvy' becomes alive }; looking on errors in file /var/log/puppet/puppet-agent.log locally and at the nodes: [" +
                                                        "{'host':'api.dev.com', 'port':'22', 'user':'%1$s', 'privateKeyFile':'~/.ssh/id_rsa', 'type':'API'}]", SYSTEM_USER_NAME));

    }

    @Test
    public void shouldBeAlive() {
        doReturn(true).when(spyCdecArtifact).isApiServiceAlive();
        assertTrue(spyCdecArtifact.isAlive());
    }

    @Test
    public void shouldNotBeAlive() {
        doReturn(false).when(spyCdecArtifact).isApiServiceAlive();
        assertFalse(spyCdecArtifact.isAlive());
    }

    @AfterMethod
    public void tearDown() throws IOException {
        OSUtils.VERSION = INITIAL_OS_VERSION;

        FileUtils.deleteDirectory(new File(BASE_TMP_DIR));
        FileUtils.deleteDirectory(BackupConfig.BASE_TMP_DIRECTORY.toFile());  // to clean up directories creating in time of executing backup/restore tests

        System.clearProperty("http.proxyUser");
        System.clearProperty("http.proxyPassword");
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");

        System.clearProperty("https.proxyUser");
        System.clearProperty("https.proxyPassword");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
    }
}
