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
public interface Reviewer {

    /**
     * Get reviewer url.
     *
     * @return {@link String} reviewerUrl
     */
    String getReviewerUrl();

    void setReviewerUrl(final String reviewerUrl);

    Reviewer withReviewerUrl(final String reviewerUrl);

    /**
     * Get reviewer vote.
     *
     * @return {@link int} vote
     */
    int getVote();

    void setVote(final int vote);

    Reviewer withVote(final int vote);

    /**
     * Get reviewer id.
     *
     * @return {@link String} id
     */
    String getId();

    void setId(final String id);

    Reviewer withId(final String id);

    /**
     * Get reviewer displayName.
     *
     * @return {@link String} displayName
     */
    String getDisplayName();

    void setDisplayName(final String displayName);

    Reviewer withDisplayName(final String displayName);

    /**
     * Get reviewer uniqueName.
     *
     * @return {@link String} uniqueName
     */
    String getUniqueName();

    void setUniqueName(final String uniqueName);

    Reviewer withUniqueName(final String uniqueName);

    /**
     * Get reviewer url.
     *
     * @return {@link String} url
     */
    String getUrl();

    void setUrl(final String url);

    Reviewer withUrl(final String url);

    /**
     * Get reviewer image url.
     *
     * @return {@link String} imageUrl
     */
    String getImageUrl();

    void setImageUrl(final String imageUrl);

    Reviewer withImageUrl(final String imageUrl);

    /**
     * Get reviewer isContainer.
     *
     * @return {@link boolean} isContainer
     */
    boolean getIsContainer();

    void setIsContainer(final boolean isContainer);

    Reviewer withIsContainer(final boolean isContainer);
}
