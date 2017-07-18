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

import javax.validation.constraints.NotNull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser of String gitlab URLs and provide {@link GitlabUrl} objects.
 *
 * @author Florent Benoit
 */
public class GitlabURLParserImpl implements  GitlabURLParser {

    /**
     * Regexp to find repository details (repository name, project name and branch and subfolder)
     * Examples of valid URLs are in the test class.
     */
    protected static final Pattern
            GITLAB_PATTERN = Pattern.compile(
            "^(?:http)(?:s)?(?:\\:\\/\\/)gitlab.com/(?<repoUser>[^/]++)/(?<repoName>[^/]++)(?:/tree/(?<branchName>[^/]++)(?:/(?<subFolder>.*))?)?$");


    @Override
    public boolean isValid(@NotNull String url) {
        return GITLAB_PATTERN.matcher(url).matches();
    }

    @Override
    public GitlabUrl parse(String url) {
        // Apply github url to the regexp
        Matcher matcher = GITLAB_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format(
                    "The given github url %s is not a valid URL github url. It should start with https://gitlab.com/<user>/<repo>",
                    url));
        }

        return new GitlabUrl().withUsername(matcher.group("repoUser"))
                              .withRepository(matcher.group("repoName"))
                              .withBranch(matcher.group("branchName"))
                              .withSubfolder(matcher.group("subFolder"))
                              .withDockerfileFilename(".factory.dockerfile")
                              .withFactoryFilename(".factory.json");

    }
}
