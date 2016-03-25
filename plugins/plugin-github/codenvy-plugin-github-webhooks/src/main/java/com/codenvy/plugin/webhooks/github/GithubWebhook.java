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
package com.codenvy.plugin.webhooks.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Link factories to a GitHub webhook.
 * A factory may be linked to a webhook only if source.project.location = {@link repositoryUrl}
 *
 * @author Stephane Tournie
 */
public class GithubWebhook {

    private static final Logger LOG = LoggerFactory.getLogger(GithubWebhook.class);

    private final String      repositoryUrl;
    private final Set<String> factoriesIds;

    public GithubWebhook(String repositoryUrl, String... factoriesIds) {
        this.repositoryUrl = repositoryUrl;

        if (factoriesIds.length == 0) {
            LOG.warn("A webhook for repository {} cannot be set without factories", repositoryUrl);
        }
        this.factoriesIds = new HashSet<String>(Arrays.asList(factoriesIds));
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public Set<String> getFactoriesIds() {
        return factoriesIds;
    }
}
