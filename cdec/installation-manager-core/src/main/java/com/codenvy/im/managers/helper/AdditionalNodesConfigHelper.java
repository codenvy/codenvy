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

import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.NodeConfig;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Joiner.on;
import static java.lang.String.format;

/** @author Dmytro Nochevnov */
public abstract class AdditionalNodesConfigHelper {

    protected Config config;

    private static final String NODE = "node";

    enum AdditionalNode {        
        RUNNER(NodeConfig.NodeType.RUNNER, Config.ADDITIONAL_RUNNERS, NodeConfig.NodeType.RUNNER.toString().toLowerCase()),
        BUILDER(NodeConfig.NodeType.BUILDER, Config.ADDITIONAL_BUILDERS, NodeConfig.NodeType.BUILDER.toString().toLowerCase()),
        MACHINE_NODE(NodeConfig.NodeType.MACHINE_NODE, Config.SWARM_NODES, NODE);
        
        private NodeConfig.NodeType type;
        private String property;
        private String dnsPrefix;

        protected Config config;
        
        AdditionalNode(NodeConfig.NodeType type, String property, String dnsPrefix) {
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
            for (AdditionalNode item : AdditionalNode.values()) {
                if (item.getType().equals(type)) {
                    return item.getProperty();
                }
            }
            
            return null;
        }
    }

    public AdditionalNodesConfigHelper(Config config) {
        this.config = config;
    }

