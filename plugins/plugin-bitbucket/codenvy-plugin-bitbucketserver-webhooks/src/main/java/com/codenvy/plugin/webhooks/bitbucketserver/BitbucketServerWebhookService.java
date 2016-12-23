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
package com.codenvy.plugin.webhooks.bitbucketserver;

import com.codenvy.plugin.webhooks.AuthConnection;
import com.codenvy.plugin.webhooks.FactoryConnection;
import com.codenvy.plugin.webhooks.BaseWebhookService;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.Changeset;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.Project;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.PushEvent;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.RefChange;
import com.codenvy.plugin.webhooks.bitbucketserver.shared.Repository;
import com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;
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
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptySet;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/bitbucketserver-webhook")
public class BitbucketServerWebhookService extends BaseWebhookService {

    private static final Logger LOG = LoggerFactory.getLogger(BitbucketServerWebhookService.class);

    private static final String WEBHOOKS_PROPERTIES_FILENAME = "bitbucketserver-webhooks.properties";

    private final String bitbucketEndpoint;

    @Inject
    public BitbucketServerWebhookService(final AuthConnection authConnection,
                                         final FactoryConnection factoryConnection,
                                         @Named ("bitbucket.endpoint") String bitbucketEndpoint ) {
        super(authConnection, factoryConnection);
        this.bitbucketEndpoint = bitbucketEndpoint.endsWith("/") ? bitbucketEndpoint.substring(0, bitbucketEndpoint.length() - 1)
                                                                 : bitbucketEndpoint;
    }

    @POST
    @Consumes(APPLICATION_JSON)
    public Response handleWebhookEvent(@Context HttpServletRequest request) throws ServerException {
        EnvironmentContext.getCurrent().setSubject(new TokenSubject());

        Response response = Response.noContent().build();
        try (ServletInputStream inputStream = request.getInputStream()) {
            if (inputStream == null) {
                return response;
            }
            final PushEvent event = DtoFactory.getInstance().createDtoFromJson(inputStream, PushEvent.class);
            for (RefChange refChange : event.getRefChanges()) {
                Optional<Changeset> changeset = event.getChangesets()
                                                     .getValues()
                                                     .stream()
                                                     .filter(changeSet -> changeSet.getToCommit().getId().equals(refChange.getToHash()))
                                                     .findFirst();
                if (!changeset.isPresent()) {
                    continue;
                }
                String commitMessage = changeset.get().getToCommit().getMessage();
                if (commitMessage.startsWith("Merge pull request #")) {
                    handleMergeEvent(event, commitMessage);
                    continue;
                }
                String eventType = refChange.getType().toLowerCase();
                if ("update".equals(eventType) || "add".equals(eventType)) {
                    handlePushEvent(event, refChange.getRefId().substring(11));
                }
            }
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ServerException(e.getLocalizedMessage());
        }

        return response;
    }

    @VisibleForTesting
    void handlePushEvent(PushEvent event, String branch) throws ServerException {
        Repository repository = event.getRepository();
        Project project = repository.getProject();
        String cloneUrl = computeCloneUrl(project.getOwner().getName(), project.getKey(), repository.getName());

        for (FactoryDto factory : getFactoriesForRepositoryAndBranch(getFactoriesIDs(cloneUrl), cloneUrl, branch)) {
            Link factoryLink = factory.getLink(FACTORY_URL_REL);
            if (factoryLink == null) {
                LOG.warn("Factory " + factory.getId() + " do not contain mandatory \'" + FACTORY_URL_REL + "\' link");
                continue;
            }
            getConnectors(factory.getId()).forEach(connector -> connector.addFactoryLink(factoryLink.getHref()));
        }
    }

    @VisibleForTesting
    void handleMergeEvent(PushEvent event, String lastCommitMessage) throws ServerException {
        String source = lastCommitMessage.substring(lastCommitMessage.indexOf(" from ") + 6, lastCommitMessage.indexOf(" to "));
        String branch = source.contains(":") ? source.substring(source.indexOf(":") + 1) : source;
        String commitId = event.getRefChanges().get(0).getToHash();
        Project project = event.getRepository().getProject();
        String baseRepositoryName = event.getRepository().getName();
        String baseUrl = computeCloneUrl(source.contains(":") ? source.substring(1, source.indexOf("/")) : project.getOwner().getName(),
                                         source.contains(":") ? source.substring(0, source.indexOf("/")) : project.getKey(),
                                         source.contains(":") ? source.substring(source.indexOf("/") + 1) : baseRepositoryName);
        String headUrl = computeCloneUrl(project.getOwner().getName(), project.getKey(), baseRepositoryName);

        for (FactoryDto factory : getFactoriesForRepositoryAndBranch(getFactoriesIDs(baseUrl), baseUrl, branch)) {
            updateFactory(updateProjectInFactory(factory, headUrl, branch, baseUrl, commitId));
        }
    }

    private String computeCloneUrl(String owner, String projectKey, String repositoryName) {
        StringBuilder sb = new StringBuilder();
        sb.append(bitbucketEndpoint.substring(0, bitbucketEndpoint.indexOf("://") + 3))
          .append(owner)
          .append("@")
          .append(bitbucketEndpoint.substring(bitbucketEndpoint.indexOf("://") + 3))
          .append("/scm/")
          .append(projectKey)
          .append("/")
          .append(repositoryName)
          .append(".git");

        return sb.toString().toLowerCase();
    }

    private Set<String> getFactoriesIDs(final String repositoryUrl) throws ServerException {
        Optional<BitbucketServerWebhook> optional = getConfiguredWebhooks().stream()
                                                                           .filter(webhook -> webhook.getRepositoryCloneUrl()
                                                                                                     .equals(repositoryUrl))
                                                                           .findFirst();
        if (optional.isPresent()) {
            return optional.get().getFactoriesIds();
        } else {
            return emptySet();
        }
    }

    private static List<BitbucketServerWebhook> getConfiguredWebhooks() throws ServerException {
        Properties webhooksProperties = getProperties(WEBHOOKS_PROPERTIES_FILENAME);
        return webhooksProperties.stringPropertyNames()
                                 .stream()
                                 .filter(key -> {
                                     String value = webhooksProperties.getProperty(key);
                                     if (!isNullOrEmpty(value)) {
                                         String[] valueSplit = value.split(",");
                                         return valueSplit.length == 3 && valueSplit[0].equals("bitbucketserver");
                                     }
                                     return false;
                                 })
                                 .map(key -> {
                                     String[] valueSplit = webhooksProperties.getProperty(key).split(",");
                                     String[] factoriesIDs = valueSplit[2].split(";");
                                     return new BitbucketServerWebhook(valueSplit[1], factoriesIDs);
                                 })
                                 .collect(Collectors.toList());
    }
}
