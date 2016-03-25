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
public interface CreatedBy {

    /**
     * Get createdby id.
     *
     * @return {@link String} id
     */
    String getId();

    void setId(final String id);

    CreatedBy withId(final String id);

    /**
     * Get createdby displayName.
     *
     * @return {@link String} displayName
     */
    String getDisplayName();

    void setDisplayName(final String displayName);

    CreatedBy withDisplayName(final String displayName);

    /**
     * Get createdby uniqueName.
     *
     * @return {@link String} uniqueName
     */
    String getUniqueName();

    void setUniqueName(final String uniqueName);

    CreatedBy withUniqueName(final String uniqueName);

    /**
     * Get createdby url.
     *
     * @return {@link String} url
     */
    String getUrl();

    void setUrl(final String url);

    CreatedBy withUrl(final String url);

    /**
     * Get createdby image url.
     *
     * @return {@link String} imageUrl
     */
    String getImageUrl();

    void setImageUrl(final String imageUrl);

    CreatedBy withImageUrl(final String imageUrl);
}
