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

import com.codenvy.api.license.CodenvyLicense;
import com.codenvy.api.license.InvalidLicenseException;
import com.codenvy.api.license.LicenseException;
import com.codenvy.api.license.LicenseNotFoundException;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.helper.SystemProxySettings;
import com.codenvy.im.commands.Command;
import com.codenvy.im.commands.CommandLibrary;
import com.codenvy.im.commands.MacroCommand;
import com.codenvy.im.commands.decorators.PuppetErrorInterrupter;
import com.codenvy.im.managers.Codenvy4xLicenseManager;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.managers.NodeConfig;
import com.codenvy.im.managers.NodeException;
import com.codenvy.im.managers.UnknownInstallationTypeException;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.Version;
import org.eclipse.che.commons.json.JsonParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static com.codenvy.im.commands.CommandLibrary.createFileBackupCommand;
import static com.codenvy.im.commands.CommandLibrary.createForcePuppetAgentCommand;
import static com.codenvy.im.commands.CommandLibrary.createReplaceCommand;
import static com.codenvy.im.commands.CommandLibrary.createWaitServiceActiveCommand;
import static com.codenvy.im.commands.SimpleCommand.createCommand;
import static com.codenvy.im.utils.Commons.asMap;
import static com.google.api.client.repackaged.com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

/**
 * @author Dmytro Nochevnov
 */
public class NodeManagerHelperCodenvy4Impl extends NodeManagerHelper {
    public static final String YUM_CONF_FILE = "/etc/yum.conf";
    public static final String WGETRC_FILE   = "/etc/wgetrc";

    protected static final String LEGALITY_NODE_LICENSE_SERVICE = "/license/legality/node";
    protected static final String NODE_NUMBER_PARAM             = "nodeNumber";

    private final HttpTransport transport;
    private final Codenvy4xLicenseManager codenvy4xLicenseManager;

    public NodeManagerHelperCodenvy4Impl(ConfigManager configManager,
                                         Codenvy4xLicenseManager codenvy4xLicenseManager,
                                         HttpTransport transport) {
        super(configManager);
        this.codenvy4xLicenseManager = codenvy4xLicenseManager;
        this.transport = transport;
    }

    @Override
    public Command getAddNodeCommand(NodeConfig node, String property) throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        NodeConfigHelper additionalNodesConfigHelper = getNodeConfigHelper(config);

        List<Command> commands = new ArrayList<>();

