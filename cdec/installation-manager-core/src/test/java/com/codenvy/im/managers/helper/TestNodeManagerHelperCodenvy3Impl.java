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
package com.codenvy.im.managers.helper;

import com.codenvy.im.BaseTest;
import com.codenvy.im.commands.Command;
import com.codenvy.im.commands.CommandLibrary;
import com.codenvy.im.commands.MacroCommand;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.managers.NodeConfig;
import com.codenvy.im.managers.NodeException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/** @author Dmytro Nochevnov */
public class TestNodeManagerHelperCodenvy3Impl extends BaseTest {

    @Mock
    private ConfigManager    mockConfigManager;
    @Mock
    private NodeConfigHelper mockNodeConfigHelper;
    @Mock
    private Command          mockCommand;

    private static final String              TEST_NODE_DNS  = "runner1.hostname";
    private static final NodeConfig.NodeType TEST_NODE_TYPE = NodeConfig.NodeType.RUNNER;
    private static final NodeConfig          TEST_NODE      = new NodeConfig(TEST_NODE_TYPE, TEST_NODE_DNS);

    private static final String TEST_VALUE_WITH_NODE             = "test_runner_node_url";
    private static final String ADDITIONAL_RUNNERS_PROPERTY_NAME = Config.ADDITIONAL_RUNNERS;

    private NodeManagerHelperCodenvy3Impl spyHelperCodenvy3;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        spyHelperCodenvy3 = spy(new NodeManagerHelperCodenvy3Impl(mockConfigManager));

