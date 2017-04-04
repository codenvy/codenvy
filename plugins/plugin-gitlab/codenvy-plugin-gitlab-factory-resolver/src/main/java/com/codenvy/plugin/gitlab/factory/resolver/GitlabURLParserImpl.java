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
