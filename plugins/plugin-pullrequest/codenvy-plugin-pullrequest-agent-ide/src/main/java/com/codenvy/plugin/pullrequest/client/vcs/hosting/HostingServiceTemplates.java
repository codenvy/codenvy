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
package com.codenvy.plugin.pullrequest.client.vcs.hosting;

import com.google.gwt.i18n.client.Messages;

/**
 * Hosting service templates.
 *
 * @author Kevin Pollet
 */
public interface HostingServiceTemplates extends Messages {
    /**
     * The SSH URL to a repository.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @return the URL
     */
    String sshUrlTemplate(String username, String repository);

    /**
     * The HTTP URL to a repository.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @return the URL
     */
    String httpUrlTemplate(String username, String repository);

    /**
     * The URL to a pull request.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @param pullRequestNumber
     *         the pull request number.
     * @return the URL
     */
    String pullRequestUrlTemplate(String username, String repository, String pullRequestNumber);

    /**
     * The formatted version of the review factory url using the Hosting service markup language.
     *
     * @param protocol
     *         the protocol used http or https
     * @param host
     *         the host.
     * @param reviewFactoryUrl
     *         the review factory url.
     * @return the formatted version of the review factory url
     */
    String formattedReviewFactoryUrlTemplate(String protocol, String host, String reviewFactoryUrl);
}
