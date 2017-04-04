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
package com.codenvy.plugin.pullrequest.client;

import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.HostingServiceTemplates;

/**
 * Templates for GitHub constants.
 *
 * @author Kevin Pollet
 */
public interface BitBucketTemplates extends HostingServiceTemplates {
    @DefaultMessage("git@bitbucket.org:{0}/{1}.git")
    String sshUrlTemplate(String username, String repository);

    @DefaultMessage("https://bitbucket.org/{0}/{1}.git")
    String httpUrlTemplate(String username, String repository);

    @DefaultMessage("https://bitbucket.org/{0}/{1}/pull-request/{2}")
    String pullRequestUrlTemplate(String username, String repository, String pullRequestNumber);

    @DefaultMessage("[![Review]({0}//{1}/factory/resources/codenvy-review.svg)]({2})")
    String formattedReviewFactoryUrlTemplate(String protocol, String host, String reviewFactoryUrl);
}