        doReturn(ImmutableList.of(Paths.get("/etc/puppet/" + Config.MULTI_SERVER_CUSTOM_CONFIG_PP),
                                  Paths.get("/etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP)).iterator())
            .when(mockConfigManager).getCodenvyPropertiesFiles(InstallType.MULTI_SERVER);

        doReturn(mockNodeConfigHelper).when(spyHelperCodenvy3).getNodeConfigHelper(any(Config.class));

        initConfigs();
    }

    private void initConfigs() throws IOException {
        doReturn(TEST_VALUE_WITH_NODE).when(mockNodeConfigHelper).getValueWithNode(TEST_NODE);
    }

    @Test
    public void testGetAddNodeCommand() throws Exception {
        prepareMultiNodeEnv(mockConfigManager);
        Command result = spyHelperCodenvy3.getAddNodeCommand(TEST_NODE, ADDITIONAL_RUNNERS_PROPERTY_NAME);
        assertNotNull(result);
        assertTrue(result instanceof MacroCommand);

        List<Command> commands = ((MacroCommand) result).getCommands();
        assertEquals(commands.size(), 18);

        assertTrue(commands.get(0).toString().matches("\\{'command'='sudo cp /etc/puppet/" + Config.MULTI_SERVER_CUSTOM_CONFIG_PP
                                                      + " /etc/puppet/" + Config.MULTI_SERVER_CUSTOM_CONFIG_PP + ".back ; "
                                                      + "sudo cp /etc/puppet/" + Config.MULTI_SERVER_CUSTOM_CONFIG_PP
                                                      + " /etc/puppet/" + Config.MULTI_SERVER_CUSTOM_CONFIG_PP + ".back.[0-9]+ ; "
                                                      + "', 'agent'='LocalAgent'\\}"));
        assertEquals(commands.get(1).toString(),
                     "{'command'='sudo cat /etc/puppet/" + Config.MULTI_SERVER_CUSTOM_CONFIG_PP + " " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed -E 's|(~n[^#]*\\$)additional_runners *= *\"[^\"]*\"|\\1additional_runners = \"test_runner_node_url\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp /etc/puppet/manifests/nodes/multi_server/custom_configurations.pp', 'agent'='LocalAgent'}");
        assertTrue(commands.get(2).toString().matches("\\{'command'='sudo cp /etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP
                                                      + " /etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP + ".back ; "
                                                      + "sudo cp /etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP
                                                      + " /etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP + ".back.[0-9]+ ; "
                                                      + "', 'agent'='LocalAgent'\\}"));

        assertEquals(commands.get(3).toString(),
                     "{'command'='sudo cat /etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP + " " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed -E 's|(~n[^#]*\\$)additional_runners *= *\"[^\"]*\"|\\1additional_runners = \"test_runner_node_url\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp /etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP + "', 'agent'='LocalAgent'}");

        assertEquals(commands.get(4).toString(),
                     "{'command'='sudo sh -c \"echo -e 'runner1.hostname' >> /etc/puppet/autosign.conf\"', 'agent'='LocalAgent'}");
        assertEquals(commands.get(5).toString(), format("{'command'='yum clean all', 'agent'='LocalAgent'}"));
        assertEquals(commands.get(6).toString(),
                     format("{'command'='if [ \"`yum list installed | grep puppetlabs-release`\" == \"\" ]; then sudo yum -y -q install https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm; fi', 'agent'='{'host'='runner1.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
        assertEquals(commands.get(7).toString(),
                     format("{'command'='sudo yum -y -q install puppet-3.8.6-1.el7.noarch', 'agent'='{'host'='runner1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
        assertEquals(commands.get(8).toString(),
                     format("{'command'='sudo systemctl enable puppet', 'agent'='{'host'='runner1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
        assertTrue(commands.get(9).toString().matches(
                format("\\{'command'='sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back ; sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back.[0-9]+ ; ', 'agent'='\\{'host'='runner1.hostname', 'port'='22', 'user'='%1$s', 'identity'='\\[~/.ssh/id_rsa\\]'\\}'\\}",
                       SYSTEM_USER_NAME)),
                   commands.get(9).toString());

        assertEquals(commands.get(10).toString(),
                     format("{'command'='sudo sed -i 's/\\[main\\]/\\[main\\]\\n  server = null\\n  runinterval = 420\\n  configtimeout = 600\\n/g' /etc/puppet/puppet.conf', 'agent'='{'host'='runner1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
        assertEquals(commands.get(11).toString(),
                     format("{'command'='sudo sed -i 's/\\[agent\\]/\\[agent\\]\\n  show_diff = true\\n  pluginsync = true\\n  report = false\\n  default_schedules = false\\n  certname = runner1.hostname\\n/g' /etc/puppet/puppet.conf', 'agent'='{'host'='runner1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
        assertEquals(commands.get(12).toString(), format("{'command'='sudo sh -c 'echo -e \"\\nPUPPET_EXTRA_OPTS=--logdest /var/log/puppet/puppet-agent.log\\n\" >> /etc/sysconfig/puppetagent'', " +
                                                        "'agent'='{'host'='runner1.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));                            
        assertEquals(commands.get(13).toString(),
                     format("{'command'='sudo systemctl start puppet', 'agent'='{'host'='runner1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
        assertEquals(commands.get(14).toString(),
                     format("PuppetErrorInterrupter{ {'command'='doneState=\"Installing\"; testFile=\"/home/codenvy/codenvy-tomcat/logs/catalina.out\"; while [ \"${doneState}\" != \"Installed\" ]; do     if sudo test -f ${testFile}; then doneState=\"Installed\"; fi;     sleep 30; done', 'agent'='{'host'='runner1.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'} }; " + 
                            "looking on errors in file /var/log/puppet/puppet-agent.log locally and at the nodes: [{'host':'runner1.hostname', 'port':'22', 'privateKeyFile':'~/.ssh/id_rsa', 'type':'RUNNER'}]",
                            SYSTEM_USER_NAME));
        assertEquals(commands.get(15).toString(),
                     format("{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='{'host'='api.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
        assertEquals(commands.get(16).toString(),
                     format("{'command'='testFile=\"/home/codenvy/codenvy-data/conf/general.properties\"; while true; do     if sudo grep \"test_runner_node_url$\" ${testFile}; then break; fi;     sleep 5; done; sleep 15; # delay to involve into start of rebooting api server', 'agent'='{'host'='api.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
        assertEquals(commands.get(17).toString(), "Wait until artifact 'codenvy' becomes alive");
    }

    @Test
    public void testGetUpdatePuppetConfigCommand() throws Exception {
        Command result = spyHelperCodenvy3.getUpdatePuppetConfigCommand("hostname", "new.hostname");
        assertNotNull(result);
        assertEquals(result.toString(), CommandLibrary.EMPTY_COMMAND.toString());
    }

    @Test(expectedExceptions = NodeException.class, expectedExceptionsMessageRegExp = "error")
    public void testGetAddNodeCommandWithNodeException() throws Exception {
        prepareMultiNodeEnv(mockConfigManager);
        doThrow(new IOException("error")).when(mockConfigManager).fetchMasterHostName();

        spyHelperCodenvy3.getAddNodeCommand(TEST_NODE, ADDITIONAL_RUNNERS_PROPERTY_NAME);
    }

    @Test
    public void testGetRemoveNodeCommand() throws Exception {
        prepareMultiNodeEnv(mockConfigManager);
        Command removeNodeCommand = spyHelperCodenvy3.getRemoveNodeCommand(TEST_NODE, ADDITIONAL_RUNNERS_PROPERTY_NAME);

        List<Command> commands = ((MacroCommand) removeNodeCommand).getCommands();
        assertEquals(commands.size(), 11);

        assertTrue(commands.get(0).toString().matches("\\{'command'='sudo cp /etc/puppet/" + Config.MULTI_SERVER_CUSTOM_CONFIG_PP
                                                      + " /etc/puppet/" + Config.MULTI_SERVER_CUSTOM_CONFIG_PP + ".back ; "
                                                      + "sudo cp /etc/puppet/" + Config.MULTI_SERVER_CUSTOM_CONFIG_PP
                                                      + " /etc/puppet/" + Config.MULTI_SERVER_CUSTOM_CONFIG_PP + ".back.[0-9]+ ; "
                                                      + "', 'agent'='LocalAgent'\\}"));
        assertEquals(commands.get(1).toString(),
                     "{'command'='sudo cat /etc/puppet/" + Config.MULTI_SERVER_CUSTOM_CONFIG_PP + " " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed -E 's|(~n[^#]*\\$)additional_runners *= *\"[^\"]*\"|\\1additional_runners = \"null\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp /etc/puppet/manifests/nodes/multi_server/custom_configurations.pp', 'agent'='LocalAgent'}");

        assertTrue(commands.get(2).toString().matches("\\{'command'='sudo cp /etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP
                                                      + " /etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP + ".back ; "
                                                      + "sudo cp /etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP
                                                      + " /etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP + ".back.[0-9]+ ; "
                                                      + "', 'agent'='LocalAgent'\\}"));

        assertEquals(commands.get(3).toString(),
                     "{'command'='sudo cat /etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP + " " +
                     "| sed ':a;N;$!ba;s/\\n/~n/g' " +
                     "| sed -E 's|(~n[^#]*\\$)additional_runners *= *\"[^\"]*\"|\\1additional_runners = \"null\"|g' " +
                     "| sed 's|~n|\\n|g' > tmp.tmp " +
                     "&& sudo mv tmp.tmp /etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP + "', 'agent'='LocalAgent'}");

        assertEquals(commands.get(4).toString(),
                     format("{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='{'host'='api.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
        assertEquals(commands.get(5).toString(),
                     format("{'command'='testFile=\"/home/codenvy/codenvy-data/conf/general.properties\"; while true; do     if ! sudo grep \"runner1.hostname\" ${testFile}; then break; fi;     sleep 5; done; sleep 15; # delay to involve into start of rebooting api server', 'agent'='{'host'='api.example.com', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
        assertEquals(commands.get(6).toString(), "Wait until artifact 'codenvy' becomes alive");
        assertEquals(commands.get(7).toString(), "{'command'='sudo puppet cert clean runner1.hostname', 'agent'='LocalAgent'}");
        assertEquals(commands.get(8).toString(), "{'command'='sudo systemctl restart puppetmaster', 'agent'='LocalAgent'}");
        assertEquals(commands.get(9).toString(),
                     format("{'command'='sudo systemctl stop puppet', 'agent'='{'host'='runner1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
        assertEquals(commands.get(10).toString(),
                     format("{'command'='sudo rm -rf /var/lib/puppet/ssl', 'agent'='{'host'='runner1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}",
                            SYSTEM_USER_NAME));
    }

    @Test(expectedExceptions = NodeException.class, expectedExceptionsMessageRegExp = "error")
    public void testGetRemoveNodeCommandWithNodeException() throws Exception {
        Config mockConfig = mock(Config.class);
        doReturn(mockConfig).when(mockConfigManager).loadInstalledCodenvyConfig();
        doThrow(new IllegalArgumentException("error")).when(mockConfig)
                                                      .getValue(NodeConfig.NodeType.API.toString().toLowerCase() + Config.NODE_HOST_PROPERTY_SUFFIX);

        spyHelperCodenvy3.getRemoveNodeCommand(TEST_NODE, ADDITIONAL_RUNNERS_PROPERTY_NAME);
    }

    @Test
    public void shouldBeOkWhenInstallTypeIsMultiServerCodenvy() throws Exception {
        prepareMultiNodeEnv(mockConfigManager);
        spyHelperCodenvy3.checkInstallType();
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = "You can add/remove node in Multi-Server Codenvy only")
    public void shouldThrowExceptionWhenInstallTypeIsSingleServerCodenvy() throws Exception {
        prepareSingleNodeEnv(mockConfigManager);
        spyHelperCodenvy3.checkInstallType();
    }

    @Test
    public void testRecognizeNodeTypeFromConfigBy() throws Exception {
        doReturn(NodeConfig.NodeType.RUNNER).when(mockNodeConfigHelper).recognizeNodeTypeFromConfigByDns(TEST_NODE_DNS);
        assertEquals(spyHelperCodenvy3.recognizeNodeTypeFromConfigBy(TEST_NODE_DNS), NodeConfig.NodeType.RUNNER);        
    }

    @Test
    public void testGetPropertyNameBy() throws Exception {
        doReturn(ADDITIONAL_RUNNERS_PROPERTY_NAME).when(mockNodeConfigHelper).getPropertyNameByType(NodeConfig.NodeType.RUNNER);
        assertEquals(spyHelperCodenvy3.getPropertyNameBy(NodeConfig.NodeType.RUNNER), ADDITIONAL_RUNNERS_PROPERTY_NAME);
    }

    @Test
    public void testRecognizeNodeConfigFromDns() throws Exception {
        doReturn(TEST_NODE).when(mockNodeConfigHelper).recognizeNodeConfigFromDns(TEST_NODE_DNS);
        assertEquals(spyHelperCodenvy3.recognizeNodeConfigFromDns(TEST_NODE_DNS), TEST_NODE);        
    }

    @Test
    public void testNodesConfigHelper() throws Exception {
        prepareMultiNodeEnv(mockConfigManager);
        NodeConfigHelper helper = spyHelperCodenvy3.getNodeConfigHelper(new Config(Collections.EMPTY_MAP));
        assertNotNull(helper);
    }

    @Test
    public void testGetNodes() throws Exception {
        ImmutableMap<String, ImmutableList<String>> testBuilders = ImmutableMap.of(Config.ADDITIONAL_BUILDERS, ImmutableList.of("builder1.test.com", "builder2.test.com"));
        doReturn(testBuilders).when(mockNodeConfigHelper).extractNodesDns(NodeConfig.NodeType.BUILDER);

        ImmutableMap<String, ImmutableList<String>> testRunners = ImmutableMap.of(Config.ADDITIONAL_RUNNERS, ImmutableList.of("runner2.test.com"));
        doReturn(testRunners).when(mockNodeConfigHelper).extractNodesDns(NodeConfig.NodeType.RUNNER);

        Map result = spyHelperCodenvy3.getNodes();
        Map expected = new HashMap(testBuilders);
        expected.putAll(testRunners);
        assertEquals(result, expected);
    }

    @Test
    public void testValidateLicenseDoNothing() throws IOException {
        spyHelperCodenvy3.validateLicense();
    }
}
