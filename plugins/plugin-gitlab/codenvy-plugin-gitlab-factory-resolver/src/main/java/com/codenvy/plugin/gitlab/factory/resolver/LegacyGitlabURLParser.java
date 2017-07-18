/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
