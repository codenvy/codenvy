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
 * Templates for Bitbucket Server constants.
 *
 * @author Igor Vinokur
 */
public interface BitBucketServerTemplates extends HostingServiceTemplates {
    @DefaultMessage("{0}/{1}.git")
    String sshUrlTemplate(String path, String repository);

    @DefaultMessage("{0}/{1}.git")
    String httpUrlTemplate(String url, String repository);

    @DefaultMessage("{0}/repos/{1}/pull-requests/{2}")
    String pullRequestUrlTemplate(String url, String repository, String pullRequestNumber);

    @DefaultMessage("[![Review]({0}//{1}/factory/resources/factory-review.svg)]({2})")
    String formattedReviewFactoryUrlTemplate(String protocol, String host, String reviewFactoryUrl);
}
