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
package org.eclipse.che.ide.ext.bitbucket.server;

/**
 * Url templates for Bitbucket rest API.
 *
 * @author Igor Vinokur
 */
public interface URLTemplates {

    String repositoryUrl(String owner, String repositorySlug);

    String userUrl();

    String pullRequestUrl(String owner, String repositorySlug);

    String updatePullRequestUrl(String owner, String repositorySlug, int pullrequestId);

    String forksUrl(String owner, String repositorySlug);

    String forkRepositoryUrl(String owner, String repositorySlug);
}
