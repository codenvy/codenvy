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
package org.eclipse.che.ide.ext.bitbucket.server;

import static java.lang.String.format;

/**
 * Defines URL templates for BitBucket Server.
 *
 * @author Igor Vinokur
 */
public class BitbucketServerURLTemplates implements URLTemplates {

    private static final String REPOSITORY = "projects/~%s/repos/%s";

    private final String restUrl;

    BitbucketServerURLTemplates(String hostUrl) {
        this.restUrl = hostUrl + "/rest/api/latest/";
    }

    public String repositoryUrl(String owner, String repositorySlug) {
        return format(restUrl + REPOSITORY, owner, repositorySlug);
    }

    public String userUrl(String username) {
        return restUrl + "users/" + username;
    }

    public String pullrequestUrl(String owner, String repositorySlug) {
        return format(restUrl + REPOSITORY + "/pull-requests", owner, repositorySlug);
    }

    public String forksUrl(String owner, String repositorySlug) {
        return format(restUrl + REPOSITORY + "/forks", owner, repositorySlug);
    }

    public String forkRepositoryUrl(String owner, String repositorySlug) {
        return format(restUrl + REPOSITORY + "/pull-requests", owner, repositorySlug);
    }
}
