/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
