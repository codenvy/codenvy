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
public interface Relation {

    /**
     * Get relation type.
     *
     * @return {@link String} rel
     */
    String getRel();

    void setRel(final String rel);

    Relation withRel(final String rel);

    /**
     * Get relation url.
     *
     * @return {@link String} url
     */
    String getUrl();

    void setUrl(final String url);

    Relation withUrl(final String url);

    /**
     * Get relation attributes.
     *
     * @return {@link Attributes} attributes
     */
    Attributes getAttributes();

    void setAttributes(final Attributes attributes);

    Relation withAttributes(final Attributes attributes);
}
