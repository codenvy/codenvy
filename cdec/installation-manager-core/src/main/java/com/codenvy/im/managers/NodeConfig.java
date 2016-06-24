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
package com.codenvy.im.managers;

import org.eclipse.che.commons.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/** @author Dmytro Nochevnov */
public class NodeConfig {

    /** (API node should be last one to accumulate changes from the other nodes ) */
    public enum NodeType {
        DATA,
        SITE,
        BUILDER,
        RUNNER,
        DATASOURCE,
        ANALYTICS,
        API,
        MACHINE_NODE  // for Codenvy 4.0
    }

    private String host;
    private int port = 22;
    private String user;
    private Path privateKeyFile = Paths.get("~/.ssh/id_rsa");  // there should be absolute path to file
    private NodeType type;

    public NodeConfig(NodeType type, String host) {
        this.type = type;
        this.host = host;
    }

    public NodeConfig(NodeType type, String host, String user) {
        this.type = type;
        this.host = host;
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public NodeConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    /**
     * @throws IllegalArgumentException if the port = 0.
     */
    public NodeConfig setPort(int port) throws IllegalArgumentException {
        if (port <= 0) {
            throw new IllegalArgumentException("Port number must be greater than zero");
        }

        this.port = port;
        return this;
    }

    public String getUser() {
        return user;
    }

    public NodeConfig setUser(String user) {
        this.user = user;
        return this;
    }

    public Path getPrivateKeyFile() {
        return privateKeyFile;
    }

    public NodeConfig setPrivateKeyFile(Path privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
        return this;
    }

    public NodeType getType() {
        return type;
    }


    public void setType(NodeType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(format("{'host':'%1$s', 'port':'%2$s'", host, port));
        if (user != null) {
            result.append(format(", 'user':'%1$s'", user));
        }
                             
        return result.append(format(", 'privateKeyFile':'%1$s', 'type':'%2$s'}", privateKeyFile, type))
                     .toString();
    }

    /**
     * @return node config of all types of node where hostname are being obtained from the config properties
     */
    public static List<NodeConfig> extractConfigsFrom(Config config) {
        List<NodeConfig> nodeConfigs = new ArrayList<>();
        for (NodeType type: NodeType.values()) {
            NodeConfig node = extractConfigFrom(config, type);
            if (node != null) {
                nodeConfigs.add(node);
            }
        }

        return nodeConfigs;
    }

    /**
     * @return config of node of certain type with hostname which is being obtained from the config properties.
     */
    @Nullable
    public static NodeConfig extractConfigFrom(Config config, NodeType type) {
        String nodeHostPropertyName = type.toString().toLowerCase() + Config.NODE_HOST_PROPERTY_SUFFIX;
        String nodeHost = config.getValue(nodeHostPropertyName);
        String nodeSshUser = config.getValue(Config.NODE_SSH_USER_NAME);
        if (nodeHost != null && !nodeHost.isEmpty()) {
            return new NodeConfig(type, nodeHost, nodeSshUser);
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NodeConfig that = (NodeConfig)o;

        if (port != that.port) {
            return false;
        }
        if (host != null ? !host.equals(that.host) : that.host != null) {
            return false;
        }
        if (privateKeyFile != null ? !privateKeyFile.equals(that.privateKeyFile) : that.privateKeyFile != null) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (user != null ? !user.equals(that.user) : that.user != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (privateKeyFile != null ? privateKeyFile.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
