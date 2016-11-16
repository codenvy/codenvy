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
package com.codenvy.machine;

import com.google.common.util.concurrent.Striped;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.machine.DockerInstance;
import org.eclipse.che.plugin.docker.machine.DockerInstanceProcessesCleaner;
import org.eclipse.che.plugin.docker.machine.DockerInstanceStopDetector;
import org.eclipse.che.plugin.docker.machine.DockerMachineFactory;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.concurrent.locks.Lock;

/**
 * @author Max Shaposhnik
 */
public class HostedDockerInstance extends DockerInstance {

    private static final Striped<Lock> NODE_LOCKS = Striped.lazyWeakLock(16);

    @Inject
    public HostedDockerInstance(DockerConnector docker,
                                @Named("che.docker.registry") String registry,
                                @Named("che.docker.namespace") @Nullable String registryNamespace,
                                DockerMachineFactory dockerMachineFactory,
                                @Assisted Machine machine,
                                @Assisted("container") String container,
                                @Assisted("image") String image,
                                @Assisted DockerNode node,
                                @Assisted LineConsumer outputConsumer,
                                DockerInstanceStopDetector dockerInstanceStopDetector,
                                DockerInstanceProcessesCleaner processesCleaner,
                                @Named("che.docker.registry_for_snapshots") boolean snapshotUseRegistry) {
        super(docker,
              registry,
              registryNamespace,
              dockerMachineFactory,
              machine,
              container,
              image,
              node,
              outputConsumer,
              dockerInstanceStopDetector,
              processesCleaner,
              snapshotUseRegistry);
    }

    @Override
    protected void commitContainer(String repository, String tag) throws IOException {
        final Lock nodeLock = NODE_LOCKS.get(getNode().getHost());
        nodeLock.lock();
        try {
            super.commitContainer(repository, tag);
        } finally {
            nodeLock.unlock();
        }
    }
}
