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
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.NodeConfig;
import com.google.common.collect.ImmutableList;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;
import java.util.StringJoiner;

import static java.lang.String.format;

/** @author Dmytro Nochevnov */
public class NodeConfigHelperCodenvy3Impl extends NodeConfigHelper {
    
    private static final List<Node> NODE_TYPES = ImmutableList.of(Node.BUILDER, Node.RUNNER);

    private static final String NODE_URL_TEMPLATE = "http://%1$s:8080/%2$s/internal/%2$s";

    private static final String NODE_REGEX = "^http(|s)://";

    public static final char NODE_DELIMITER = ',';

    public NodeConfigHelperCodenvy3Impl(Config config) {
        super(config);
    }

    /**
     * Iterate through registered additional node types to find type which = prefix of dns, and then return NodeConfig(found_type, dns).
     * For example (Codenvy3): given:
     * dns = "builder2.dev.com"
     * $builder_host_name = "builder1.dev.com"  => base_node_domain = ".dev.com"
     * Result = new NodeConfig(BUILDER, "builder2.dev.com")
     * <p/>
     * Example 2: given:
     * dns = "builder2.dev.com"
     * $builder_host_name = "builder1.example.com"  => base_node_domain = ".example.com"  != ".dev.com"
     * Result = IllegalArgumentException("Illegal DNS name 'builder2.dev.com' of additional node....)
     *
     * @throws IllegalArgumentException
     *         if dns doesn't comply with convention '{supported_node_type}{number}{base_node_domain}'
     */
    @Override
    public NodeConfig recognizeNodeConfigFromDns(String dns) throws IllegalArgumentException, IllegalStateException {
        StringJoiner allowedDnsTemplates = new StringJoiner(", ", "[", "]");

        for (Node item : getNodeListProperties()) {
            NodeConfig.NodeType type = item.getType();
            String baseNodeDomain = getBaseNodeDomain(type, config);

            String typeString = item.getDnsPrefix();
            String regex = format("^%s\\d+%s$",
                                  typeString,
                                  baseNodeDomain.replace(".", "\\."));

            if (dns != null && dns.toLowerCase().matches(regex)) {
                return new NodeConfig(type, dns);
            }

            allowedDnsTemplates.add(format("'%1$s<number>%2$s'",
                                           typeString,
                                           baseNodeDomain));
        }

        throw new IllegalArgumentException(format("Illegal DNS name '%s' of node. Correct DNS name templates: %s",
                                                  dns,
                                                  allowedDnsTemplates));
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
    public String getBaseNodeDomain(NodeConfig.NodeType type, Config config) {
        NodeConfig baseNode = NodeConfig.extractConfigFrom(config, type);
        if (baseNode == null) {
            throw new IllegalStateException(format("Host name of base node of type '%s' wasn't found.", type));
        }

        return ConfigManager.getBaseNodeDomain(baseNode).toLowerCase();
    }

    /**
     * (this method is useless and so isn't implemented yet.)
     */
    @Override
    public int getNodeNumber() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    /**
     * Read all urls of nodes stored from the puppet master config, find out node with certain dns and return type of node with certain dns.
     * For example (Codenvy3): given:
     * $additional_builders = "http://builder2.example.com:8080/builder/internal/builder,http://builder3.example.com:8080/builder/internal/builder"
     * dns = "builder3.example.com"
     * Result = BUILDER
     */
    @Nullable
    public NodeConfig.NodeType recognizeNodeTypeFromConfigByDns(String dns) {
        for (Node item : getNodeListProperties()) {
            List<String> nodes = getNodes(item.getProperty());
            if (nodes == null) {
                continue;
            }

            for (String nodeDns : nodes) {
                if (nodeDns.contains(dns)) {
                    return item.getType();
                }
            }
        }

        return null;
    }
}
