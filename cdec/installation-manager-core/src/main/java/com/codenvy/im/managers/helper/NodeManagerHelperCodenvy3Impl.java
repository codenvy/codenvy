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

import com.codenvy.im.artifacts.ArtifactFactory;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.commands.Command;
import com.codenvy.im.commands.CommandLibrary;
import com.codenvy.im.commands.MacroCommand;
import com.codenvy.im.commands.WaitOnAliveArtifactCommand;
import com.codenvy.im.commands.decorators.PuppetErrorInterrupter;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.managers.NodeConfig;
import com.codenvy.im.managers.NodeException;
import com.codenvy.im.managers.UnknownInstallationTypeException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.codenvy.im.commands.CommandLibrary.createFileBackupCommand;
import static com.codenvy.im.commands.CommandLibrary.createForcePuppetAgentCommand;
import static com.codenvy.im.commands.SimpleCommand.createCommand;
import static java.lang.String.format;

/**
 * @author Dmytro Nochevnov
 */
public class NodeManagerHelperCodenvy3Impl extends NodeManagerHelper {

    public NodeManagerHelperCodenvy3Impl(ConfigManager configManager) {
        super(configManager);
    }

    /**
     * @return commands to add node into puppet master config and wait until node becomes alive
     */
    @Override
    public Command getAddNodeCommand(NodeConfig node, String property) throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        NodeConfigHelper additionalNodesConfigHelper = getNodeConfigHelper(config);

        List<Command> commands = new ArrayList<>();

        String value = additionalNodesConfigHelper.getValueWithNode(node);
        NodeConfig apiNode = NodeConfig.extractConfigFrom(config, NodeConfig.NodeType.API);

        try {
            // modify puppet master config
            Iterator<Path> propertiesFiles = configManager.getCodenvyPropertiesFiles(InstallType.MULTI_SERVER);
            while (propertiesFiles.hasNext()) {
                Path file = propertiesFiles.next();

                commands.add(createFileBackupCommand(file));
                commands.add(CommandLibrary.createPuppetPropertyReplaceCommand(file, property, value));
            }

            // add node into the autosign list of puppet master
            commands.add(createCommand(format("sudo sh -c \"echo -e '%s' >> /etc/puppet/autosign.conf\"",
                                              node.getHost())));

            String puppetMasterNodeDns = configManager.fetchMasterHostName();

            // install and enable puppet agent on adding node
            commands.add(createCommand("yum clean all"));   // cleanup to avoid yum install failures
            commands.add(createCommand(format("if [ \"`yum list installed | grep puppetlabs-release`\" == \"\" ]; "
                                              + "then sudo yum -y -q install %s; "
                                              + "fi", config.getValue(Config.PUPPET_RESOURCE_URL)), node));
            commands.add(createCommand(format("sudo yum -y -q install %s", config.getValue(Config.PUPPET_AGENT_PACKAGE)), node));
            commands.add(createCommand("sudo systemctl enable puppet", node));

            // configure puppet agent
            commands.add(createFileBackupCommand("/etc/puppet/puppet.conf", node));
            commands.add(createCommand(format("sudo sed -i 's/\\[main\\]/\\[main\\]\\n" +
                                              "  server = %s\\n" +
                                              "  runinterval = 420\\n" +
                                              "  configtimeout = 600\\n/g' /etc/puppet/puppet.conf",
                                              puppetMasterNodeDns),
                                       node));

            commands.add(createCommand(format("sudo sed -i 's/\\[agent\\]/\\[agent\\]\\n" +
                                              "  show_diff = true\\n" +
                                              "  pluginsync = true\\n" +
                                              "  report = false\\n" +
                                              "  default_schedules = false\\n" +
                                              "  certname = %s\\n/g' /etc/puppet/puppet.conf",
                                              node.getHost()),
                                       node));

            // log puppet messages into the /var/log/puppet/puppet-agent.log file instead of /var/log/messages
            commands.add(createCommand("sudo sh -c 'echo -e \"\\nPUPPET_EXTRA_OPTS=--logdest /var/log/puppet/puppet-agent.log\\n\" >> /etc/sysconfig/puppetagent'", node));

            // start puppet agent
            commands.add(createCommand("sudo systemctl start puppet", node));

            // wait until server on additional node is installed; interrupt on puppet errors;
            commands.add(new PuppetErrorInterrupter(createCommand("doneState=\"Installing\"; " +
                                                                  "testFile=\"/home/codenvy/codenvy-tomcat/logs/catalina.out\"; " +
                                                                  "while [ \"${doneState}\" != \"Installed\" ]; do " +
                                                                  "    if sudo test -f ${testFile}; then doneState=\"Installed\"; fi; " +
                                                                  "    sleep 30; " +
                                                                  "done",
                                                                  node),
                                                    node,
                                                    configManager));

            // force applying updated puppet config on puppet agent of API node
            commands.add(createForcePuppetAgentCommand(apiNode));

            // wait until there is a changed configuration on API server
            commands.add(createCommand(format("testFile=\"/home/codenvy/codenvy-data/conf/general.properties\"; " +
                                              "while true; do " +
                                              "    if sudo grep \"%s$\" ${testFile}; then break; fi; " +
                                              "    sleep 5; " +  // sleep 5 sec
                                              "done; " +
                                              "sleep 15; # delay to involve into start of rebooting api server", value),
                                       apiNode));

            // wait until API server restarts
            commands.add(new WaitOnAliveArtifactCommand(ArtifactFactory.createArtifact(CDECArtifact.NAME)));
        } catch (Exception e) {
            throw new NodeException(e.getMessage(), e);
        }

