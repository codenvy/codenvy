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
package com.codenvy.plugin.webhooks.bitbucketserver.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents a Bitbucket Server project.
 *
 * @author Igor Vinokur
 */
@DTO
public interface Project {

    /**
     * Returns project's key.
     */
    String getKey();

    void setKey(String key);

    Project withKey(String key);

    /**
     * Returns {@link User} object that represent's project owner.
     */
    User getOwner();

    void setOwner(User owner);

    Project withOwner(User owner);
}
