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
import org.eclipse.che.commons.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Joiner.on;
import static java.lang.String.format;

/** @author Dmytro Nochevnov */
public abstract class NodeConfigHelper {

    protected Config config;

    private static final String NODE = "node";

    public static boolean isDefaultNode(Config config, String dns) {
        String hostUrl = config.getHostUrl();
        return hostUrl != null && hostUrl.equals(dns);
    }

    abstract public int getNodeNumber();

    enum Node {
        RUNNER(NodeConfig.NodeType.RUNNER, Config.ADDITIONAL_RUNNERS, NodeConfig.NodeType.RUNNER.toString().toLowerCase()),
        BUILDER(NodeConfig.NodeType.BUILDER, Config.ADDITIONAL_BUILDERS, NodeConfig.NodeType.BUILDER.toString().toLowerCase()),
        MACHINE_NODE(NodeConfig.NodeType.MACHINE_NODE, Config.SWARM_NODES, NODE);
        
        private NodeConfig.NodeType type;
        private String property;
        private String dnsPrefix;

        protected Config config;
        
        Node(NodeConfig.NodeType type, String property, String dnsPrefix) {
            this.type = type;
            this.property = property;
            this.dnsPrefix = dnsPrefix;
        }
        
        public NodeConfig.NodeType getType() {
            return type;
        }

        public String getProperty() {
            return property;
        }
        
        public String getDnsPrefix() {
            return dnsPrefix;
        }
        
        @Nullable
        public static String getProperty(NodeConfig.NodeType type) {
            for (Node item : Node.values()) {
                if (item.getType().equals(type)) {
                    return item.getProperty();
                }
            }
            
            return null;
        }
    }

    public NodeConfigHelper(Config config) {
        this.config = config;
    }

    /**
     * Read all urls of nodes stored from the puppet master config, find out node with certain dns and return type of node with certain dns.
     */
    @Nullable
    abstract public NodeConfig.NodeType recognizeNodeTypeFromConfigByDns(String dns);

    /**
     * Iterate through registered additional node types to find type which = prefix of dns, and then return NodeConfig(found_type, dns).
     *
     * @throws IllegalArgumentException
     *         if dns doesn't comply with convention '{supported_node_type}{number}{base_node_domain}' or '{base_node_domain}' (for MACHINE_NODE only)
     */
    abstract public NodeConfig recognizeNodeConfigFromDns(String dns) throws IllegalArgumentException, IllegalStateException;

    /**
     * @return base node config for certain type of additional node.
     * For example (Codenvy3): given:
     * $runner_host_name=runner1.example.com
     * Result: getBaseNodeDomain(RUNNER, config) => NodeConfig{ dns: "runner1.example.com" }
     */
    public abstract String getBaseNodeDomain(NodeConfig.NodeType type, Config config);

    /**
     * @return name of property of puppet master config, which holds additional nodes of certain type.
     */
    @Nullable
    public String getPropertyNameByType(NodeConfig.NodeType nodeType) {
        return Node.getProperty(nodeType);
    }

    /**
     * Construct url of adding node, add it to the list of additional nodes of type = addingNode.getType() of the configuration of puppet master,
     * and return this list as row with comma-separated values.
     * For example (Codenvy3): given:
     * $additional_builders = "http://builder2.example.com:8080/builder/internal/builder"
     * addingNode = new NodeConfig(BUILDER, "builder3.example.com")
     * Result = "http://builder2.example.com:8080/builder/internal/builder,http://builder3.example.com:8080/builder/internal/builder"
     *
     * @throws IllegalArgumentException
     *         if there is adding node in the list of nodes
     * @throws IllegalStateException
     *         if node list property isn't found in Codenvy config
     */
    public String getValueWithNode(NodeConfig addingNode) throws IllegalArgumentException, IllegalStateException {
        String additionalNodesProperty = getPropertyNameByType(addingNode.getType());

        // get node list with substitution of "$host_url" url of default node
        List<String> nodesUrls = getNodes(additionalNodesProperty);
        if (nodesUrls == null) {
            throw new IllegalStateException(format("Node list property '%s' isn't found in Codenvy config", additionalNodesProperty));
        }

        String nodeUrl = getNodeUrl(addingNode);
        if (nodesUrls.contains(nodeUrl)) {
            throw new IllegalArgumentException(format("Node '%s' has been already used", addingNode.getHost()));
        }

        // get node list again, but with "$host_url" dns of default node
        nodesUrls = getNodesWithoutSubstitution(additionalNodesProperty);

        // restore '$host_url' token linked to default node in node list
        if (addingNode.getHost().equals(config.getHostUrl())) {
            nodeUrl = nodeUrl.replace(addingNode.getHost(), "$" + Config.HOST_URL);
        }

        nodesUrls.add(nodeUrl);

        return on(getNodeDelimiter()).skipNulls().join(nodesUrls);
    }

