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
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Api(
        value = "/github-webhook",
        description = "GitHub webhooks handler"
)
@Path("/github-webhook")
public class GitHubWebhookService extends BaseWebhookService {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubWebhookService.class);

    private static final String GITHUB_WEBHOOKS_PROPERTIES_FILENAME = "github-webhooks.properties";
    private static final String GITHUB_REQUEST_HEADER               = "X-GitHub-Event";

    @Inject
    public GitHubWebhookService(final AuthConnection authConnection, final FactoryConnection factoryConnection) {
        super(authConnection, factoryConnection);
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
                            final PullRequestEvent PRevent = DtoFactory.getInstance().createDtoFromJson(inputStream, PullRequestEvent.class);
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
        final List<Factory> factories = getFactoriesForRepositoryAndBranch(factoriesIDs, contribRepositoryHtmlUrl, contribBranch);
        if (factories.isEmpty()) {
            throw new ServerException("No factory found for repository " + contribRepositoryHtmlUrl + " and branch " + contribBranch);
        }

        for (Factory f : factories) {
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
        final List<Factory> factories = getFactoriesForRepositoryAndBranch(factoriesIDs, prHeadRepositoryHtmlUrl, prHeadBranch);
        if (factories.isEmpty()) {
            throw new ServerException("No factory found for branch " + prHeadBranch);
        }

        for (Factory f : factories) {
            // Update project into the factory with given repository and branch
            final Factory updatedfactory =
                    updateProjectInFactory(f, prHeadRepositoryHtmlUrl, prHeadBranch, prBaseRepositoryHtmlUrl, prHeadCommitId);

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
     * @throws ServerException
     */
    private Set<String> getWebhookConfiguredFactoriesIDs(final String baseRepositoryHtmlUrl)
            throws ServerException {

        // Get webhook configured for given repository
        final Optional<GithubWebhook> webhook = getGitHubWebhook(baseRepositoryHtmlUrl);

        final GithubWebhook w = webhook.orElseThrow(
                () -> new ServerException("No webhook configured for repository " + baseRepositoryHtmlUrl));

        // Get factory id's listed into the webhook
        return w.getFactoriesIds();
    }

    /**
     * Get webhook configured for a given repository
     *
     * @param repositoryUrl
     *         the URL of the repository
     * @return the webhook configured for the repository or null if no webhook is configured for this repository
     * @throws ServerException
     */
    private Optional<GithubWebhook> getGitHubWebhook(String repositoryUrl) throws ServerException {
        List<GithubWebhook> webhooks = getGitHubWebhooks();
        GithubWebhook webhook = null;
        for (GithubWebhook w : webhooks) {
            String webhookRepositoryUrl = w.getRepositoryUrl();
            if (repositoryUrl.equals(webhookRepositoryUrl)) {
                webhook = w;
            }
        }
        return Optional.ofNullable(webhook);
    }

    /**
     * Get all configured webhooks
     *
     * GitHub webhook: [webhook-name]=[webhook-type],[repository-url],[factory-id];[factory-id];...;[factory-id]
     *
     * @return the list of all webhooks contained in GITHUB_WEBHOOKS_PROPERTIES_FILENAME properties fil
     */
    private static List<GithubWebhook> getGitHubWebhooks() throws ServerException {
        List<GithubWebhook> webhooks = new ArrayList<>();
        Properties webhooksProperties = getProperties(GITHUB_WEBHOOKS_PROPERTIES_FILENAME);
        Set<String> keySet = webhooksProperties.stringPropertyNames();
        keySet.stream().forEach(key -> {
            String value = webhooksProperties.getProperty(key);
            if (!isNullOrEmpty(value)) {
                String[] valueSplit = value.split(",");
                if (valueSplit.length == 3
                    && valueSplit[0].equals("github")) {
                    String[] factoriesIDs = valueSplit[2].split(";");
                    GithubWebhook githubWebhook = new GithubWebhook(valueSplit[1], factoriesIDs);
                    webhooks.add(githubWebhook);
                    LOG.debug("new GithubWebhook({})", value);
                }
            }
        });
        return webhooks;
    }
}
