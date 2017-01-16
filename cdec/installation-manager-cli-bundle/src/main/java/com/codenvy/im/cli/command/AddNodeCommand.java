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
package com.codenvy.im.cli.command;

import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.helper.NodeConfigHelper;
import com.codenvy.im.response.NodeInfo;
import com.codenvy.im.response.NodeManagerResponse;
import com.codenvy.im.response.ResponseCode;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableMap;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static com.google.api.client.repackaged.com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Dmytro Nochevnov
 */
@Command(scope = "codenvy", name = "add-node", description = "Add node into Codenvy")
public class AddNodeCommand extends AbstractIMCommand {

    protected static final String DEFAULT_CODENVY_IP_FOR_THE_DOCKER_CONTAINER = "172.17.42.1";

    @Argument(name = "dns", description = "DNS name of the node to add.", required = true, multiValued = false, index = 0)
    private String dns;

    @Option(name = "--codenvy-ip",
            description = "stands for IP of a Codenvy workspace master host. It must be set if workspace master does not have a real DNS that a workspace agent can resolve",
            required = false)
            
    private String codenvyIp;

    @Override
    protected void doExecuteCommand() throws Exception {
        if (!isNullOrEmpty(dns)) {
            try {
                getConsole().showProgressor();

                if (isNullOrEmpty(codenvyIp)) {
                    validateCodenvyConfig();
                } else {
                    updateCodenvyConfig();
                }

                NodeInfo nodeInfo = getFacade().addNode(dns);

                NodeManagerResponse nodeManagerResponse = new NodeManagerResponse();
                nodeManagerResponse.setStatus(ResponseCode.OK);
                nodeManagerResponse.setNode(nodeInfo);

                getConsole().printResponseExitInError(nodeManagerResponse);
            } finally {
                getConsole().hideProgressor();
            }
        }
    }

    protected void validateCodenvyConfig() throws IOException {
        if (isCodenvy4CompatibleInstalled()) {
            Config config = getConfigManager().loadInstalledCodenvyConfig();

            // check on adding default node
            if (NodeConfigHelper.isDefaultNode(config, dns)) {
                return;
            }

            String property = config.getProperties().get(Config.MACHINE_EXTRA_HOSTS);
            if (property == null || property.contains(DEFAULT_CODENVY_IP_FOR_THE_DOCKER_CONTAINER)) {
                throw new IllegalStateException(
                        "This is the first time you add extra node to Codenvy. " +
                        "It is required to set IP of a Codenvy workspace master host. " +
                        "Use the following syntax: add-node --codenvy-ip <CODENVY_IP_ADDRESS> <NODE_DNS>");
            }
        }
    }

    protected boolean isCodenvy4CompatibleInstalled() throws IOException {
        Optional<Version> version = createArtifact(CDECArtifact.NAME).getInstalledVersion();
        return version.isPresent() && version.get().is4Compatible();
    }

    protected void updateCodenvyConfig() throws IOException {
        Map<String, String> props = ImmutableMap.of(Config.MACHINE_EXTRA_HOSTS, "$host_url:" + codenvyIp);
        getFacade().updateArtifactConfig(createArtifact(CDECArtifact.NAME), props);
    }
}
