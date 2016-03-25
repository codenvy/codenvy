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
package com.codenvy.plugin.webhooks.github.shared;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.JsonFieldName;

@DTO
public interface PullRequest {

    public String HTML_URL_FIELD  = "html_url";
    public String MERGED_BY_FIELD = "merged_by";

    /**
     * Get pull request id.
     *
     * @return {@link String} id
     */
    String getId();

    void setId(String id);

    PullRequest withId(String id);

    /**
     * Get pull request URL.
     *
     * @return {@link String} url
     */
    String getUrl();

    void setUrl(String url);

    PullRequest withUrl(String url);

    /**
     * Get pull request html URL.
     *
     * @return {@link String} html_url
     */
    @JsonFieldName(HTML_URL_FIELD)
    String getHtmlUrl();

    void setHtmlUrl(String html_url);

    PullRequest withHtmlUrl(String html_url);

    /**
     * Get pull request number.
     *
     * @return {@link String} number
     */
    String getNumber();

    void setNumber(String number);

    PullRequest withNumber(String number);

    /**
     * Get pull request state.
     *
     * @return {@link String} state
     */
    String getState();

    void setState(String state);

    PullRequest withState(String state);

    /**
     * Get pull request head.
     *
     * @return {@link PullRequestBaseOrHead} head
     */
    PullRequestBaseOrHead getHead();

    void setHead(PullRequestBaseOrHead head);

    PullRequest withHead(PullRequestBaseOrHead head);

    /**
     * Get pull request base.
     *
     * @return {@link PullRequestBaseOrHead} base
     */
    PullRequestBaseOrHead getBase();

    void setBase(PullRequestBaseOrHead base);

    PullRequest withBase(PullRequestBaseOrHead base);

    /**
     * Tells if the pull request is merged.
     *
     * @return true iff the pull request is merged
     */
    boolean getMerged();

    void setMerged(boolean merged);

    PullRequest withMerged(boolean merged);
}
