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
import com.codenvy.im.agent.AgentException;
import com.codenvy.im.agent.ConnectionException;
import com.codenvy.im.commands.Command;
import com.codenvy.im.commands.CommandException;
import com.codenvy.im.commands.CommandLibrary;
import com.codenvy.im.commands.MacroCommand;
import com.codenvy.im.managers.Codenvy4xLicenseManager;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.managers.NodeConfig;
import com.codenvy.im.managers.NodeException;
import com.codenvy.im.utils.HttpTransport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.eclipse.che.api.core.ApiException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/** @author Dmytro Nochevnov */
public class TestNodeManagerHelperCodenvy4Impl extends BaseTest {

    private static final String VALIDATION_NODE_JSON = "{\"value\" : \"true\"}";

    @Mock
    private ConfigManager           mockConfigManager;
    @Mock
    private NodeConfigHelper        mockNodeConfigHelper;
    @Mock
    private Command                 mockCommand;
    @Mock
    private HttpTransport           transport;
    @Mock
    private Codenvy4xLicenseManager codenvy4xLicenseManager;

    private static final String TEST_NODE_DNS = "node1.hostname";
    private static final String TEST_HOST_URL = "hostname";

    private static final NodeConfig.NodeType TEST_NODE_TYPE    = NodeConfig.NodeType.MACHINE_NODE;
    private static final NodeConfig          TEST_NODE         = new NodeConfig(TEST_NODE_TYPE, TEST_NODE_DNS);
    private static final NodeConfig          TEST_DEFAULT_NODE = new NodeConfig(TEST_NODE_TYPE, TEST_HOST_URL);

    private static final String ADDITIONAL_NODES_PROPERTY_NAME = Config.SWARM_NODES;
    private static final String TEST_VALUE_WITH_NODE           = TEST_NODE_DNS + ":2375";

    private NodeManagerHelperCodenvy4Impl spyHelperCodenvy4;

    private int k;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        spyHelperCodenvy4 = spy(new NodeManagerHelperCodenvy4Impl(mockConfigManager, codenvy4xLicenseManager, transport));

        doReturn(ImmutableList.of(Paths.get("/etc/puppet/" + Config.SINGLE_SERVER_4_0_PROPERTIES)).iterator())
            .when(mockConfigManager).getCodenvyPropertiesFiles(InstallType.SINGLE_SERVER);

        doReturn(mockNodeConfigHelper).when(spyHelperCodenvy4).getNodeConfigHelper(any(Config.class));

        initConfigs();
        initLicenseValidationRequest();
        when(transport.doGetWithoutProxy(anyString())).thenReturn(VALIDATION_NODE_JSON);

