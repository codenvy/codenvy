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
 * Represents links from Bitbucket Server pull request.
 */
@DTO
public interface BitbucketServerPullRequestLinks {
    List<BitbucketLink> getSelf();

    void setSelf(List<BitbucketLink> self);

    BitbucketServerPullRequestLinks withSelf(List<BitbucketLink> self);
}
