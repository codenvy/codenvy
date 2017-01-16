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
package com.codenvy.im.managers.helper;

import com.codenvy.im.agent.AgentException;
import com.codenvy.im.commands.Command;
import com.codenvy.im.commands.CommandLibrary;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.NodeConfig;
import com.codenvy.im.managers.NodeException;
import com.codenvy.im.managers.UnknownInstallationTypeException;
import org.eclipse.che.commons.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Dmytro Nochevnov
 */
public abstract class NodeManagerHelper {
    protected ConfigManager         configManager;

    public NodeManagerHelper(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public abstract Command getAddNodeCommand(NodeConfig node, String property) throws IOException;

    public abstract Command getRemoveNodeCommand(NodeConfig node,
                                                 String property) throws IOException;

    public abstract void checkInstallType() throws IllegalStateException, UnknownInstallationTypeException, IOException;

    /**
     * Read all urls from list of nodes stored from the puppet master config, find out node with certain dns and then return type of this node.
     */
    @Nullable
    public NodeConfig.NodeType recognizeNodeTypeFromConfigBy(String dns) throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        return getNodeConfigHelper(config).recognizeNodeTypeFromConfigByDns(dns);
    }

    /**
     * @return name of property of puppet master config, which holds additional nodes of certain type.
     */
    @Nullable
    public String getPropertyNameBy(NodeConfig.NodeType nodeType) throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        return getNodeConfigHelper(config).getPropertyNameByType(nodeType);
    }

    /**
     * Iterate through registered node types to find type which = prefix of dns, and then return NodeConfig(found_type, dns).
     */
    public NodeConfig recognizeNodeConfigFromDns(String dns) throws IllegalArgumentException, IllegalStateException, IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        return getNodeConfigHelper(config).recognizeNodeConfigFromDns(dns);
    }

    public abstract NodeConfigHelper getNodeConfigHelper(Config config);

    /** Update puppet.conf on additional nodes */
    public abstract Command getUpdatePuppetConfigCommand(String oldHostName, String newHostName) throws IOException;

    /** @return Map<NodeType, List<NodeDns>> */
    public abstract Map<String,List<String>> getNodes() throws IOException;

    /**
     * Check sudo rights without password on node.
     */
    public Command getValidateSudoRightsCommand(NodeConfig node) throws IOException {
        return CommandLibrary.createCheckSudoRightsWithoutPasswordCommand(node);
    }

    /**
     * Check accessibility of puppet master from the node.
     */
    public Command getValidatePuppetMasterAccessibilityCommand(String puppetMasterNodeDns, NodeConfig node) throws IOException {
        return CommandLibrary.createCheckRemotePortOpenedCommand(puppetMasterNodeDns, 8140, node);
    }

    /**
     * Check if node is accessible and user on the node has sudo rights, and node is able to communicate with puppet master.
     * @param node
     * @param puppetMasterNodeDns
     * @throws IOException
     */
    public void validate(NodeConfig node, String puppetMasterNodeDns) throws IOException {
        Command validateSudoRightsCommand = getValidateSudoRightsCommand(node);
        try {
            validateSudoRightsCommand.execute();
        } catch (IOException e) {
            String errorMessage = e.getMessage();
            if (e.getCause() instanceof AgentException) {
                errorMessage = format("It seems user doesn't have sudo rights without password on node '%s'.", node.getHost());
            }

            throw new NodeException(errorMessage, e);
        }

        try {
            Command validatePuppetMasterAccessibilityCommand = getValidatePuppetMasterAccessibilityCommand(puppetMasterNodeDns, node);
            validatePuppetMasterAccessibilityCommand.execute();
        } catch (IOException e) {
            String errorMessage = e.getMessage();
            if (e.getCause() instanceof AgentException) {
                errorMessage = format("It seems Puppet Master '%s:%s' is not accessible from the node '%s'", puppetMasterNodeDns, 8140, node.getHost());
            }

            throw new NodeException(errorMessage, e);
        }
    }

    abstract public void validateLicense() throws IOException;

    abstract public boolean isDefaultNode(NodeConfig node, String hostUrl);
}
