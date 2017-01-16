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

import com.codenvy.im.artifacts.ArtifactFactory;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.UnsupportedArtifactVersionException;
import com.codenvy.im.commands.Command;
import com.codenvy.im.managers.helper.NodeManagerHelper;
import com.codenvy.im.managers.helper.NodeManagerHelperCodenvy3Impl;
import com.codenvy.im.managers.helper.NodeManagerHelperCodenvy4Impl;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/** @author Dmytro Nochevnov */
@Singleton
public class NodeManager {
    private final ConfigManager                   configManager;
    private final Map<Integer, NodeManagerHelper> HELPERS;

    @Inject
    public NodeManager(ConfigManager configManager,
                       HttpTransport httpTransport,
                       Codenvy4xLicenseManager codenvy4xLicenseManager) throws IOException {
        this.configManager = configManager;

        HELPERS = ImmutableMap.of(
            3, new NodeManagerHelperCodenvy3Impl(configManager),
            4, new NodeManagerHelperCodenvy4Impl(configManager, codenvy4xLicenseManager, httpTransport)
        );
    }

    /**
     * @param dns
     * @throws IllegalArgumentException
     *         if node type isn't supported, or if there is adding node in the list of existed nodes
     */
    public NodeConfig add(String dns) throws IOException, IllegalArgumentException {
        getHelper().checkInstallType();

        Config config = configManager.loadInstalledCodenvyConfig();
        NodeConfig addingNode = getHelper().recognizeNodeConfigFromDns(dns);

        getHelper().validateLicense();

        String nodeSshUser = config.getValue(Config.NODE_SSH_USER_NAME);
        addingNode.setUser(nodeSshUser);

        String property = getHelper().getPropertyNameBy(addingNode.getType());
        if (property == null) {
            throw new IllegalArgumentException("This type of node isn't supported");
        }

        if (! getHelper().isDefaultNode(addingNode, config.getHostUrl())) {
            String puppetMasterNodeDns;
            if (configManager.detectInstallationType() == InstallType.MULTI_SERVER) {
                puppetMasterNodeDns = configManager.fetchMasterHostName();
            } else  {
                puppetMasterNodeDns = config.getHostUrl();
            }

            getHelper().validate(addingNode, puppetMasterNodeDns);
        }

        Command addNodeCommand = getHelper().getAddNodeCommand(addingNode, property);
        addNodeCommand.execute();

        return addingNode;
    }

    /**
     * @throws IllegalArgumentException
     *         if node type isn't supported, or if there is no removing node in the list of existed nodes
     */
    public NodeConfig remove(String dns) throws IOException, IllegalArgumentException {
        getHelper().checkInstallType();

        Config config = configManager.loadInstalledCodenvyConfig();
        NodeConfig.NodeType nodeType = getHelper().recognizeNodeTypeFromConfigBy(dns);
        if (nodeType == null) {
            throw new NodeException(format("Node '%s' is not found in Codenvy configuration", dns));
        }

        String property = getHelper().getPropertyNameBy(nodeType);
        if (property == null) {
            throw new IllegalArgumentException(format("Node type '%s' isn't supported", nodeType));
        }

        String nodeSshUser = config.getValue(Config.NODE_SSH_USER_NAME);
        NodeConfig removingNode = new NodeConfig(nodeType, dns, nodeSshUser);

        Command command = getHelper().getRemoveNodeCommand(removingNode, property);
        command.execute();

        return removingNode;
    }

    public void updatePuppetConfig(String oldHostName, String newHostName) throws IOException {
        Version codenvyVersion = Version.valueOf(configManager.loadInstalledCodenvyConfig().getValue(Config.VERSION));

        if (! codenvyVersion.is4Compatible()) {
            return;
        }

        Command updatePuppetConfigCommand = getHelper().getUpdatePuppetConfigCommand(oldHostName, newHostName);
        updatePuppetConfigCommand.execute();
    }

    public Map<String, List<String>> getNodes() throws IOException {
        return getHelper().getNodes();
    }



    /**
     * @throws IOException, UnsupportedArtifactVersionException
     */
    NodeManagerHelper getHelper() throws IOException {
        Version codenvyVersion = Version.valueOf(configManager.loadInstalledCodenvyConfig().getValue(Config.VERSION));
        if (codenvyVersion.is3Major()) {
            return HELPERS.get(3);
        } else if (codenvyVersion.is4Compatible()) {
            return HELPERS.get(4);
        } else {
            throw new UnsupportedArtifactVersionException(ArtifactFactory.createArtifact(CDECArtifact.NAME), codenvyVersion);
        }
    }
}
