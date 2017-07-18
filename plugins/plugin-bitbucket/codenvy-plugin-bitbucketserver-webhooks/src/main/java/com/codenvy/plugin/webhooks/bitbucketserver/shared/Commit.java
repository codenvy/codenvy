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
 * Represents a single commit from push.
 *
 * @author Igor Vinokur
 */
@DTO
public interface Commit {

    /**
     * Returns Id of the commit.
     */
    String getId();

    void setId(String id);

    Commit withId(String id);

    /**
     * Returns message of the commit.
     */
    String getMessage();

    void setMessage(String message);

    Commit withMessage(String message);
}
