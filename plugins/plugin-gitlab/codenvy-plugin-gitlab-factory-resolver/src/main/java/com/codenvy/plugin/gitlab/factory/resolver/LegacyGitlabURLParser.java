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
package com.codenvy.plugin.gitlab.factory.resolver;

import org.eclipse.che.plugin.urlfactory.URLChecker;

import javax.inject.Inject;

/**
 * Support for old dockerfile and factory file names;
 *
 * @author Max Shaposhnik
 */
public class LegacyGitlabURLParser extends GitlabURLParserImpl {

    private URLChecker urlChecker;

    @Inject
    public LegacyGitlabURLParser(URLChecker urlChecker) {
        this.urlChecker = urlChecker;
    }

    @Override
    public GitlabUrl parse(String url) {
        GitlabUrl gitlabUrl = super.parse(url);
        if (!urlChecker.exists(gitlabUrl.dockerFileLocation())) {
            gitlabUrl.withDockerfileFilename(".codenvy.dockerfile");
        }

        if (!urlChecker.exists(gitlabUrl.factoryJsonFileLocation())) {
            gitlabUrl.withFactoryFilename(".codenvy.json");
        }
        return gitlabUrl;
    }
}
