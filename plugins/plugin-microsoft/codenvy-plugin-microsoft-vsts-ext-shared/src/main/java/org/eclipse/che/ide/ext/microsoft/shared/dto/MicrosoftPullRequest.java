/*
 *  [2012] - [2017] Codenvy, S.A.
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
package org.eclipse.che.ide.ext.microsoft.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Mihail Kuznyetsov
 */
@DTO
public interface MicrosoftPullRequest {

    Integer getPullRequestId();

    void setPullRequestId(Integer pullRequestId);

    MicrosoftPullRequest withPullRequestId(Integer pullRequestId);

    MicrosoftRepository getRepository();

    void setRepository(MicrosoftRepository repository);

    MicrosoftPullRequest withRepository(MicrosoftRepository repository);

    String getStatus();

    void setStatus(String status);

    MicrosoftPullRequest withStatus(String status);

    String getSourceRefName();

    void setSourceRefName(String sourceRefName);

    MicrosoftPullRequest withSourceRefName(String sourceRefName);

    String getTargetRefName();

    void setTargetRefName(String targetRefName);

    MicrosoftPullRequest withTargetRefName(String targetRefName);

    String getUrl();

    void setUrl(String url);

    MicrosoftPullRequest withUrl(String url);

    String getHtmlUrl();

    void setHtmlUrl(String htmlUrl);

    MicrosoftPullRequest withHtmlUrl(String htmlUrl);

    String getDescription();

    void setDescription(String description);

    MicrosoftPullRequest withDescription(String description);
}
