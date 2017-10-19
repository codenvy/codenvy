/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.swarm.client;

import com.codenvy.swarm.client.model.DockerNode;
import java.io.IOException;
import java.util.List;

// TODO consider should it be DockerNode || URI || something else

/**
 * Node selection strategy for Swarm. Used for not implemented yet in Swarm docker operations.
 * Should be replaced later with native Swarm methods
 *
 * @author Eugene Voevodin
 */
public interface NodeSelectionStrategy {

  DockerNode select(List<DockerNode> nodes) throws IOException;
}
