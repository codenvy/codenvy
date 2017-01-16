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
import java.util.Random;

/**
 * Random selection strategy for Swarm connector.
 *
 * @author Eugene Voevodin
 */
public class RandomNodeSelectionStrategy implements NodeSelectionStrategy {

    private static final Random random = new Random();

    @Override
    public DockerNode select(List<DockerNode> uris) throws IOException {
        if (uris.size() == 0) {
            throw new IOException("No docker nodes available");
        }
        return uris.get(random.nextInt(uris.size()));
    }
}
