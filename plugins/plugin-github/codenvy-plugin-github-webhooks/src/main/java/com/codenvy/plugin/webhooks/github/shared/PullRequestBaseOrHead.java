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

@DTO
public interface PullRequestBaseOrHead {
    /**
     * Get pull request head (or base) label.
     *
     * @return {@link String} label
     */
    String getLabel();

    void setLabel(String label);

    PullRequestBaseOrHead withLabel(String label);

    /**
     * Get pull request head (or base) ref.
     *
     * @return {@link String} ref
     */
    String getRef();

    void setRef(String ref);

    PullRequestBaseOrHead withRef(String ref);

    /**
     * Get pull request head (or base) sha.
     *
     * @return {@link String} sha
     */
    String getSha();

    void setSha(String sha);

    PullRequestBaseOrHead withSha(String sha);

    /**
     * Get pull request head (or base) repo.
     *
     * @return {@link Repository} repo
     */
    Repository getRepo();

    void setRepo(Repository repo);

    PullRequestBaseOrHead withRepo(Repository repo);
}