        try {
            if (!isDefaultNode(node, config.getHostUrl())) {
                // add node into the autosign list of puppet master
                commands.add(createCommand(format("if [[ \"$(grep \"%1$s\" /etc/puppet/autosign.conf)\" == \"\" ]]; "
                                                  + "then sudo sh -c \"echo -e '%1$s' >> /etc/puppet/autosign.conf\"; "
                                                  + "fi",
                                                  node.getHost())));

                String puppetMasterNodeDns;
                if (configManager.detectInstallationType() == InstallType.MULTI_SERVER) {
                    puppetMasterNodeDns = configManager.fetchMasterHostName();
                } else {
                    puppetMasterNodeDns = config.getHostUrl();
                }

                // setup proxy settings
                commands.add(obtainSetupProxySettingsCommand(node));

                // remove outdated certificate of agent from puppet agent, if exists
                commands.add(createCommand(format("if sudo test -d /var/lib/puppet/ssl; then "
                                                  + "  sudo find /var/lib/puppet/ssl -name %s.pem -delete; "
                                                  + "fi", node.getHost()), node));

                // install and enable puppet agent on adding node
                commands.add(createCommand("yum clean all"));   // cleanup to avoid yum install failures
                commands.add(createCommand(format("if [[ \"$(yum list installed | grep puppetlabs-release)\" == \"\" ]]; then "
                                                  + "  sudo yum -y -q install %s; "
                                                  + "fi", config.getValue(Config.PUPPET_RESOURCE_URL)),
                                           node));
                commands.add(createCommand(format("sudo yum -y -q install %s", config.getValue(Config.PUPPET_AGENT_PACKAGE)), node));
                commands.add(createCommand("sudo systemctl enable puppet", node));

                // configure puppet agent
                commands.add(createFileBackupCommand("/etc/puppet/puppet.conf", node));
                commands.add(createCommand(format("if [[ \"$(grep \"server = %1$s\" /etc/puppet/puppet.conf)\" == \"\" ]]; then "
                                                  + "  sudo sed -i 's/\\[main\\]/\\[main\\]\\n"
                                                  + "    server = %1$s\\n"
                                                  + "    runinterval = 420\\n"
                                                  + "    configtimeout = 600\\n/g' /etc/puppet/puppet.conf; "
                                                  + "  sudo sed -i 's/\\[agent\\]/\\[agent\\]\\n"
                                                  + "    show_diff = true\\n"
                                                  + "    pluginsync = true\\n"
                                                  + "    report = false\\n"
                                                  + "    default_schedules = false\\n"
                                                  + "    certname = %2$s\\n/g' /etc/puppet/puppet.conf; "
                                                  + "fi",
                                                  puppetMasterNodeDns,
                                                  node.getHost()),
                                           node));

                // log puppet messages into the /var/log/puppet/puppet-agent.log file instead of /var/log/messages
                commands.add(createCommand("if [[ \"$(sudo grep \"PUPPET_EXTRA_OPTS=--logdest /var/log/puppet/puppet-agent.log\" /etc/sysconfig/puppetagent)\" == \"\" ]]; then "
                                           + "  sudo sh -c 'echo -e \"\\nPUPPET_EXTRA_OPTS=--logdest /var/log/puppet/puppet-agent.log\" >> /etc/sysconfig/puppetagent'; "
                                           + "fi", node));

                // force applying updated puppet config at the adding node
                commands.add(createForcePuppetAgentCommand(node));

                // wait until docker on additional node is installed by puppet; interrupt on puppet puppet errors;
                commands.add(new PuppetErrorInterrupter(createWaitServiceActiveCommand("docker", node),
                                                        node,
                                                        configManager));
            }

            // --- register node in the swarm
            // add node dns to the $swarm_nodes as a separate row
            String value = additionalNodesConfigHelper.getValueWithNode(node);
            Iterator<Path> propertiesFiles = configManager.getCodenvyPropertiesFiles(configManager.detectInstallationType());
            while (propertiesFiles.hasNext()) {
                Path file = propertiesFiles.next();

                commands.add(createFileBackupCommand(file));
                commands.add(CommandLibrary.createPuppetPropertyReplaceCommand(file, property, value));
            }

            // force applying updated puppet config on puppet agent locally
            commands.add(createForcePuppetAgentCommand());

            // wait until adding node is among registered nodes in the output of request on puppet master: curl http://{host_url}:23750/info
            // (see log in file /var/log/swarm.log on puppet master)
            commands.add(createCommand(format("doneState=\"Checking\"; " +
                                              "while [ \"${doneState}\" != \"Done\" ]; do " +
                                              "    curl http://%s:23750/info | grep '%s'; " +
                                              "    if [ $? -eq 0 ]; then doneState=\"Done\"; " +
                                              "    else sleep 5; " +
                                              "    fi; " +
                                              "done",
                                              config.getHostUrl(),
                                              format("\"%s\"", additionalNodesConfigHelper.getNodeUrl(node)))));
        } catch (Exception e) {
            throw new NodeException(e.getMessage(), e);
        }

