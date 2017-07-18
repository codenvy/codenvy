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
package com.codenvy.machine;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.DockerConnectorProvider;
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

    private static final Map<String, Semaphore> semaphores = new ConcurrentHashMap<>();
    private final int concurrentCommits;

    @Inject
    public HostedDockerInstance(DockerConnectorProvider dockerConnectorProvider,
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
                                @Named("che.docker.concurrent_commits_on_node") int concurrentCommits) throws MachineException {
        super(dockerConnectorProvider,
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
        this.concurrentCommits = concurrentCommits;
    }

    @Override
    protected void commitContainer(String repository, String tag) throws IOException {
        final Semaphore nodeSemaphore = getSemaphore(getNode().getHost());
        try {
            nodeSemaphore.acquire();
            super.commitContainer(repository, tag);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e.getLocalizedMessage(), e);
        } finally {
            nodeSemaphore.release();
        }
    }

    private Semaphore getSemaphore(String key) {
        Semaphore semaphore = semaphores.get(key);
        if (semaphore == null) {
            Semaphore newSemaphore = new Semaphore(concurrentCommits, true);
            semaphore = semaphores.putIfAbsent(key, newSemaphore);
            if (semaphore == null) {
                semaphore = newSemaphore;
            }
        }
        return semaphore;
    }

}