    /**
     * Erase url of removing node from the list of nodes of type = removingNode.getType() of the configuration of puppet master,
     * and return this list as row with comma-separated values.
     * For example (Codenvy3): given:
     * $additional_builders = "http://builder2.example.com:8080/builder/internal/builder,http://builder3.example.com:8080/builder/internal/builder"
     * removingNode = new NodeConfig(BUILDER, "builder3.example.com")
     * Result = "http://builder2.example.com:8080/builder/internal/builder"
     *
     * @throws IllegalArgumentException
     *         if there is no removing node in the list of node list
     * @throws IllegalStateException
     *         if node list property isn't found in Codenvy config
     */
    public String getValueWithoutNode(NodeConfig removingNode) throws IllegalArgumentException {
        String additionalNodesProperty = getPropertyNameByType(removingNode.getType());
        List<String> nodesUrls = getNodes(additionalNodesProperty);
        if (nodesUrls == null) {
            throw new IllegalStateException(format("Node list property '%s' isn't found in Codenvy config", additionalNodesProperty));
        }

        String nodeUrl = getNodeUrl(removingNode);
        if (!nodesUrls.contains(nodeUrl)) {
            throw new IllegalArgumentException(format("There is no node '%s' in the list of nodes", removingNode.getHost()));
        }

        nodesUrls.remove(nodeUrl);

        return on(getNodeDelimiter()).skipNulls().join(nodesUrls);
    }

    /**
     * @return list of nodes extracted from value of "additionalNodesProperty" of config. Enclosed variables like "$host_url" is substituted with real value.
     */
    public List<String> getNodes(String additionalNodesProperty) {
        return config.getAllValues(additionalNodesProperty, String.valueOf(getNodeDelimiter()));
    }

    /**
     * @return list of nodes extracted from value of "additionalNodesProperty" of config. Enclosed variables like "$host_url" isn't substituted.
     */
    public List<String> getNodesWithoutSubstitution(String additionalNodesProperty) {
        return config.getAllValuesWithoutSubstitution(additionalNodesProperty, String.valueOf(getNodeDelimiter()));
    }

    /**
     * @return for given node type: Map[{nodeListPropertyName}, {List<String> of nodesDns}]
     */
    @Nullable
    public Map<String, List<String>> extractNodesDns(NodeConfig.NodeType nodeType) {
        Map<String, List<String>> result = new HashMap<>();

        String nodeListProperty = getPropertyNameByType(nodeType);
        List<String> nodesUrls = getNodes(nodeListProperty);
        if (nodesUrls == null) {
            return null;
        }

        List<String> nodesDns = new ArrayList<>();
        for (String nodeUrl: nodesUrls) {
            String nodeDns = getNodeDns(nodeUrl);
            if (nodeDns != null) {
                nodesDns.add(nodeDns);
            }
        }

        result.put(nodeListProperty, nodesDns);

        return result;
    }

    /**
     * @return link like "http://builder3.example.com:8080/builder/internal/builder", or "http://runner3.example.com:8080/runner/internal/runner"
     * For example (Codenvy3): given:
     * node = new NodeConfig(BUILDER, "builder2.example.com")
     * Result = "http://builder2.example.com:8080/builder/internal/builder"
     */
    protected String getNodeUrl(NodeConfig node) {
        return format(getNodeTemplate(),
                      node.getHost(),
                      node.getType().toString().toLowerCase()
                     );
    }

    /**
     * @return dns name like "builder3.example.com"
     * For example (Codenvy3): given:
     * nodeUrl = "http://builder2.example.com:8080/builder/internal/builder"
     * Result = builder2.example.com
     *
     * (Codenvy4): given:
     * nodeUrl = "node1.codenvy:2375"
     * Result = node1.codenvy
     */
    @Nullable
    public String getNodeDns(String nodeUrl) {
        String regex = getNodeRegex() + "([^:/]+)";
        Pattern p = Pattern.compile(regex);

        Matcher m = p.matcher(nodeUrl);
        if (m.find()) {
            return m.group().replaceAll(getNodeRegex(), "");
        }

        return null;
    }

    /**
     * @return regex to recognize additional node url
     */
    public abstract String getNodeRegex();

    /**
     * @return template of additional node url based on node dns
     */
    public abstract String getNodeTemplate();

    /**
     * @return names of properties of config file which hold additional node urls
     */
    public abstract List<Node> getNodeListProperties();

    /**
     * @return char which is used to separate urls of additional nodes in puppet config
     */
    public abstract char getNodeDelimiter();
}