        return new MacroCommand(commands, "Add node commands");
    }

    @Override
    public Command getRemoveNodeCommand(NodeConfig node, String property) throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        NodeConfigHelper nodeConfigHelper = getNodeConfigHelper(config);

        List<Command> commands = new ArrayList<>();
        try {
            String value = nodeConfigHelper.getValueWithoutNode(node);

            // remove node's dns from the swarm_nodes config property
            Iterator<Path> propertiesFiles = configManager.getCodenvyPropertiesFiles(configManager.detectInstallationType());
            while (propertiesFiles.hasNext()) {
                Path file = propertiesFiles.next();

                commands.add(createFileBackupCommand(file));
                commands.add(CommandLibrary.createPuppetPropertyReplaceCommand(file, property, value));
            }

            // force applying updated puppet config on puppet agent locally
            commands.add(createForcePuppetAgentCommand());

            if (! isDefaultNode(node, config.getHostUrl())) {
                // remove node from autosign.conf
                commands.add(createCommand(format("sudo sed -i '/^%1$s$/d' /etc/puppet/autosign.conf", node.getHost())));

                // remove out-date puppet agent's certificate from puppet master
                commands.add(createCommand(format("sudo puppet node clean %s", node.getHost())));
                commands.add(createCommand("sudo systemctl restart puppetmaster"));
            }

            // wait until there is no removing node in the /usr/local/swarm/node_list
            commands.add(createCommand(format("testFile=\"/usr/local/swarm/node_list\"; " +
                                  "while true; do " +
                                  "    if ! sudo grep \"^%s\" ${testFile}; then break; fi; " +
                                  "    sleep 5; " +  // sleep 5 sec
                                  "done; ", node.getHost())));
        } catch (Exception e) {
            throw new NodeException(e.getMessage(), e);
        }

        return new MacroCommand(commands, "Remove node commands");
    }

    @Override
    public void checkInstallType() throws IllegalStateException, UnknownInstallationTypeException, IOException {
        // Adding/removing nodes are supported in Single-Server and Multi-Server Codenvy.
        // So, do nothing.
    }

    @Override
    public NodeConfigHelper getNodeConfigHelper(Config config) {
        return new NodeConfigHelperCodenvy4Impl(config);
    }

    @Override
    public Command getUpdatePuppetConfigCommand(String oldHostName, String newHostName) throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        NodeConfigHelper nodeConfigHelper = getNodeConfigHelper(config);

        List<Command> commands = new ArrayList<>();

        String nodeListPropertyName = nodeConfigHelper.getPropertyNameByType(NodeConfig.NodeType.MACHINE_NODE);
        List<String> additionalNodes = nodeConfigHelper.extractNodesDns(NodeConfig.NodeType.MACHINE_NODE).get(nodeListPropertyName);
        if (additionalNodes == null) {
            return CommandLibrary.EMPTY_COMMAND;
        }

        // check accessibility of nodes on new host url
        for (String dns : additionalNodes) {
            NodeConfig node = new NodeConfig(NodeConfig.NodeType.MACHINE_NODE, dns);
            if (isDefaultNode(node, config.getHostUrl())) {
                continue;
            }

            node.setHost(node.getHost().replace(oldHostName, newHostName));

            try {
                validate(node, newHostName);
            } catch(NodeException e) {
                throw new NodeException(format("There is a problem with one of the additional nodes with new hostname. %s", e.getMessage()));
            }
        }

        // check autosign.conf on puppet master
        for (String dns : additionalNodes) {
            NodeConfig node = new NodeConfig(NodeConfig.NodeType.MACHINE_NODE, dns);
            if (isDefaultNode(node, config.getHostUrl())) {
                continue;
            }

            String oldNodeHostName = node.getHost();
            String newNodeHostName = oldNodeHostName.replace(oldHostName, newHostName);

            commands.add(createReplaceCommand("/etc/puppet/autosign.conf", oldNodeHostName, newNodeHostName));
        }

        commands.add(createCommand("sudo systemctl restart puppetmaster"));

        // fix configuration of nodes
        for (String dns : additionalNodes) {
            NodeConfig node = new NodeConfig(NodeConfig.NodeType.MACHINE_NODE, dns);

            if (isDefaultNode(node, config.getHostUrl())) {
                continue;
            }

            String oldNodeHostName = node.getHost();
            String newNodeHostName = oldNodeHostName.replace(oldHostName, newHostName);
            node.setHost(newNodeHostName);

            List<Command> nodeCommands = new ArrayList<>();

            nodeCommands.add(createFileBackupCommand("/etc/puppet/puppet.conf", node));
            nodeCommands.add(createCommand(format("sudo sed -i 's/certname = %1$s/certname = %2$s/g' /etc/puppet/puppet.conf", oldNodeHostName,
                                                  newNodeHostName), node));
            nodeCommands.add(createCommand(format("sudo sed -i 's/server = %1$s/server = %2$s/g' /etc/puppet/puppet.conf", oldHostName, newHostName), node));
            nodeCommands.add(createCommand(format("sudo grep \"dns_alt_names = .*,%1$s.*\" /etc/puppet/puppet.conf; "
                                                  + "if [ $? -ne 0 ]; then sudo sed -i 's/dns_alt_names = .*/&,%1$s/' /etc/puppet/puppet.conf; fi", newHostName),
                                           node));  // add new host name to dns_alt_names
            nodeCommands.add(createCommand("sudo systemctl restart puppet", node));

            commands.add(new MacroCommand(nodeCommands, format("Commands to update puppet.conf file on node '%s'", dns)));
        }

        // --- update node in the swarm_nodes list
        String nodeList = configManager.loadInstalledCodenvyConfig().getValueWithoutSubstitution(Config.SWARM_NODES);
        nodeList = nodeList.replace(format(".%s", oldHostName),
                                    format(".%s", newHostName));

        Iterator<Path> propertiesFiles = configManager.getCodenvyPropertiesFiles(configManager.detectInstallationType());
        while (propertiesFiles.hasNext()) {
            Path file = propertiesFiles.next();

            commands.add(createFileBackupCommand(file));
            commands.add(CommandLibrary.createPuppetPropertyReplaceCommand(file, Config.SWARM_NODES, nodeList));
        }

        commands.add(createCommand("sudo systemctl restart puppet"));

        return new MacroCommand(commands, "Commands to update Puppet config on additional nodes.");
    }

    @Override
    public Map<String, List<String>> getNodes() throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();

        NodeConfigHelper helper = getNodeConfigHelper(config);
        Map<String, List<String>> nodes = helper.extractNodesDns(NodeConfig.NodeType.MACHINE_NODE);
        if (nodes != null) {
            return nodes;
        }

        return new HashMap<>();
    }

    @Override
    public void validateLicense() throws IOException {
        if (isCodenvy4Installed()) {
            validateLicenseOn4x();
            return;
        }

        Config config = configManager.loadInstalledCodenvyConfig();
        int nodeNumber = getNodeConfigHelper(config).getNodeNumber() + 1;
        try {
            String legalityNodeUrl = configManager.getApiEndpoint() + LEGALITY_NODE_LICENSE_SERVICE +
                                     "?" + NODE_NUMBER_PARAM + "=" + nodeNumber;
            String value = (String)asMap(transport.doGetWithoutProxy(legalityNodeUrl)).get("value");
            if (!Boolean.valueOf(value)) {
                throw new IllegalStateException("Your Codenvy subscription only allows a single server.");
            }
        }  catch (JsonParseException | IOException e) {
            throw new IllegalStateException("Codenvy License can't be validated.", e);
        }
    }

    private boolean isCodenvy4Installed() throws IOException {
        Optional<Version> version = createArtifact(CDECArtifact.NAME).getInstalledVersion();
        return version.isPresent() && version.get().is4Major();
    }

    private void validateLicenseOn4x() throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        if (getNodeConfigHelper(config).getNodeNumber() == 0) {
            return;
        }

        try {
            CodenvyLicense codenvyLicense = codenvy4xLicenseManager.load();

            CodenvyLicense.LicenseType licenseType = codenvyLicense.getLicenseType();
            if (codenvyLicense.isExpired()) {
                switch (licenseType) {
                    case EVALUATION_PRODUCT_KEY:
                        throw new IllegalStateException("Your Codenvy subscription only allows a single server.");
                    case PRODUCT_KEY:
                    default:
                        // do nothing
                }
            }
        } catch (LicenseNotFoundException e) {
            throw new IllegalStateException("Your Codenvy subscription only allows a single server.");
        } catch (InvalidLicenseException e) {
            throw new IllegalStateException("Codenvy License is invalid or has unappropriated format.");
        } catch (LicenseException e) {
            throw new IllegalStateException("Codenvy License can't be validated.", e);
        }
    }

    public boolean isDefaultNode(NodeConfig node, String hostUrl) {
        return node.getHost().equals(hostUrl);
    }

    /** @return commands to setup next settings carefully (without duplication)
     # In /etc/yum.conf:
     proxy={Config.SYSTEM_HTTP_PROXY : without_credentials}
     proxy_username={"http.proxyUser"}
     proxy_password={"http.proxyPassword"}

     # In /etc/wgetrc:
     use_proxy=on
     http_proxy={Config.SYSTEM_HTTP_PROXY}
     https_proxy={SYSTEM_HTTPS_PROXY}
     no_proxy='{Config.SYSTEM_NO_PROXY}'
     * @param node
     */
    private Command obtainSetupProxySettingsCommand(NodeConfig node) throws IOException {
        List<Command> commands = new ArrayList<>();

        // prepare list of commands to setup /etc/yum.conf file
        Map<String, String> codenvyProxySettings = configManager.obtainProxyProperties();
        String httpProxy = codenvyProxySettings.get(Config.SYSTEM_HTTP_PROXY);
        if (!isNullOrEmpty(httpProxy)) {
            String httpProxyWithoutCredentials = httpProxy.replaceAll("(https?://).*@(.*)", "$1$2");
            commands.add(CommandLibrary.createUpdateFileCommand(Paths.get(YUM_CONF_FILE),
                                                                format("proxy=%s", httpProxyWithoutCredentials),
                                                                "^proxy=.*$",
                                                                node));
        }

        SystemProxySettings javaProxySettings = SystemProxySettings.create();
        if (!isNullOrEmpty(javaProxySettings.getHttpUser())) {
            commands.add(CommandLibrary.createUpdateFileCommand(Paths.get(YUM_CONF_FILE),
                                                                format("proxy_username=%s", javaProxySettings.getHttpUser()),
                                                                "^proxy_username=.*$",
                                                                node));

            if (nonNull(javaProxySettings.getHttpPassword())) {
                commands.add(CommandLibrary.createUpdateFileCommand(Paths.get(YUM_CONF_FILE),
                                                                    format("proxy_password=%s", javaProxySettings.getHttpPassword()),
                                                                    "^proxy_password=.*$",
                                                                    node));
            }
        }

        // prepare list of commands to setup /etc/wgetrc file
        if (codenvyProxySettings.keySet().size() > 0) {
            commands.add(CommandLibrary.createUpdateFileCommand(Paths.get(WGETRC_FILE),
                                                                "use_proxy=on",
                                                                "^use_proxy=.*$",
                                                                node));

            if (codenvyProxySettings.containsKey(Config.SYSTEM_HTTP_PROXY)) {
                commands.add(CommandLibrary.createUpdateFileCommand(Paths.get(WGETRC_FILE),
                                                                    format("http_proxy=%s", codenvyProxySettings.get(Config.SYSTEM_HTTP_PROXY)),
                                                                    "^http_proxy=.*$",
                                                                    node));
            }

            if (codenvyProxySettings.containsKey(Config.SYSTEM_HTTPS_PROXY)) {
                commands.add(CommandLibrary.createUpdateFileCommand(Paths.get(WGETRC_FILE),
                                                                    format("https_proxy=%s", codenvyProxySettings.get(Config.SYSTEM_HTTPS_PROXY)),
                                                                    "^https_proxy=.*$",
                                                                    node));
            }

            if (codenvyProxySettings.containsKey(Config.SYSTEM_NO_PROXY)) {
                commands.add(CommandLibrary.createUpdateFileCommand(Paths.get(WGETRC_FILE),
                                                                    format("no_proxy='%s'", codenvyProxySettings.get(Config.SYSTEM_NO_PROXY)),
                                                                    "^no_proxy=.*$",
                                                                    node));
            }
        }

        return new MacroCommand(commands, "Commands to setup proxy settings");
    }
}
