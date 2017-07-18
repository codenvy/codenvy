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

import java.util.List;

/**
 * Represents a Bitbucket Server user.
 *
 * @author Igor Vinokur
 */
@DTO
public interface BitbucketServerUser {
    String getName();

    void setName(String name);

    String getDisplayName();

    void setDisplayName(String displayName);

    String getId();

    void setId(String id);

    BitbucketServerUserLinks getLinks();

    void setLinks(BitbucketServerUserLinks links);

    @DTO
    interface BitbucketServerUserLinks {
        List<BitbucketLink> getSelf();

        void setSelf(List<BitbucketLink> self);
    }
}
