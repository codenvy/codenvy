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
import org.eclipse.che.dto.shared.JsonFieldName;

@DTO
public interface StorageDocument {

    public String VSTS_DOCUMENT_ETAG = "__etag";

    /**
     * Get document id.
     *
     * @return {@link String} id
     */
    String getId();

    void setId(final String id);

    StorageDocument withId(final String id);

    /**
     * Get document etag.
     *
     * @return {@link String} etag
     */
    @JsonFieldName(VSTS_DOCUMENT_ETAG)
    String getEtag();

    void setEtag(final String etag);

    StorageDocument withEtag(final String etag);

    /**
     * Get document value.
     *
     * @return {@link String} value
     */
    String getValue();

    void setValue(final String value);

    StorageDocument withValue(final String value);
}
