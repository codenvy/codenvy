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
package com.codenvy.plugin.jenkins.webhooks;

import com.codenvy.plugin.jenkins.webhooks.shared.JenkinsEvent;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.factory.Factory;
import org.eclipse.che.api.factory.server.FactoryManager;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;

/**
 * Facade for Jenkins webhooks related operations.
 *
 * @author Igor Vinokur
 */
public class JenkinsWebhookManager {

    private static final Logger LOG = LoggerFactory.getLogger(JenkinsWebhookManager.class);

    private final FactoryManager          factoryManager;
    private final UserManager             userManager;
    private final JenkinsConnectorFactory jenkinsConnectorFactory;
    private final String                  baseUrl;
    private final String                  username;

    @Inject
    JenkinsWebhookManager(FactoryManager factoryManager,
                          UserManager userManager,
                          JenkinsConnectorFactory jenkinsConnectorFactory,
                          @Named("che.api") String apiUrl,
                          @Named("integration.factory.owner.username") String username) {
        this.factoryManager = factoryManager;
        this.userManager = userManager;
        this.jenkinsConnectorFactory = jenkinsConnectorFactory;
        this.baseUrl = apiUrl;
        this.username = username;
    }

    /**
     * Handle build failed Jenkins event by performing next operations:
     * 1. Generate factory based on factory, configured in Jenkins connector properties, but with given commit as an endpoint.
     * If the factory for given commit is already exist from previous requests, this step will be skipped.
     * 2. Update Jenkins job description with build failed factory url.
     *
     * @param jenkinsEvent
     *         {@link JenkinsEvent} object that contains information about failed Jenkins build
     * @throws ServerException
     *         when error occurs during handling failed job event
     */
    void handleFailedJobEvent(JenkinsEvent jenkinsEvent) throws ServerException {
        JenkinsConnector jenkinsConnector = jenkinsConnectorFactory.create(jenkinsEvent.getJenkinsUrl(), jenkinsEvent.getJobName())
                                                                   .updateUrlWithCredentials();
        try {
            String commitId = jenkinsConnector.getCommitId(jenkinsEvent.getBuildId());
            String repositoryUrl = jenkinsEvent.getRepositoryUrl();
            Optional<Factory> existingFailedFactory = getExistingFailedFactoryIfPresent(repositoryUrl, commitId);
            Factory failedFactory = existingFailedFactory.isPresent() ? existingFailedFactory.get() :
                                    createFailedFactory(jenkinsConnector.getFactoryId(),
                                                        repositoryUrl,
                                                        commitId);
            jenkinsConnector.addFailedBuildFactoryLink(baseUrl.substring(0, baseUrl.indexOf("/api")) + "/f?id=" + failedFactory.getId());
        } catch (IOException | NotFoundException | ConflictException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ServerException(e.getLocalizedMessage());
        }
    }

    private Optional<Factory> getExistingFailedFactoryIfPresent(String repositoryUrl, String commitId) throws NotFoundException,
                                                                                                              ServerException {
        return getUserFactories().stream()
                                 .filter(f -> f.getWorkspace()
                                               .getProjects()
                                               .stream()
                                               .anyMatch(project -> repositoryUrl.equals(project.getSource().getLocation()) &&
                                                                    commitId.equals(project.getSource()
                                                                                           .getParameters()
                                                                                           .get("commitId"))))
                                 .findFirst();
    }

    private List<Factory> getUserFactories() throws NotFoundException, ServerException {
        List<Factory> factories = new ArrayList<>();
        List<Factory> request;
        int skipCount = 0;
        do {
            request = factoryManager.getByAttribute(30,
                                                    skipCount,
                                                    singletonList(Pair.of("creator.userId", userManager.getByName(username).getId())));
            factories.addAll(request);
            skipCount += request.size();
        } while (request.size() == 30);
        return factories;
    }

    private Factory createFailedFactory(String factoryId, String repositoryUrl, String commitId) throws ConflictException,
                                                                                                        ServerException,
                                                                                                        NotFoundException {
        FactoryImpl factory = (FactoryImpl)factoryManager.getById(factoryId);
        factory.setName(factory.getName() + "_" + commitId);
        factory.getWorkspace()
               .getProjects()
               .stream()
               .filter(project -> project.getSource().getLocation().equals(repositoryUrl))
               .forEach(project -> {
                   Map<String, String> parameters = project.getSource().getParameters();
                   parameters.remove("branch");
                   parameters.put("commitId", commitId);
               });
        return factoryManager.saveFactory(factory);
    }
}
