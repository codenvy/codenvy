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
package com.codenvy.swarm.client;

import com.codenvy.swarm.client.model.DockerNode;

import java.io.IOException;
import java.util.List;

//TODO consider should it be DockerNode || URI || something else

/**
 * Node selection strategy for Swarm.
 * Used for not implemented yet in Swarm docker operations.
 * Should be replaced later with native Swarm methods
 *
 * @author Eugene Voevodin
 */
public interface NodeSelectionStrategy {

    DockerNode select(List<DockerNode> nodes) throws IOException;
}
