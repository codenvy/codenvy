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
package com.codenvy.plugin.webhooks.vsts.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface ResourceContainers {

    /**
     * Get collection container.
     *
     * @return {@link ResourceContainer} collection
     */
    ResourceContainer getCollection();

    void setCollection(final ResourceContainer collection);

    ResourceContainers withCollection(final ResourceContainer collection);

    /**
     * Get account container.
     *
     * @return {@link ResourceContainer} account
     */
    ResourceContainer getAccount();

    void setAccount(final ResourceContainer account);

    ResourceContainers withAccount(final ResourceContainer account);

    /**
     * Get project container.
     *
     * @return {@link ResourceContainer} project
     */
    ResourceContainer getProject();

    void setProject(final ResourceContainer project);

    ResourceContainers withProject(final ResourceContainer project);
}
