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
package org.eclipse.che.ide.ext.bitbucket.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents a project in the Bitbucket Server rest API.
 *
 * @author Igor Vinokur
 */
@DTO
public interface BitbucketServerProject {
    String getKey();

    void setKey(String key);

    BitbucketServerProject withKey(String key);

    int getId();

    void setId(int id);

    BitbucketServerProject withId(int id);

    String getName();

    void setName(String name);

    BitbucketServerProject withName(String name);

    BitbucketServerUser getOwner();

    void setOwner(BitbucketServerUser owner);

    BitbucketServerProject withOwner(BitbucketServerUser owner);
}
