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
public interface Message {

    /**
     * Get message text.
     *
     * @return {@link String} text
     */
    String getText();

    void setText(final String text);

    Message withText(final String text);

    /**
     * Get message html.
     *
     * @return {@link String} html
     */
    String getHtml();

    void setHtml(final String html);

    Message withHtml(final String html);

    /**
     * Get message markdown.
     *
     * @return {@link String} markdown
     */
    String getMarkdown();

    void setMarkdown(final String markdown);

    Message withMarkdown(final String markdown);
}
