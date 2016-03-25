/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
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
