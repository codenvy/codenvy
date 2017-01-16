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

import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.NodeConfig;
import com.google.common.collect.ImmutableList;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static java.lang.String.format;

/** @author Dmytro Nochevnov */
public class NodeConfigHelperCodenvy4Impl extends NodeConfigHelper {

    private static final List<Node> NODE_TYPES = ImmutableList.of(Node.MACHINE_NODE);

    private static final String NODE_URL_TEMPLATE = "%1$s:2375";

    private static final String NODE_REGEX = "^";

    public static final char NODE_DELIMITER = '\n';

    public NodeConfigHelperCodenvy4Impl(Config config) {
        super(config);
    }

    /**
     * Iterate through registered additional node types to find type which = prefix of dns, and then return NodeConfig(found_type, dns).
     *
     * Example:
     * dns = "node2.dev.com"
     * $swarm_nodes = "node1.example.com"  => base_node_domain = ".example.com"  != ".dev.com"
     * Result = IllegalArgumentException("Illegal DNS name 'node2.dev.com' of additional node....)
     *
     * Example 2: given:
     * dns = "dev.com"
     * $swarm_nodes
     * Result: NodeConfig(machine_node, dev.com)
     *
     * @throws IllegalArgumentException if dns doesn't comply with convention '{supported_node_type}{number}{base_node_domain}' or '{base_node_domain}' (for MACHINE_NODE only)
     */
    public NodeConfig recognizeNodeConfigFromDns(String dns) throws IllegalArgumentException, IllegalStateException {
        StringJoiner allowedDnsTemplates = new StringJoiner(", ", "[", "]");

        for (Node item : getNodeListProperties()) {
            NodeConfig.NodeType type = item.getType();
            String baseNodeDomain = getBaseNodeDomain(type, config);

            String typeString = item.getDnsPrefix();

            String regex;
            if (item.equals(Node.MACHINE_NODE)) {
                // recognize default node as a machine node as well
                regex = format("^(%s\\d+.)?%s$",
                                      typeString,
                                      baseNodeDomain);
            } else {
                regex = format("^%s\\d+.%s$",
                               typeString,
                               baseNodeDomain);
            }

            if (dns != null && dns.toLowerCase().matches(regex)) {
                return new NodeConfig(type, dns);
            }

            allowedDnsTemplates.add(format("'%1$s', '%2$s<number>.%1$s'",
                                           baseNodeDomain,
                                           typeString));
        }

        throw new IllegalArgumentException(format("Illegal DNS name '%s' of node. Correct DNS name templates: %s",
                                                  dns,
                                                  allowedDnsTemplates));
    }

    /**
     * @return node with dns = "{host_url}"
     */
    @Override
    public String getBaseNodeDomain(NodeConfig.NodeType type, Config config) {
        String baseNode = config.getHostUrl();
        if (baseNode == null) {
            throw new IllegalStateException(format("Host name of base node of type '%s' wasn't found.", type));
        }

        return baseNode;
    }

    @Override
    public String getNodeRegex() {
        return NODE_REGEX;
    }

    @Override
    public String getNodeTemplate() {
        return NODE_URL_TEMPLATE;
    }

    @Override
    public List<Node> getNodeListProperties() {
        return NODE_TYPES;
    }

    @Override
    public char getNodeDelimiter() {
        return NODE_DELIMITER;
    }

    @Override
    /**
     * Read all urls of nodes stored from the puppet master config, find out node with certain dns and return type of node with certain dns.
     * For example: given:
     * $swarm_nodes = "$host_url/nnode1.dev.com"
     * $host_url = "dev.com"
     *
     * dns = "node1.dev.com"
     * Result = MACHINE_NODE
     *
     * dns = "dev.com" (default node)
     * Result = MACHINE_NODE
     */
    @Nullable
    public NodeConfig.NodeType recognizeNodeTypeFromConfigByDns(String dns) {
        for (Node item : getNodeListProperties()) {
            List<String> nodes = getNodes(item.getProperty());
            if (nodes == null) {
                continue;
            }

            for (String nodeDns : nodes) {
                if (nodeDns.startsWith(dns)) {
                    return item.getType();
                }
            }
        }

        return null;
    }

    public int getNodeNumber() {
        List<String> nodes = getNodesWithoutSubstitution(getPropertyNameByType(NodeConfig.NodeType.MACHINE_NODE));
        if (Objects.isNull(nodes)) {
            return 0;
        }

        return nodes.size();
    }
}
