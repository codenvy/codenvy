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
package com.codenvy.swarm.client.model;

/**
 * Represents node where docker runs.
 * Used for workarounds because of not implemented APIs in Swarm
 *
 * @author Eugene Voevodin
 */
public class DockerNode {
    //TODO add ram and containers ?
    private final String hostname;
    private final String addr;

    public DockerNode(String hostname, String addr) {
        this.hostname = hostname;
        this.addr = addr;
    }

    public String getAddr() {
        return addr;
    }

    public String getHostname() {
        return hostname;
    }
}
