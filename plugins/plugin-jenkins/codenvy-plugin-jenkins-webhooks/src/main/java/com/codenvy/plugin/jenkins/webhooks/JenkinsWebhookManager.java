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
package com.codenvy.plugin.jenkins.webhooks;

import com.codenvy.plugin.jenkins.webhooks.shared.JenkinsEventDto;
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
    public JenkinsWebhookManager(FactoryManager factoryManager,
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
     *         {@link JenkinsEventDto} object that contains information about failed Jenkins build
     * @throws ServerException
     *         when error occurs during handling failed job event
     */
    public void handleFailedJobEvent(JenkinsEventDto jenkinsEvent) throws ServerException {
        JenkinsConnector jenkinsConnector = jenkinsConnectorFactory.create(jenkinsEvent.getJenkinsUrl(), jenkinsEvent.getJobName())
                                                                   .updateUrlWithCredentials();
        try {
            String commitId = jenkinsConnector.getCommitId(jenkinsEvent.getBuildId());
            String repositoryUrl = jenkinsEvent.getRepositoryUrl();
            Optional<Factory> existingFailedFactory = findExistingFailedFactory(repositoryUrl, commitId);
            Factory failedFactory = existingFailedFactory.isPresent() ? existingFailedFactory.get() :
                                    createFailedFactory(jenkinsConnector.getFactoryId(),
                                                        repositoryUrl,
                                                        commitId);
            jenkinsConnector.addFailedBuildFactoryLink(baseUrl.substring(0, baseUrl.indexOf("/api")) + "/f?id=" + failedFactory.getId());
        } catch (IOException | NotFoundException | ConflictException e) {
            LOG.warn(e.getMessage());
            throw new ServerException(e.getMessage());
        }
    }

    private Optional<Factory> findExistingFailedFactory(String repositoryUrl, String commitId) throws NotFoundException, ServerException {
        return getUserFactories().stream()
                                 .filter(f -> f.getWorkspace()
                                               .getProjects()
                                               .stream()
                                               .anyMatch(project -> repositoryUrl.equals(project.getSource().getLocation()) &&
                                                                    commitId.equals(project.getSource()
                                                                                           .getParameters()
                                                                                           .get("commitId"))))
                                 .findAny();
    }

    private List<Factory> getUserFactories() throws NotFoundException, ServerException {
        List<Factory> factories = new ArrayList<>();
        List<Factory> factoriesPage;
        String userId = userManager.getByName(username).getId();
        int skipCount = 0;
        do {
            factoriesPage = factoryManager.getByAttribute(30, skipCount, singletonList(Pair.of("creator.userId", userId)));
            factories.addAll(factoriesPage);
            skipCount += factoriesPage.size();
        } while (factoriesPage.size() == 30);
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