        return new MacroCommand(commands, "Add node commands");
    }

    @Override
    public Command getRemoveNodeCommand(NodeConfig node,
                                        String property) throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        NodeConfigHelper additionalNodesConfigHelper = getNodeConfigHelper(config);

        try {
            String value = additionalNodesConfigHelper.getValueWithoutNode(node);
            NodeConfig apiNode = NodeConfig.extractConfigFrom(config, NodeConfig.NodeType.API);

            List<Command> commands = new ArrayList<>();

            // modify puppet master config
            Iterator<Path> propertiesFiles = configManager.getCodenvyPropertiesFiles(InstallType.MULTI_SERVER);
            while (propertiesFiles.hasNext()) {
                Path file = propertiesFiles.next();

                commands.add(createFileBackupCommand(file));
                commands.add(CommandLibrary.createPuppetPropertyReplaceCommand(file, property, value));
            }

            // force applying updated puppet config for puppet agent on API node
            commands.add(createForcePuppetAgentCommand(apiNode));

            // wait until there node is removed from configuration on API server
            commands.add(createCommand(format("testFile=\"/home/codenvy/codenvy-data/conf/general.properties\"; " +
                                              "while true; do " +
                                              "    if ! sudo grep \"%s\" ${testFile}; then break; fi; " +
                                              "    sleep 5; " +  // sleep 5 sec
                                              "done; " +
                                              "sleep 15; # delay to involve into start of rebooting api server", node.getHost()),
                                       apiNode));

            // wait until API server restarts
            commands.add(new WaitOnAliveArtifactCommand(ArtifactFactory.createArtifact(CDECArtifact.NAME)));

            // remove out-date puppet agent's certificate
            commands.add(createCommand(format("sudo puppet cert clean %s", node.getHost())));
            commands.add(createCommand("sudo systemctl restart puppetmaster"));

            // stop puppet agent on removing node and remove out-date certificate
            commands.add(createCommand("sudo systemctl stop puppet", node));
            commands.add(createCommand("sudo rm -rf /var/lib/puppet/ssl", node));
            return new MacroCommand(commands, "Remove node commands");
        } catch (Exception e) {
            throw new NodeException(e.getMessage(), e);
        }
    }

    @Override
    public void checkInstallType() throws IllegalStateException, UnknownInstallationTypeException, IOException {
        if (configManager.detectInstallationType() != InstallType.MULTI_SERVER) {
            throw new IllegalStateException("You can add/remove node in Multi-Server Codenvy only");
        }
    }

    /**
     * @return empty command because we don't need to update puppet.conf on additional nodes in Codenvy 3.x
     */
    @Override
    public Command getUpdatePuppetConfigCommand(String oldHostName, String newHostName) {
        return CommandLibrary.EMPTY_COMMAND;
    }

    @Override
    public Map<String, List<String>> getNodes() throws IOException {
        Map<String, List<String>> nodes = new HashMap<>();
        Config config = configManager.loadInstalledCodenvyConfig();

        NodeConfigHelper helper = getNodeConfigHelper(config);
        Map<String, List<String>> additionalRunners = helper.extractNodesDns(NodeConfig.NodeType.RUNNER);
        if (additionalRunners != null) {
            nodes.putAll(additionalRunners);
        }

        Map<String, List<String>> additionalBuilders = helper.extractNodesDns(NodeConfig.NodeType.BUILDER);
        if (additionalBuilders != null) {
            nodes.putAll(additionalBuilders);
        }

        return nodes;
    }

    @Override
    public void validateLicense() {
        // Codenvy 3.x doesn't support licenses
    }

    @Override
    public boolean isDefaultNode(NodeConfig node, String hostUrl) {
        return false;
    }

    @Override
    public NodeConfigHelper getNodeConfigHelper(Config config) {
        return new NodeConfigHelperCodenvy3Impl(config);
    }

}
