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
 * Defines URL templates for hosted version of BitBucket.
 *
 * @author Igor Vinokur
 */
public class BitbucketURLTemplates implements URLTemplates {

    private static final String BITBUCKET_API_URL     = "https://api.bitbucket.org";
    private static final String BITBUCKET_2_0_API_URL = BITBUCKET_API_URL + "/2.0";
    private static final String BITBUCKET_1_0_API_URL = BITBUCKET_API_URL + "/1.0";

    @Override
    public String repositoryUrl(String owner, String repositorySlug) {
        return BITBUCKET_2_0_API_URL + "/repositories/" + owner + "/" + repositorySlug;
    }

    @Override
    public String userUrl() {
        return BITBUCKET_2_0_API_URL + "/user";
    }

    @Override
    public String pullrequestUrl(String owner, String repositorySlug) {
        return BITBUCKET_2_0_API_URL + "/repositories/" + owner + "/" + repositorySlug + "/pullrequests";
    }

    @Override
    public String updatePullrequestUrl(String owner, String repositorySlug, int pullrequestId) {
        return BITBUCKET_2_0_API_URL + "/repositories/" + owner + "/" + repositorySlug + "/pullrequests/" + pullrequestId;
    }

    @Override
    public String forksUrl(String owner, String repositorySlug) {
        return BITBUCKET_2_0_API_URL + "/repositories/" + owner + "/" + repositorySlug + "/forks";
    }

    @Override
    public String forkRepositoryUrl(String owner, String repositorySlug) {
        return BITBUCKET_1_0_API_URL + "/repositories/" + owner + "/" + repositorySlug + "/fork";
    }
}
