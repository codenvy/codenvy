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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Docker instance implementation that limits number of simultaneous container commits on the given node.
 *
 * @author Max Shaposhnik
 */
public class HostedDockerInstance extends DockerInstance {

    private static final Map<String, Semaphore> SEMAPHORES = new ConcurrentHashMap<>();
    private static int concurrentCommits;

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
                                @Named("che.docker.registry_for_snapshots") boolean snapshotUseRegistry,
                                @Named("che.docker.concurrent_commits_on_node") int concurrentCommits) {
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
        HostedDockerInstance.concurrentCommits = concurrentCommits;
    }

    @Override
    protected void commitContainer(String repository, String tag) throws IOException, InterruptedException {
        final Semaphore nodeSemahore = getSemaphore(getNode().getHost());
        nodeSemahore.acquire();
        try {
            super.commitContainer(repository, tag);
        } finally {
            nodeSemahore.release();
        }
    }

    private Semaphore getSemaphore(String key) {
        Semaphore semaphore = SEMAPHORES.get(key);
        if (semaphore == null) {
            Semaphore newSemaphore = new Semaphore(concurrentCommits, true);
            semaphore = SEMAPHORES.putIfAbsent(key,newSemaphore);
            if (semaphore == null) {
                semaphore = newSemaphore;
            }
        }
        System.out.println("~~~~~~~~~~~~" + semaphore);
        return semaphore;
    }

}
