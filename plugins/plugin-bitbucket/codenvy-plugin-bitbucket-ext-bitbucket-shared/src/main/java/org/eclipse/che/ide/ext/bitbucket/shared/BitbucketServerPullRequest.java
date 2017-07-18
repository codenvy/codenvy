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
 * Represents a Bitbucket Server pull request.
 *
 * @author Igor Vinokur
 */
@DTO
public interface BitbucketServerPullRequest {

    int getId();

    void setId(int id);

    BitbucketServerPullRequest withId(int id);

    int getVersion();

    void setVersion(int version);

    BitbucketServerPullRequest withVersion(int version);

    String getTitle();

    void setTitle(String title);

    BitbucketServerPullRequest withTitle(String title);

    String getDescription();

    void setDescription(String description);

    BitbucketServerPullRequest withDescription(String description);

    State getState();

    void setState(State state);

    BitbucketServerPullRequest withState(State state);

    BitbucketServerPullRequestLinks getLinks();

    void setLinks(BitbucketServerPullRequestLinks links);

    BitbucketServerPullRequest withLinks(BitbucketServerPullRequestLinks links);

    BitbucketServerAuthor getAuthor();

    void setAuthor(BitbucketServerAuthor author);

    BitbucketServerPullRequest withAuthor(BitbucketServerAuthor author);

    BitbucketServerPullRequestRef getFromRef();

    void setFromRef(BitbucketServerPullRequestRef fromRef);

    BitbucketServerPullRequest withFromRef(BitbucketServerPullRequestRef fromRef);

    BitbucketServerPullRequestRef getToRef();

    void setToRef(BitbucketServerPullRequestRef toRef);

    BitbucketServerPullRequest withToRef(BitbucketServerPullRequestRef toRef);

    enum State {
        OPEN,
        DECLINED,
        MERGED
    }

}
