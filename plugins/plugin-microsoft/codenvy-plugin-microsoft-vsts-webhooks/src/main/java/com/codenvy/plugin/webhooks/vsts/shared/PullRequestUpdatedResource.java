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

import java.util.List;

@DTO
public interface PullRequestUpdatedResource {

    public String LINKS_FIELD = "_links";

    /**
     * Get resource repository.
     *
     * @return {@link String} repository
     */
    ResourceRepository getRepository();

    void setRepository(final ResourceRepository repository);

    PullRequestUpdatedResource withRepository(final ResourceRepository repository);

    /**
     * Get resource pull request id.
     *
     * @return {@link String} pullRequestId
     */
    String getPullRequestId();

    void setPullRequestId(final String pullRequestId);

    PullRequestUpdatedResource withPullRequestId(final String pullRequestId);

    /**
     * Get resource status.
     *
     * @return {@link String} status
     */
    String getStatus();

    void setStatus(final String status);

    PullRequestUpdatedResource withStatus(final String status);

    /**
     * Get resource created by.
     *
     * @return {@link String} createdBy
     */
    CreatedBy getCreatedBy();

    void setCreatedBy(final CreatedBy createdBy);

    PullRequestUpdatedResource withCreatedBy(final CreatedBy createdBy);

    /**
     * Get resource creation date.
     *
     * @return {@link String} status
     */
    String getCreationDate();

    void setCreationDate(final String creationDate);

    PullRequestUpdatedResource withCreationDate(final String creationDate);

    /**
     * Get resource closed date.
     *
     * @return {@link String} closedDate
     */
    String getClosedDate();

    void setClosedDate(final String closedDate);

    PullRequestUpdatedResource withClosedDate(final String closedDate);

    /**
     * Get resource title.
     *
     * @return {@link String} title
     */
    String getTitle();

    void setTitle(final String title);

    PullRequestUpdatedResource withTitle(final String title);

    /**
     * Get resource description.
     *
     * @return {@link String} description
     */
    String getDescription();

    void setDescription(final String description);

    PullRequestUpdatedResource withDescription(final String description);

    /**
     * Get resource source ref name.
     *
     * @return {@link String} sourceRefName
     */
    String getSourceRefName();

    void setSourceRefName(final String sourceRefName);

    PullRequestUpdatedResource withSourceRefName(final String sourceRefName);

    /**
     * Get resource target ref name.
     *
     * @return {@link String} targetRefName
     */
    String getTargetRefName();

    void setTargetRefName(final String targetRefName);

    PullRequestUpdatedResource withTargetRefName(final String targetRefName);

    /**
     * Get resource merge status.
     *
     * @return {@link String} mergeStatus
     */
    String getMergeStatus();

    void setMergeStatus(final String mergeStatus);

    PullRequestUpdatedResource withMergeStatus(final String mergeStatus);

    /**
     * Get resource merge id.
     *
     * @return {@link String} mergeId
     */
    String getMergeId();

    void setMergeId(final String mergeId);

    PullRequestUpdatedResource withMergeId(final String mergeId);

    /**
     * Get resource last merge source commit.
     *
     * @return {@link String} lastMergeSourceCommit
     */
    Commit getLastMergeSourceCommit();

    void setLastMergeSourceCommit(final Commit lastMergeSourceCommit);

    PullRequestUpdatedResource withLastMergeSourceCommit(final Commit lastMergeSourceCommit);

    /**
     * Get resource last merge target commit.
     *
     * @return {@link String} lastMergeTargetCommit
     */
    Commit getLastMergeTargetCommit();

    void setLastMergeTargetCommit(final Commit lastMergeTargetCommit);

    PullRequestUpdatedResource withLastMergeTargetCommit(final Commit lastMergeTargetCommit);

    /**
     * Get resource last merge commit.
     *
     * @return {@link String} lastMergeCommit
     */
    Commit getLastMergeCommit();

    void setLastMergeCommit(final Commit lastMergeCommit);

    PullRequestUpdatedResource withLastMergeCommit(final Commit lastMergeCommit);

    /**
     * Get resource reviewers.
     *
     * @return {@link String} reviewers
     */
    List<Reviewer> getReviewers();

    void setReviewers(final List<Reviewer> reviewers);

    PullRequestUpdatedResource withReviewers(final List<Reviewer> reviewers);

    /**
     * Get resource links.
     *
     * @return {@link PullRequestUpdatedResourceLinks} links
     */
    @JsonFieldName(LINKS_FIELD)
    PullRequestUpdatedResourceLinks getLinks();

    void setLinks(final PullRequestUpdatedResourceLinks links);

    PullRequestUpdatedResource withLinks(final PullRequestUpdatedResourceLinks links);

    /**
     * Get resource url.
     *
     * @return {@link String} url
     */
    String getUrl();

    void setUrl(final String url);

    PullRequestUpdatedResource withUrl(final String url);
}
