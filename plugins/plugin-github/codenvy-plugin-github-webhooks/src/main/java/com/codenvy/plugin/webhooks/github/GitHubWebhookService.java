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
package com.codenvy.plugin.webhooks.github;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.codenvy.plugin.webhooks.AuthConnection;
import com.codenvy.plugin.webhooks.FactoryConnection;
import com.codenvy.plugin.webhooks.BaseWebhookService;
import com.codenvy.plugin.webhooks.connectors.Connector;
import com.codenvy.plugin.webhooks.github.shared.PullRequestEvent;
import com.codenvy.plugin.webhooks.github.shared.PushEvent;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.inject.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.codenvy.plugin.webhooks.CloneUrlMatcher.DEFAULT_CLONE_URL_MATCHER;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(
        value = "/github-webhook",
        description = "GitHub webhooks handler"
)
@Path("/github-webhook")
public class GitHubWebhookService extends BaseWebhookService {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubWebhookService.class);

    private static final String GITHUB_REQUEST_HEADER             = "X-GitHub-Event";
    private static final String WEBHOOK_PROPERTY_PATTERN          = "env.CODENVY_GITHUB_WEBHOOK_.+";
    private static final String WEBHOOK_REPOSITORY_URL_SUFFIX     = "_REPOSITORY_URL";
    private static final String WEBHOOK_FACTORY_ID_SUFFIX_PATTERN = "_FACTORY.+_ID";

    private final ConfigurationProperties configurationProperties;

    @Inject
    public GitHubWebhookService(final AuthConnection authConnection,
                                final FactoryConnection factoryConnection,
                                final ConfigurationProperties configurationProperties,
                                @Named("integration.factory.owner.username") String username,
                                @Named("integration.factory.owner.password") String password) {
        super(authConnection, factoryConnection, configurationProperties, username, password);
        this.configurationProperties = configurationProperties;
    }

    @ApiOperation(value = "Handle GitHub webhook events",
                  response = Response.class)
    @ApiResponses({
                          @ApiResponse(code = 200, message = "OK"),
                          @ApiResponse(code = 202, message = "The request has been accepted for processing, but the processing has not been completed."),
                          @ApiResponse(code = 500, message = "Internal Server Error")
                  })
    @POST
    @Consumes(APPLICATION_JSON)
    public Response handleGithubWebhookEvent(@ApiParam(value = "New contribution", required = true)
                                             @Context HttpServletRequest request)
            throws ServerException {

        Response response = Response.ok().build();
        try (ServletInputStream inputStream = request.getInputStream()) {
            if (inputStream != null) {
                String githubHeader = request.getHeader(GITHUB_REQUEST_HEADER);
                if (!isNullOrEmpty(githubHeader)) {
                    switch (githubHeader) {
                        case "push":
                            final PushEvent pushEvent = DtoFactory.getInstance().createDtoFromJson(inputStream, PushEvent.class);
                            handlePushEvent(pushEvent);
                            break;
                        case "pull_request":
                            final PullRequestEvent PRevent =
                                    DtoFactory.getInstance().createDtoFromJson(inputStream, PullRequestEvent.class);
                            handlePullRequestEvent(PRevent);
                            break;
                        default:
                            response = Response.accepted(new GenericEntity<>(
                                    "GitHub message \'" + githubHeader + "\' received. It isn't intended to be processed.", String.class))
                                               .build();
                            break;
                    }
                }
            }
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ServerException(e.getLocalizedMessage());
        }

        return response;
    }

    /**
     * Handle GitHub {@link PushEvent}
     *
     * @param contribution
     *         the push event to handle
     * @return HTTP 200 response if event was processed successfully
     * HTTP 202 response if event was processed partially
     * @throws ServerException
     */
    private void handlePushEvent(PushEvent contribution) throws ServerException {
        LOG.debug("{}", contribution);

        // Set current Codenvy user
        EnvironmentContext.getCurrent().setSubject(new TokenSubject());

        // Get contribution data
        final String contribRepositoryHtmlUrl = contribution.getRepository().getHtmlUrl();
        final String[] contribRefSplit = contribution.getRef().split("/");
        final String contribBranch = contribRefSplit[contribRefSplit.length - 1];

        // Get factories id's that are configured in a webhook
        final Set<String> factoriesIDs = getWebhookConfiguredFactoriesIDs(contribRepositoryHtmlUrl);

        // Get factories that contain a project for given repository and branch
        final List<FactoryDto> factories = getFactoriesForRepositoryAndBranch(factoriesIDs,
                                                                              contribRepositoryHtmlUrl,
                                                                              contribBranch,
                                                                              DEFAULT_CLONE_URL_MATCHER);
        if (factories.isEmpty()) {
            throw new ServerException("No factory found for repository " + contribRepositoryHtmlUrl + " and branch " + contribBranch);
        }

        for (FactoryDto f : factories) {
            // Get 'open factory' URL
            final Link factoryLink = f.getLink(FACTORY_URL_REL);
            if (factoryLink == null) {
                throw new ServerException("Factory " + f.getId() + " do not contain mandatory \'" + FACTORY_URL_REL + "\' link");
            }

            // Get connectors configured for the factory
            final List<Connector> connectors = getConnectors(f.getId());

            // Add factory link within third-party services
            connectors.forEach(connector -> connector.addFactoryLink(factoryLink.getHref()));
        }
    }

    /**
     * Handle GitHub {@link PullRequestEvent}
     *
     * @param prEvent
     *         the pull request event to handle
     * @return HTTP 200 response if event was processed successfully
     * HTTP 202 response if event was processed partially
     * @throws ServerException
     */
    private void handlePullRequestEvent(PullRequestEvent prEvent) throws ServerException {
        LOG.debug("{}", prEvent);

        // Set current Codenvy user
        EnvironmentContext.getCurrent().setSubject(new TokenSubject());

        // Check that event indicates a successful merging
        final String action = prEvent.getAction();
        if (!"closed".equals(action)) {
            throw new ServerException(
                    "PullRequest Event action is " + action + ". " + this.getClass().getSimpleName() + " do not handle this one.");
        }
        final boolean isMerged = prEvent.getPullRequest().getMerged();
        if (!isMerged) {
            throw new ServerException("Pull Request was closed with unmerged commits !");
        }

        // Get head repository data
        final String prHeadRepositoryHtmlUrl = prEvent.getPullRequest().getHead().getRepo().getHtmlUrl();
        final String prHeadBranch = prEvent.getPullRequest().getHead().getRef();
        final String prHeadCommitId = prEvent.getPullRequest().getHead().getSha();

        // Get base repository data
        final String prBaseRepositoryHtmlUrl = prEvent.getPullRequest().getBase().getRepo().getHtmlUrl();

        // Get factories id's that are configured in a webhook
        final Set<String> factoriesIDs = getWebhookConfiguredFactoriesIDs(prBaseRepositoryHtmlUrl);

        // Get factories that contain a project for given repository and branch
        final List<FactoryDto> factories = getFactoriesForRepositoryAndBranch(factoriesIDs,
                                                                              prHeadRepositoryHtmlUrl,
                                                                              prHeadBranch,
                                                                              DEFAULT_CLONE_URL_MATCHER);
        if (factories.isEmpty()) {
            throw new ServerException("No factory found for branch " + prHeadBranch);
        }

        for (FactoryDto f : factories) {
            // Update project into the factory with given repository and branch
            final FactoryDto updatedfactory =
                    updateProjectInFactory(f,
                                           prHeadRepositoryHtmlUrl,
                                           prHeadBranch,
                                           prBaseRepositoryHtmlUrl,
                                           prHeadCommitId,
                                           DEFAULT_CLONE_URL_MATCHER);

            // Persist updated factory
            updateFactory(updatedfactory);

            // TODO Remove factory id from webhook
        }
    }

    /**
     * Get factories configured in a webhook for given base repository
     * and contain a project for given head repository and head branch
     *
     * @param baseRepositoryHtmlUrl
     *         the URL of the repository for which a webhook is configured
     * @return the factories configured in a webhook and that contain a project that matches given repo and branch
     */
    private Set<String> getWebhookConfiguredFactoriesIDs(final String baseRepositoryHtmlUrl) {
        Map<String, String> properties = configurationProperties.getProperties(WEBHOOK_PROPERTY_PATTERN);

        Set<String> webhooks = properties.entrySet()
                                         .stream()
                                         .filter(entry -> baseRepositoryHtmlUrl.equals(entry.getValue()))
                                         .map(entry -> entry.getKey()
                                                            .substring(0, entry.getKey().lastIndexOf(WEBHOOK_REPOSITORY_URL_SUFFIX)))
                                         .collect(toSet());

        if (webhooks.isEmpty()) {
            LOG.warn("No GitHub webhooks were registered for repository {}", baseRepositoryHtmlUrl);
        }

        return properties.entrySet()
                         .stream()
                         .filter(entry -> webhooks.stream()
                                                  .anyMatch(webhook -> entry.getKey().matches(webhook + WEBHOOK_FACTORY_ID_SUFFIX_PATTERN)))
                         .map(Entry::getValue)
                         .collect(toSet());
    }
}