    /**
     * Read all urls of additional nodes stored from the puppet master config, find out node with certain dns and return type of additional node with
     * certain dns.
     * For example (Codenvy3): given:
     * $additional_builders = "http://builder2.example.com:8080/builder/internal/builder,http://builder3.example.com:8080/builder/internal/builder"
     * dns = "builder3.example.com"
     * Result = BUILDER
     */
    @Nullable
    public NodeConfig.NodeType recognizeNodeTypeFromConfigBy(String dns) {
        for (AdditionalNode item : getAdditionalNodes()) {
            String additionalNodes = config.getValueWithoutSubstitution(item.getProperty());

            if (additionalNodes != null && additionalNodes.contains(dns)) {
                return item.getType();
            }
        }

        return null;
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
     *         if dns doesn't comply with convention '<supported_node_type><number>(base_node_domain)'
     */
    public NodeConfig recognizeNodeConfigFromDns(String dns) throws IllegalArgumentException, IllegalStateException {
        StringJoiner allowedDnsTemplates = new StringJoiner(", ", "[", "]");
        
        for (AdditionalNode item : getAdditionalNodes()) {
            NodeConfig.NodeType type = item.getType();
            String baseNodeDomain = getBaseNodeDomain(type, config);
                       
            String typeString = item.getDnsPrefix();
            String regex = format("^%s\\d+%s$",
                                  typeString,
                                  baseNodeDomain);

            if (dns != null && dns.toLowerCase().matches(regex)) {
                return new NodeConfig(type, dns);
            }
            
            allowedDnsTemplates.add(format("'%s<number>%s'", 
                                           typeString, 
                                           baseNodeDomain));
        }

        throw new IllegalArgumentException(format("Illegal DNS name '%s' of additional node. Correct DNS name templates: %s", 
                                                  dns, 
                                                  allowedDnsTemplates));
    }

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
    public String getPropertyNameBy(NodeConfig.NodeType nodeType) {
        return AdditionalNode.getProperty(nodeType);
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
     *         if there is adding node in the list of additional nodes
     * @throws IllegalStateException
     *         if additional nodes property isn't found in Codenvy config
     */
    public String getValueWithNode(NodeConfig addingNode) throws IllegalArgumentException, IllegalStateException {
        String additionalNodesProperty = getPropertyNameBy(addingNode.getType());
        List<String> nodesUrls = getNodes(additionalNodesProperty);
        if (nodesUrls == null) {
            throw new IllegalStateException(format("Additional nodes property '%s' isn't found in Codenvy config", additionalNodesProperty));
        }

        String nodeUrl = getAdditionalNodeUrl(addingNode);
        if (nodesUrls.contains(nodeUrl)) {
            throw new IllegalArgumentException(format("Node '%s' has been already used", addingNode.getHost()));
        }

        nodesUrls.add(nodeUrl);

        return on(getAdditionalNodeDelimiter()).skipNulls().join(nodesUrls);
    }

    /**
     * Erase url of removing node from the list of additional nodes of type = removingNode.getType() of the configuration of puppet master,
     * and return this list as row with comma-separated values.
     * For example (Codenvy3): given:
     * $additional_builders = "http://builder2.example.com:8080/builder/internal/builder,http://builder3.example.com:8080/builder/internal/builder"
     * removingNode = new NodeConfig(BUILDER, "builder3.example.com")
     * Result = "http://builder2.example.com:8080/builder/internal/builder"
     *
     * @throws IllegalArgumentException
     *         if there is no removing node in the list of additional nodes
     * @throws IllegalStateException
     *         if additional nodes property isn't found in Codenvy config
     */
    public String getValueWithoutNode(NodeConfig removingNode) throws IllegalArgumentException {
        String additionalNodesProperty = getPropertyNameBy(removingNode.getType());
        List<String> nodesUrls = getNodes(additionalNodesProperty);
        if (nodesUrls == null) {
            throw new IllegalStateException(format("Additional nodes property '%s' isn't found in Codenvy config", additionalNodesProperty));
        }

        String nodeUrl = getAdditionalNodeUrl(removingNode);
        if (!nodesUrls.contains(nodeUrl)) {
            throw new IllegalArgumentException(format("There is no node '%s' in the list of additional nodes", removingNode.getHost()));
        }

        nodesUrls.remove(nodeUrl);

        return on(getAdditionalNodeDelimiter()).skipNulls().join(nodesUrls);
    }

    /**
     * @return list of additional nodes extracted from value of "additionalNodesProperty" of config. May include dns of default node.
     */
    public List<String> getNodes(String additionalNodesProperty) {
        return config.getAllValues(additionalNodesProperty, String.valueOf(getAdditionalNodeDelimiter()));
    }

    /**
     * @return for given additional node type: Map[{additionalNodesPropertyName}, {List<String> of additionalNodesDns}]
     */
    @Nullable
    public Map<String, List<String>> extractAdditionalNodesDns(NodeConfig.NodeType nodeType) {
        Map<String, List<String>> result = new HashMap<>();

        String additionalNodesProperty = getPropertyNameBy(nodeType);
        List<String> nodesUrls = getAdditionalNodes(additionalNodesProperty);
        if (nodesUrls == null) {
            return null;
        }

        List<String> nodesDns = new ArrayList<>();
        for (String nodeUrl: nodesUrls) {
            String nodeDns = getAdditionalNodeDns(nodeUrl);
            if (nodeDns != null) {
                nodesDns.add(nodeDns);
            }
        }

        result.put(additionalNodesProperty, nodesDns);

        return result;
    }

    /**
     * @return list of additional nodes extracted from value of "additionalNodesProperty" of config. Bypass default node.
     */
    @Nullable
    abstract public List<String> getAdditionalNodes(String additionalNodesProperty);

    /**
     * @return link like "http://builder3.example.com:8080/builder/internal/builder", or "http://runner3.example.com:8080/runner/internal/runner"
     * For example (Codenvy3): given:
     * node = new NodeConfig(BUILDER, "builder2.example.com")
     * Result = "http://builder2.example.com:8080/builder/internal/builder"
     */
    protected String getAdditionalNodeUrl(NodeConfig node) {
        return format(getAdditionalNodeTemplate(),
                      node.getHost(),
                      node.getType().toString().toLowerCase()
                     );
    }

    /**
     * @return dns name like "builder3.example.com"
     * For example (Codenvy3): given:
     * nodeUrl = "http://builder2.example.com:8080/builder/internal/builder"
     * Result = builder2.example.com
     */
    @Nullable
    private String getAdditionalNodeDns(String nodeUrl) {
        String regex = getAdditionalNodeRegex() + "([^:/]+)";
        Pattern p = Pattern.compile(regex);

        Matcher m = p.matcher(nodeUrl);
        if (m.find()) {
            return m.group().replaceAll(getAdditionalNodeRegex(), "");
        }

        return null;
    }

    /**
     * @return regex to recognize additional node url
     */
    public abstract String getAdditionalNodeRegex();

    /**
     * @return template of additional node url based on node dns
     */
    public abstract String getAdditionalNodeTemplate();

    /**
     * @return names of properties of config file which hold additional node urls
     */
    public abstract List<AdditionalNode> getAdditionalNodes();

    /**
     * @return char which is used to separate urls of additional nodes in puppet config
     */
    public abstract char getAdditionalNodeDelimiter();
}