        k = 0;
    }

    private void initConfigs() throws IOException {
        doReturn(new Config(ImmutableMap.of("host_url", TEST_HOST_URL,
                                            Config.VERSION, "4.0.0",
                                            Config.SWARM_NODES, "$host_url:2375"))).when(mockConfigManager).loadInstalledCodenvyConfig();
        doReturn(InstallType.SINGLE_SERVER).when(mockConfigManager).detectInstallationType();
        doReturn(TEST_VALUE_WITH_NODE).when(mockNodeConfigHelper).getValueWithNode(TEST_NODE);
    }

    private void initLicenseValidationRequest() throws ApiException, IOException {
        when(transport.doGet(anyString())).thenReturn(VALIDATION_NODE_JSON);
    }

    @Test
    public void testGetAddNodeCommand() throws Exception {
        System.setProperty("http.proxyUser", "user1");
        System.setProperty("http.proxyPassword", "passwd1");
        System.setProperty("http.proxyHost", "host1");
        System.setProperty("http.proxyPort", "1111");

        System.setProperty("http.nonProxyHosts", "127.0.0.1|codenvy");

        System.setProperty("https.proxyUser", "user2");
        System.setProperty("https.proxyPassword", "passwd2");
        System.setProperty("https.proxyHost", "host2");
        System.setProperty("https.proxyPort", "2222");

        doReturn(new Config(ImmutableMap.of("host_url", TEST_HOST_URL,
                                            Config.VERSION, "4.0.0",
                                            Config.SWARM_NODES, "$host_url:2375"))).when(mockConfigManager).loadInstalledCodenvyConfig();

        doReturn(ImmutableMap.of(Config.SYSTEM_HTTP_PROXY, "http://user1:passwd1@host1.com:1111",
                                 Config.SYSTEM_HTTPS_PROXY, "https://user2:passwd2@host2.com:2222",
                                 Config.SYSTEM_NO_PROXY, "127.0.0.1|codenvy")).when(mockConfigManager).obtainProxyProperties();

        doReturn(InstallType.SINGLE_SERVER).when(mockConfigManager).detectInstallationType();

        doReturn(TEST_NODE_DNS + ":2375").when(mockNodeConfigHelper).getNodeUrl(TEST_NODE);

        Command result = spyHelperCodenvy4.getAddNodeCommand(TEST_NODE, ADDITIONAL_NODES_PROPERTY_NAME);
        assertNotNull(result);
        assertTrue(result instanceof MacroCommand);

        List<Command> commands = ((MacroCommand) result).getCommands();
        assertEquals(commands.size(), 16);

        assertEquals(commands.get(k++).toString(), "{'command'='if [[ \"$(grep \"node1.hostname\" /etc/puppet/autosign.conf)\" == \"\" ]]; then sudo sh -c \"echo -e 'node1.hostname' >> /etc/puppet/autosign.conf\"; fi', "
                                                   + "'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), format("[{'command'='if test -n \"^proxy=.*$\" && sudo grep -Eq \"^proxy=.*$\" \"/etc/yum.conf\"; then\n"
                                                          + "  sudo sed -i \"s|^proxy=.*$|proxy=http://host1.com:1111|\" \"/etc/yum.conf\" &> /dev/null\n"
                                                          + "fi\n"
                                                          + "if ! sudo grep -Eq \"^proxy=http://host1.com:1111$\" \"/etc/yum.conf\"; then\n"
                                                          + "  echo \"proxy=http://host1.com:1111\" | sudo tee --append \"/etc/yum.conf\" &> /dev/null\n"
                                                          + "fi', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, {'command'='if test -n \"^proxy_username=.*$\" && sudo grep -Eq \"^proxy_username=.*$\" \"/etc/yum.conf\"; then\n"
                                                          + "  sudo sed -i \"s|^proxy_username=.*$|proxy_username=user1|\" \"/etc/yum.conf\" &> /dev/null\n"
                                                          + "fi\n"
                                                          + "if ! sudo grep -Eq \"^proxy_username=user1$\" \"/etc/yum.conf\"; then\n"
                                                          + "  echo \"proxy_username=user1\" | sudo tee --append \"/etc/yum.conf\" &> /dev/null\n"
                                                          + "fi', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, {'command'='if test -n \"^proxy_password=.*$\" && sudo grep -Eq \"^proxy_password=.*$\" \"/etc/yum.conf\"; then\n"
                                                          + "  sudo sed -i \"s|^proxy_password=.*$|proxy_password=passwd1|\" \"/etc/yum.conf\" &> /dev/null\n"
                                                          + "fi\n"
                                                          + "if ! sudo grep -Eq \"^proxy_password=passwd1$\" \"/etc/yum.conf\"; then\n"
                                                          + "  echo \"proxy_password=passwd1\" | sudo tee --append \"/etc/yum.conf\" &> /dev/null\n"
                                                          + "fi', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, {'command'='if test -n \"^use_proxy=.*$\" && sudo grep -Eq \"^use_proxy=.*$\" \"/etc/wgetrc\"; then\n"
                                                          + "  sudo sed -i \"s|^use_proxy=.*$|use_proxy=on|\" \"/etc/wgetrc\" &> /dev/null\n"
                                                          + "fi\n"
                                                          + "if ! sudo grep -Eq \"^use_proxy=on$\" \"/etc/wgetrc\"; then\n"
                                                          + "  echo \"use_proxy=on\" | sudo tee --append \"/etc/wgetrc\" &> /dev/null\n"
                                                          + "fi', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, {'command'='if test -n \"^http_proxy=.*$\" && sudo grep -Eq \"^http_proxy=.*$\" \"/etc/wgetrc\"; then\n"
                                                          + "  sudo sed -i \"s|^http_proxy=.*$|http_proxy=http://user1:passwd1@host1.com:1111|\" \"/etc/wgetrc\" &> /dev/null\n"
                                                          + "fi\n"
                                                          + "if ! sudo grep -Eq \"^http_proxy=http://user1:passwd1@host1.com:1111$\" \"/etc/wgetrc\"; then\n"
                                                          + "  echo \"http_proxy=http://user1:passwd1@host1.com:1111\" | sudo tee --append \"/etc/wgetrc\" &> /dev/null\n"
                                                          + "fi', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, {'command'='if test -n \"^https_proxy=.*$\" && sudo grep -Eq \"^https_proxy=.*$\" \"/etc/wgetrc\"; then\n"
                                                          + "  sudo sed -i \"s|^https_proxy=.*$|https_proxy=https://user2:passwd2@host2.com:2222|\" \"/etc/wgetrc\" &> /dev/null\n"
                                                          + "fi\n"
                                                          + "if ! sudo grep -Eq \"^https_proxy=https://user2:passwd2@host2.com:2222$\" \"/etc/wgetrc\"; then\n"
                                                          + "  echo \"https_proxy=https://user2:passwd2@host2.com:2222\" | sudo tee --append \"/etc/wgetrc\" &> /dev/null\n"
                                                          + "fi', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}, {'command'='if test -n \"^no_proxy=.*$\" && sudo grep -Eq \"^no_proxy=.*$\" \"/etc/wgetrc\"; then\n"
                                                          + "  sudo sed -i \"s|^no_proxy=.*$|no_proxy='127.0.0.1\\|codenvy'|\" \"/etc/wgetrc\" &> /dev/null\n"
                                                          + "fi\n"
                                                          + "if ! sudo grep -Eq \"^no_proxy='127.0.0.1|codenvy'$\" \"/etc/wgetrc\"; then\n"
                                                          + "  echo \"no_proxy='127.0.0.1|codenvy'\" | sudo tee --append \"/etc/wgetrc\" &> /dev/null\n"
                                                          + "fi', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}]", SYSTEM_USER_NAME));

        assertEquals(commands.get(k++).toString(), format("{'command'='if sudo test -d /var/lib/puppet/ssl; then   sudo find /var/lib/puppet/ssl -name node1.hostname.pem -delete; fi', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(commands.get(k++).toString(), "{'command'='yum clean all', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), format("{'command'='if [[ \"$(yum list installed | grep puppetlabs-release)\" == \"\" ]]; then   sudo yum -y -q install https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm; fi', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo yum -y -q install puppet-3.8.6-1.el7.noarch', "
                                                          + "'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo systemctl enable puppet', "
                                                          + "'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertTrue(commands.get(k++).toString().matches(".*'command'='sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back ; "
                                                        + "sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back.[0-9]+.*"), "Actual result: " + commands.get(5).toString());
        assertEquals(commands.get(k++).toString(), format("{'command'='if [[ \"$(grep \"server = hostname\" /etc/puppet/puppet.conf)\" == \"\" ]]; then   sudo sed -i 's/\\[main\\]/\\[main\\]\\n    server = hostname\\n    runinterval = 420\\n    configtimeout = 600\\n/g' /etc/puppet/puppet.conf;   sudo sed -i 's/\\[agent\\]/\\[agent\\]\\n    show_diff = true\\n    pluginsync = true\\n    report = false\\n    default_schedules = false\\n    certname = node1.hostname\\n/g' /etc/puppet/puppet.conf; fi', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(commands.get(k++).toString(), format("{'command'='if [[ \"$(sudo grep \"PUPPET_EXTRA_OPTS=--logdest /var/log/puppet/puppet-agent.log\" /etc/sysconfig/puppetagent)\" == \"\" ]]; then   sudo sh -c 'echo -e \"\\nPUPPET_EXTRA_OPTS=--logdest /var/log/puppet/puppet-agent.log\" >> /etc/sysconfig/puppetagent'; fi', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(commands.get(k++).toString(), format("{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', "
                                                          + "'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(commands.get(k++).toString(), format("PuppetErrorInterrupter{ {'command'='doneState=\"Checking\"; while [ \"${doneState}\" != \"Done\" ]; do     sudo service docker status | grep 'Active: active (running)';     if [ $? -eq 0 ]; then doneState=\"Done\";     else sleep 5;     fi; done', " +
                                                          "'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'} }; looking on errors in file /var/log/puppet/puppet-agent.log locally and at the nodes: " +
                                                          "[{'host':'node1.hostname', 'port':'22', 'privateKeyFile':'~/.ssh/id_rsa', 'type':'MACHINE_NODE'}]", SYSTEM_USER_NAME));
        assertTrue(commands.get(k++).toString().matches("\\{'command'='sudo cp /etc/puppet/manifests/nodes/codenvy/codenvy.pp /etc/puppet/manifests/nodes/codenvy/codenvy.pp.back ; "
                                                        + "sudo cp /etc/puppet/manifests/nodes/codenvy/codenvy.pp /etc/puppet/manifests/nodes/codenvy/codenvy.pp.back.[0-9]+ ; ', "
                                                        + "'agent'='LocalAgent'\\}"), "Actual result: " + commands.get(11).toString());
        assertEquals(commands.get(k++).toString(), "{'command'='sudo cat /etc/puppet/manifests/nodes/codenvy/codenvy.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|(~n[^#]*\\$)swarm_nodes *= *\"[^\"]*\"|\\1swarm_nodes = \"node1.hostname:2375\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp /etc/puppet/manifests/nodes/codenvy/codenvy.pp', "
                                                   + "'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='doneState=\"Checking\"; while [ \"${doneState}\" != \"Done\" ]; do     curl http://hostname:23750/info | grep '\"node1.hostname:2375\"';     if [ $? -eq 0 ]; then doneState=\"Done\";     else sleep 5;     fi; done', "
                                                   + "'agent'='LocalAgent'}");
    }

    @Test
    public void testGetUpdatePuppetConfigCommandWhenNodeListIsEmpty() throws Exception {
        prepareSingleNodeEnv(mockConfigManager);
        Command result = spyHelperCodenvy4.getUpdatePuppetConfigCommand("hostname", "new.hostname");
        assertNotNull(result);
        assertEquals(result.getDescription(), CommandLibrary.EMPTY_COMMAND.getDescription());
    }

    @Test
    public void testGetUpdatePuppetConfigCommand() throws Exception {
        final String oldHostName = "hostname";
        final String newHostName = "new.hostname";
        prepareSingleNodeEnv(mockConfigManager);

        Map<String, String> properties = new HashMap<String, String>() {{
            put("host_url", "hostname");
            put(Config.VERSION, TEST_VERSION_STR);
            put(Config.SWARM_NODES, format("$host_url:2375\n"
                                           + "node1.%1$s:2375\n"
                                           + "node2.%1$s:2375", oldHostName));
        }};
        doReturn(new Config(properties)).when(mockConfigManager).loadInstalledCodenvyConfig();

        doReturn(ImmutableMap.of(Config.SWARM_NODES, ImmutableList.of("hostname", "node1.hostname", "node2.hostname")))
            .when(mockNodeConfigHelper).extractNodesDns(NodeConfig.NodeType.MACHINE_NODE);

        doReturn(Config.SWARM_NODES).when(mockNodeConfigHelper).getPropertyNameByType(NodeConfig.NodeType.MACHINE_NODE);

        doNothing().when(spyHelperCodenvy4).validate(new NodeConfig(TEST_NODE_TYPE, "node1.new.hostname"), "new.hostname");
        doNothing().when(spyHelperCodenvy4).validate(new NodeConfig(TEST_NODE_TYPE, "node2.new.hostname"), "new.hostname");

        Command result = spyHelperCodenvy4.getUpdatePuppetConfigCommand(oldHostName, newHostName);

        verify(spyHelperCodenvy4).validate(new NodeConfig(TEST_NODE_TYPE, "node1.new.hostname"), "new.hostname");
        verify(spyHelperCodenvy4).validate(new NodeConfig(TEST_NODE_TYPE, "node2.new.hostname"), "new.hostname");
        verify(spyHelperCodenvy4, never()).validate(new NodeConfig(TEST_NODE_TYPE, "new.hostname"), "new.hostname");

        assertNotNull(result);
        List<Command> commands = ((MacroCommand) result).getCommands();
        assertEquals(commands.size(), 8);

        int i = 0;
        assertEquals(commands.get(i++).toString(), "{'command'='sudo cat /etc/puppet/autosign.conf | sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|node1.hostname|node1.new.hostname|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp /etc/puppet/autosign.conf', 'agent'='LocalAgent'}");
        assertEquals(commands.get(i++).toString(), "{'command'='sudo cat /etc/puppet/autosign.conf | sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|node2.hostname|node2.new.hostname|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp /etc/puppet/autosign.conf', 'agent'='LocalAgent'}");
        assertEquals(commands.get(i++).toString(), "{'command'='sudo systemctl restart puppetmaster', 'agent'='LocalAgent'}");

        int j = 0;
        List<Command> updateNode1Commands = ((MacroCommand) commands.get(i++)).getCommands();
        assertEquals(updateNode1Commands.size(), 5);
        assertTrue(updateNode1Commands.get(j).toString()
                           .matches(format(
                               "\\{'command'='sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back ; sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back.[0-9]+ ; ', 'agent'='\\{'host'='node1.new.hostname', 'port'='22', 'user'='%s', 'identity'='\\[~/.ssh/id_rsa\\]'\\}'\\}",
                               SYSTEM_USER_NAME)),
                   "Actual command: " + updateNode1Commands.get(j++).toString());

        assertEquals(updateNode1Commands.get(j++).toString(), format("{'command'='sudo sed -i 's/certname = node1.hostname/certname = node1.new.hostname/g' /etc/puppet/puppet.conf', 'agent'='{'host'='node1.new.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(updateNode1Commands.get(j++).toString(), format("{'command'='sudo sed -i 's/server = hostname/server = new.hostname/g' /etc/puppet/puppet.conf', 'agent'='{'host'='node1.new.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(updateNode1Commands.get(j++).toString(), format("{'command'='sudo grep \"dns_alt_names = .*,new.hostname.*\" /etc/puppet/puppet.conf; if [ $? -ne 0 ]; then sudo sed -i 's/dns_alt_names = .*/&,new.hostname/' /etc/puppet/puppet.conf; fi', 'agent'='{'host'='node1.new.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(updateNode1Commands.get(j++).toString(), format("{'command'='sudo systemctl restart puppet', 'agent'='{'host'='node1.new.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));

        j = 0;
        List<Command> updateNode2Commands = ((MacroCommand) commands.get(i++)).getCommands();
        assertEquals(updateNode2Commands.size(), 5);
        assertTrue(updateNode2Commands.get(j).toString()
                                      .matches(format(
                                          "\\{'command'='sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back ; sudo cp /etc/puppet/puppet.conf /etc/puppet/puppet.conf.back.[0-9]+ ; ', 'agent'='\\{'host'='node2.new.hostname', 'port'='22', 'user'='%s', 'identity'='\\[~/.ssh/id_rsa\\]'\\}'\\}",
                                          SYSTEM_USER_NAME)),
                   "Actual command: " + updateNode2Commands.get(j++).toString());

        assertEquals(updateNode2Commands.get(j++).toString(), format("{'command'='sudo sed -i 's/certname = node2.hostname/certname = node2.new.hostname/g' /etc/puppet/puppet.conf', 'agent'='{'host'='node2.new.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(updateNode2Commands.get(j++).toString(), format("{'command'='sudo sed -i 's/server = hostname/server = new.hostname/g' /etc/puppet/puppet.conf', 'agent'='{'host'='node2.new.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(updateNode2Commands.get(j++).toString(), format("{'command'='sudo grep \"dns_alt_names = .*,new.hostname.*\" /etc/puppet/puppet.conf; if [ $? -ne 0 ]; then sudo sed -i 's/dns_alt_names = .*/&,new.hostname/' /etc/puppet/puppet.conf; fi', 'agent'='{'host'='node2.new.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
        assertEquals(updateNode2Commands.get(j++).toString(), format("{'command'='sudo systemctl restart puppet', 'agent'='{'host'='node2.new.hostname', 'port'='22', 'user'='%s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));

        assertTrue(commands.get(i).toString()
                                      .matches("\\{'command'='sudo cp /etc/puppet/manifests/nodes/codenvy/codenvy.pp /etc/puppet/manifests/nodes/codenvy/codenvy.pp.back ; sudo cp /etc/puppet/manifests/nodes/codenvy/codenvy.pp /etc/puppet/manifests/nodes/codenvy/codenvy.pp.back.[0-9]+ ; ', 'agent'='LocalAgent'\\}"),
                   "Actual command: " + commands.get(i++).toString());

        assertEquals(commands.get(i++).toString(), "{'command'='sudo cat /etc/puppet/manifests/nodes/codenvy/codenvy.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|(~n[^#]*\\$)swarm_nodes *= *\"[^\"]*\"|\\1swarm_nodes = \"$host_url:2375\\nnode1.new.hostname:2375\\nnode2.new.hostname:2375\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp /etc/puppet/manifests/nodes/codenvy/codenvy.pp', 'agent'='LocalAgent'}");
        assertEquals(commands.get(i++).toString(), "{'command'='sudo systemctl restart puppet', 'agent'='LocalAgent'}");

    }

    @Test(expectedExceptions = NodeException.class, expectedExceptionsMessageRegExp = "error")
    public void testGetAddNodeCommandNodeException() throws Exception {
        doThrow(new RuntimeException("error")).when(mockConfigManager).getCodenvyPropertiesFiles(InstallType.SINGLE_SERVER);
        spyHelperCodenvy4.getAddNodeCommand(TEST_NODE, ADDITIONAL_NODES_PROPERTY_NAME);
    }

    @Test
    public void testGetRemoveNodeCommand() throws Exception {
        Command removeNodeCommand = spyHelperCodenvy4.getRemoveNodeCommand(TEST_NODE, ADDITIONAL_NODES_PROPERTY_NAME);

        List<Command> commands = ((MacroCommand) removeNodeCommand).getCommands();
        assertEquals(commands.size(), 7);

        assertTrue(commands.get(k++).toString().matches("\\{'command'='sudo cp /etc/puppet/manifests/nodes/codenvy/codenvy.pp /etc/puppet/manifests/nodes/codenvy/codenvy.pp.back ; "
                                                        + "sudo cp /etc/puppet/manifests/nodes/codenvy/codenvy.pp /etc/puppet/manifests/nodes/codenvy/codenvy.pp.back.[0-9]+ ; ', "
                                                        + "'agent'='LocalAgent'\\}"), "Actual command: " + commands.get(0).toString());
        assertEquals(commands.get(k++).toString(), "{'command'='sudo cat /etc/puppet/manifests/nodes/codenvy/codenvy.pp | "
                                                   + "sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|(~n[^#]*\\$)swarm_nodes *= *\"[^\"]*\"|\\1swarm_nodes = \"null\"|g' | "
                                                   + "sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp /etc/puppet/manifests/nodes/codenvy/codenvy.pp', "
                                                   + "'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='LocalAgent'}");

        assertEquals(commands.get(k++).toString(), "{'command'='sudo sed -i '/^node1.hostname$/d' /etc/puppet/autosign.conf', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo puppet node clean node1.hostname', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo systemctl restart puppetmaster', 'agent'='LocalAgent'}");

        assertEquals(commands.get(k++).toString(), "{'command'='testFile=\"/usr/local/swarm/node_list\"; while true; do     if ! sudo grep \"^node1.hostname\" ${testFile}; then break; fi;     sleep 5; done; ', 'agent'='LocalAgent'}");
    }

    @Test(expectedExceptions = NodeException.class, expectedExceptionsMessageRegExp = "error")
    public void testGetRemoveNodeCommandNodeException() throws Exception {
        doThrow(new RuntimeException("error")).when(mockConfigManager).getCodenvyPropertiesFiles(InstallType.SINGLE_SERVER);
        spyHelperCodenvy4.getRemoveNodeCommand(TEST_NODE, ADDITIONAL_NODES_PROPERTY_NAME);
    }

    @Test
    public void shouldBeOkWhenInstallTypeIsMultiServerCodenvy() throws Exception {
        spyHelperCodenvy4.checkInstallType();
    }

    @Test
    public void shouldThrowExceptionWhenInstallTypeIsSingleServerCodenvy() throws Exception {
        spyHelperCodenvy4.checkInstallType();
    }

    @Test
    public void testRecognizeNodeTypeFromConfigBy() throws Exception {
        doReturn(NodeConfig.NodeType.MACHINE_NODE).when(mockNodeConfigHelper).recognizeNodeTypeFromConfigByDns(TEST_NODE_DNS);
        assertEquals(spyHelperCodenvy4.recognizeNodeTypeFromConfigBy(TEST_NODE_DNS), NodeConfig.NodeType.MACHINE_NODE);
    }

    @Test
    public void testGetPropertyNameBy() throws Exception {
        doReturn(ADDITIONAL_NODES_PROPERTY_NAME).when(mockNodeConfigHelper).getPropertyNameByType(NodeConfig.NodeType.RUNNER);
        assertEquals(spyHelperCodenvy4.getPropertyNameBy(NodeConfig.NodeType.RUNNER), ADDITIONAL_NODES_PROPERTY_NAME);
    }

    @Test
    public void testRecognizeNodeConfigFromDns() throws Exception {
        doReturn(TEST_NODE).when(mockNodeConfigHelper).recognizeNodeConfigFromDns(TEST_NODE_DNS);
        assertEquals(spyHelperCodenvy4.recognizeNodeConfigFromDns(TEST_NODE_DNS), TEST_NODE);
    }

    @Test
    public void testNodesConfigHelper() throws Exception {
        NodeConfigHelper helper = spyHelperCodenvy4.getNodeConfigHelper(new Config(Collections.EMPTY_MAP));
        assertNotNull(helper);
    }

    @Test
    public void testGetNodes() throws Exception {
        ImmutableMap<String, ImmutableList<String>> testNodes = ImmutableMap.of(Config.SWARM_NODES, ImmutableList.of("test.com, node1.test.com", "node2.test.com"));
        doReturn(testNodes).when(mockNodeConfigHelper).extractNodesDns(NodeConfig.NodeType.MACHINE_NODE);

        Map result = spyHelperCodenvy4.getNodes();
        assertEquals(result, new HashMap(testNodes));
    }

    @Test
    public void testValidateSingleServerNode() throws Exception {
        prepareSingleNodeEnv(mockConfigManager);

        doReturn(mockCommand).when(spyHelperCodenvy4).getValidateSudoRightsCommand(TEST_NODE);
        doReturn(mockCommand).when(spyHelperCodenvy4).getValidatePuppetMasterAccessibilityCommand(HOSTNAME, TEST_NODE);
        spyHelperCodenvy4.validate(TEST_NODE, HOSTNAME);

        verify(mockCommand, times(2)).execute();
    }

    @Test
    public void testValidateMultiServerNode() throws Exception {
        prepareMultiNodeEnv(mockConfigManager);

        doReturn(mockCommand).when(spyHelperCodenvy4).getValidateSudoRightsCommand(TEST_NODE);

        doReturn(HOSTNAME).when(mockConfigManager).fetchMasterHostName();
        doReturn(mockCommand).when(spyHelperCodenvy4).getValidatePuppetMasterAccessibilityCommand(HOSTNAME, TEST_NODE);

        spyHelperCodenvy4.validate(TEST_NODE, HOSTNAME);

        verify(mockCommand, times(2)).execute();
    }

    @Test(expectedExceptions = NodeException.class, expectedExceptionsMessageRegExp = "agent error")
    public void testValidateNodeAgentException() throws Exception {
        prepareMultiNodeEnv(mockConfigManager);

        doReturn(HOSTNAME).when(mockConfigManager).fetchMasterHostName();

        doReturn(mockCommand).when(spyHelperCodenvy4).getValidateSudoRightsCommand(TEST_NODE);

        doThrow(new AgentException("agent error")).when(spyHelperCodenvy4).getValidatePuppetMasterAccessibilityCommand(HOSTNAME, TEST_NODE);
        spyHelperCodenvy4.validate(TEST_NODE, HOSTNAME);
    }

    @Test(expectedExceptions = NodeException.class, expectedExceptionsMessageRegExp = "It seems user doesn't have sudo rights without password on node 'node1.hostname'.")
    public void testValidateSudoRightsWithoutPasswordCommandException() throws Exception {
        prepareSingleNodeEnv(mockConfigManager);

        doReturn(mockCommand).when(spyHelperCodenvy4).getValidateSudoRightsCommand(TEST_NODE);
        doThrow(new CommandException("command error", new AgentException("agent error", null))).when(mockCommand).execute();

        spyHelperCodenvy4.validate(TEST_NODE, HOSTNAME);
    }

    @Test(expectedExceptions = NodeException.class, expectedExceptionsMessageRegExp = "It seems Puppet Master 'hostname:8140' is not accessible from the node 'node1.hostname'")
    public void testValidatePuppetMasterAccessibilityCommandException() throws Exception {
        prepareSingleNodeEnv(mockConfigManager);

        doReturn(mock(Command.class)).when(spyHelperCodenvy4).getValidateSudoRightsCommand(TEST_NODE);
        doReturn(mockCommand).when(spyHelperCodenvy4).getValidatePuppetMasterAccessibilityCommand(HOSTNAME, TEST_NODE);
        doThrow(new CommandException("command error", new AgentException("agent error", null))).when(mockCommand).execute();

        spyHelperCodenvy4.validate(TEST_NODE, HOSTNAME);
    }

    @Test(expectedExceptions = NodeException.class, expectedExceptionsMessageRegExp = "command error")
    public void testValidateNodeConnectionException() throws Exception {
        prepareSingleNodeEnv(mockConfigManager);

        doReturn(mockCommand).when(spyHelperCodenvy4).getValidateSudoRightsCommand(TEST_NODE);
        doThrow(new CommandException("command error", new ConnectionException("Connection error", null))).when(mockCommand).execute();

        spyHelperCodenvy4.validate(TEST_NODE, HOSTNAME);
    }

    @Test
    public void testValidateSudoRightsWithoutPasswordCommand() throws Exception {
        prepareSingleNodeEnv(mockConfigManager);

        assertEquals(spyHelperCodenvy4.getValidateSudoRightsCommand(TEST_NODE).toString(),
                     format("{'command'='sudo -k -n true 2> /dev/null', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
    }

    @Test
    public void testGetValidatePuppetMasterAccessibilityCommand() throws Exception {
        prepareSingleNodeEnv(mockConfigManager);

        assertEquals(spyHelperCodenvy4.getValidatePuppetMasterAccessibilityCommand("hostname", TEST_NODE).toString(),
                     format("{'command'='sleep 3 && echo >/dev/tcp/hostname/8140', 'agent'='{'host'='node1.hostname', 'port'='22', 'user'='%1$s', 'identity'='[~/.ssh/id_rsa]'}'}", SYSTEM_USER_NAME));
    }

    @Test
    public void testGetAddDefaultNodeCommand() throws Exception {
        doReturn("$host_url:2375").when(mockNodeConfigHelper).getValueWithNode(TEST_DEFAULT_NODE);
        doReturn(TEST_HOST_URL + ":2375").when(mockNodeConfigHelper).getNodeUrl(TEST_DEFAULT_NODE);

        Command result = spyHelperCodenvy4.getAddNodeCommand(TEST_DEFAULT_NODE, ADDITIONAL_NODES_PROPERTY_NAME);
        assertNotNull(result);
        assertTrue(result instanceof MacroCommand);

        List<Command> commands = ((MacroCommand) result).getCommands();
        assertEquals(commands.size(), 4);

        assertTrue(commands.get(k++).toString().matches("\\{'command'='sudo cp /etc/puppet/manifests/nodes/codenvy/codenvy.pp /etc/puppet/manifests/nodes/codenvy/codenvy.pp.back ; "
                                                        + "sudo cp /etc/puppet/manifests/nodes/codenvy/codenvy.pp /etc/puppet/manifests/nodes/codenvy/codenvy.pp.back.[0-9]+ ; ', "
                                                        + "'agent'='LocalAgent'\\}"), "Actual result: " + commands.get(0).toString());
        assertEquals(commands.get(k++).toString(), "{'command'='sudo cat /etc/puppet/manifests/nodes/codenvy/codenvy.pp | sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|(~n[^#]*\\$)swarm_nodes *= *\"[^\"]*\"|\\1swarm_nodes = \"$host_url:2375\"|g' | sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp /etc/puppet/manifests/nodes/codenvy/codenvy.pp', "
                                                   + "'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='doneState=\"Checking\"; while [ \"${doneState}\" != \"Done\" ]; do     curl http://hostname:23750/info | grep '\"hostname:2375\"';     if [ $? -eq 0 ]; then doneState=\"Done\";     else sleep 5;     fi; done', "
                                                   + "'agent'='LocalAgent'}");
    }

    @Test
    public void testGetRemoveDefaultNodeCommand() throws Exception {
        Command removeNodeCommand = spyHelperCodenvy4.getRemoveNodeCommand(TEST_DEFAULT_NODE, ADDITIONAL_NODES_PROPERTY_NAME);

        List<Command> commands = ((MacroCommand) removeNodeCommand).getCommands();
        assertEquals(commands.size(), 4);

        assertTrue(commands.get(k++).toString().matches("\\{'command'='sudo cp /etc/puppet/manifests/nodes/codenvy/codenvy.pp /etc/puppet/manifests/nodes/codenvy/codenvy.pp.back ; "
                                                        + "sudo cp /etc/puppet/manifests/nodes/codenvy/codenvy.pp /etc/puppet/manifests/nodes/codenvy/codenvy.pp.back.[0-9]+ ; ', "
                                                        + "'agent'='LocalAgent'\\}"), "Actual command: " + commands.get(0).toString());
        assertEquals(commands.get(k++).toString(), "{'command'='sudo cat /etc/puppet/manifests/nodes/codenvy/codenvy.pp | "
                                                   + "sed ':a;N;$!ba;s/\\n/~n/g' | sed -E 's|(~n[^#]*\\$)swarm_nodes *= *\"[^\"]*\"|\\1swarm_nodes = \"null\"|g' | "
                                                   + "sed 's|~n|\\n|g' > tmp.tmp && sudo mv tmp.tmp /etc/puppet/manifests/nodes/codenvy/codenvy.pp', "
                                                   + "'agent'='LocalAgent'}");
        assertEquals(commands.get(k++).toString(), "{'command'='sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay --logdest=/var/log/puppet/puppet-agent.log; exit 0;', 'agent'='LocalAgent'}");

        assertEquals(commands.get(k++).toString(), "{'command'='testFile=\"/usr/local/swarm/node_list\"; while true; do     if ! sudo grep \"^hostname\" ${testFile}; then break; fi;     sleep 5; done; ', 'agent'='LocalAgent'}");
    }

    @Test
    public void licenseShouldBeValid() throws IOException, ApiException {
        spyHelperCodenvy4.validateLicense();

        verify(transport).doGetWithoutProxy(anyString());
        verify(mockConfigManager).getApiEndpoint();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Your Codenvy subscription only allows a single server.")
    public void licenseShouldBeInValid() throws IOException, ApiException {
        when(transport.doGetWithoutProxy(anyString())).thenReturn("{\"value\" : \"false\"}");

        spyHelperCodenvy4.validateLicense();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Codenvy License can't be validated.")
    public void processVerifyLicenseShouldBeFailed() throws IOException, ApiException {
        doThrow(IOException.class).when(transport).doGetWithoutProxy(anyString());

        spyHelperCodenvy4.validateLicense();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        System.clearProperty("http.proxyUser");
        System.clearProperty("http.proxyPassword");
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");

        System.clearProperty("http.nonProxyHosts");

        System.clearProperty("https.proxyUser");
        System.clearProperty("https.proxyPassword");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
    }
}
