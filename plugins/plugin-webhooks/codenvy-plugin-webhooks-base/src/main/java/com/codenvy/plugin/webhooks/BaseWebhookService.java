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
package com.codenvy.plugin.webhooks;

import com.codenvy.plugin.webhooks.connectors.Connector;
import com.codenvy.plugin.webhooks.connectors.JenkinsConnector;

import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.inject.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static com.codenvy.plugin.webhooks.CloneUrlMatcher.DEFAULT_CLONE_URL_MATCHER;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * Webhooks handler
 *
 * @author Stephane Tournie
 */
public abstract class BaseWebhookService extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(BaseWebhookService.class);

    private static final String JENKINS_CONNECTOR_PREFIX_PATTERN    = "env.CODENVY_JENKINS_CONNECTOR_.+";
    private static final String JENKINS_CONNECTOR_URL_SUFFIX        = "_URL";
    private static final String JENKINS_CONNECTOR_FACTORY_ID_SUFFIX = "_FACTORY_ID";
    private static final String JENKINS_CONNECTOR_JOB_NAME_SUFFIX   = "_JOB_NAME";

    protected static final String FACTORY_URL_REL = "accept-named";

    private final AuthConnection          authConnection;
    private final FactoryConnection       factoryConnection;
    private final ConfigurationProperties configurationProperties;
    private final String                  username;
    private final String                  password;

    public BaseWebhookService(final AuthConnection authConnection,
                              final FactoryConnection factoryConnection,
                              ConfigurationProperties configurationProperties,
                              String username,
                              String password) {
        this.authConnection = authConnection;
        this.factoryConnection = factoryConnection;
        this.configurationProperties = configurationProperties;
        this.username = username;
        this.password = password;
    }

    /**
     * Get factories that contain a project for given repository and branch
     *
     * @param factoryIDs
     *         the set of id's of factories to check
     * @param headRepositoryUrl
     *         repository that factory project must match
     * @param headBranch
     *         branch that factory project must match
     * @return list of factories that contain a project for given repository and branch
     */
    protected List<FactoryDto> getFactoriesForRepositoryAndBranch(final Set<String> factoryIDs, final String headRepositoryUrl,
                                                                  final String headBranch,
                                                                  final CloneUrlMatcher matcher) throws ServerException {
        List<FactoryDto> factories = new ArrayList<>();
        for (String factoryID : factoryIDs) {
            factories.add(factoryConnection.getFactory(factoryID));
        }

        return factories.stream()
                        .filter(f -> (f != null)
                                     && (f.getWorkspace().getProjects()
                                          .stream()
                                          .anyMatch(p -> matcher.isCloneUrlMatching(p, headRepositoryUrl, headBranch))))
                        .collect(toList());
    }

    /**
     * Update project matching given predicate in given factory
     *
     * @param factory
     *         the factory to search for projects
     * @param headRepositoryUrl
     *         the URL of the repository that a project into the factory is configured with
     * @param headBranch
     *         the name of the branch that a project into the factory is configured with
     * @param baseRepositoryUrl
     *         the repository URL to set as source location for matching project in factory
     * @param headCommitId
     *         the commitId to set as 'commitId' parameter for matching project in factory
     * @return the project that matches the predicate given in argument
     * @throws ServerException
     */
    protected FactoryDto updateProjectInFactory(final FactoryDto factory,
                                                final String headRepositoryUrl,
                                                final String headBranch,
                                                final String baseRepositoryUrl,
                                                final String headCommitId,
                                                final CloneUrlMatcher matcher) throws ServerException {
        // Get projects in factory
        final List<ProjectConfigDto> factoryProjects = factory.getWorkspace().getProjects();

        factoryProjects.stream()
                       .filter(project -> matcher.isCloneUrlMatching(project, headRepositoryUrl, headBranch))
                       .forEach(project -> {
                           // Update repository and commitId
                           final SourceStorageDto source = project.getSource();
                           final Map<String, String> projectParams = source.getParameters();
                           source.setLocation(baseRepositoryUrl);
                           projectParams.put("commitId", headCommitId);

                           // Clean branch parameter if exist
                           projectParams.remove("branch");

                           source.setParameters(projectParams);
                       });

        return factory;
    }

    /**
     * Update project matching given predicate in given factory
     *
     * @param factory
     *         the factory to search for projects
     * @param repositoryUrl
     *         the repository URL that a project into the factory is configured with
     * @param headBranch
     *         the name of the branch that a project into the factory is configured with
     * @param headCommitId
     *         the commitId to set as 'commitId' parameter for matching project in factory
     * @return the project that matches the predicate given in argument
     * @throws ServerException
     */
    protected FactoryDto updateProjectInFactory(final FactoryDto factory, final String repositoryUrl, final String headBranch,
                                                final String headCommitId) throws ServerException {
        return updateProjectInFactory(factory, repositoryUrl, headBranch, repositoryUrl, headCommitId, DEFAULT_CLONE_URL_MATCHER);
    }

    protected void updateFactory(final FactoryDto factory) throws ServerException {
        final FactoryDto persistedFactory = factoryConnection.updateFactory(factory);

        if (persistedFactory == null) {
            throw new ServerException(
                    format("Error during update of factory with id %s and name %s", factory.getId(), factory.getName()));
        }

        LOG.debug("Factory with id {} and name {} successfully updated", persistedFactory.getId(), persistedFactory.getName());
    }

    /**
     * Get all configured connectors
     *
     * @param factoryId
     * @return the list of all configured connectors
     */
    protected List<Connector> getConnectors(String factoryId) throws ServerException {

        Map<String, String> properties = configurationProperties.getProperties(JENKINS_CONNECTOR_PREFIX_PATTERN);

        Set<String> connectors = properties.entrySet()
                                           .stream()
                                           .filter(entry -> entry.getValue().equals(factoryId))
                                           .map(entry -> entry.getKey()
                                                              .substring(0,
                                                                         entry.getKey().lastIndexOf(JENKINS_CONNECTOR_FACTORY_ID_SUFFIX)))
                                           .collect(Collectors.toSet());

        if (connectors.isEmpty()) {
            LOG.error("No connectors was registered for factory {}", factoryId);
        }

        return connectors.stream()
                         .map(connector -> createJenkinsConnector(properties, connector))
                         .collect(toList());
    }

    private JenkinsConnector createJenkinsConnector(Map<String, String> properties, String connector) {
        String url = properties.get(connector + JENKINS_CONNECTOR_URL_SUFFIX);
        String jobName = properties.get(connector + JENKINS_CONNECTOR_JOB_NAME_SUFFIX);

        checkArgument(!isNullOrEmpty(url) && !isNullOrEmpty(jobName),
                      format("No repository url or job name was not registered for jenkins connector '%s'", connector));

        return new JenkinsConnector(properties.get(connector + JENKINS_CONNECTOR_URL_SUFFIX),
                                    properties.get(connector + JENKINS_CONNECTOR_JOB_NAME_SUFFIX));
    }

    /**
     * Get all properties contained in a given file
     *
     * @param fileName
     *         the name of the properties file
     * @return the {@link Properties} contained in the given file or null if the file contains no properties
     * @throws ServerException
     */
    protected static Properties getProperties(final String fileName) throws ServerException {
        String filePath = Paths.get(fileName).toAbsolutePath().toString();
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(filePath)) {
            properties.load(in);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ServerException(e.getLocalizedMessage());
        }
        return properties;
    }

    /**
     * Store given key/value in given file
     *
     * @param propertyKey
     *         the key of the property to store
     * @param propertyValue
     *         the value of the property to store
     * @param fileName
     *         the name of the properties file
     * @throws ServerException
     */
    protected static void storeProperty(final String propertyKey, final String propertyValue, final String fileName)
            throws ServerException {
        String filePath = Paths.get(fileName).toAbsolutePath().toString();
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(filePath)) {
            properties.load(in);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ServerException(e.getLocalizedMessage());
        }
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            properties.setProperty(propertyKey, propertyValue);
            properties.store(out, null);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            throw new ServerException(e.getLocalizedMessage());
        }
    }

    /**
     * A user that only provides a token based on configured credentials
     */
    protected class TokenSubject implements Subject {

        private final Token token;

        public TokenSubject() throws ServerException {
            token = authConnection.authenticateUser(username, password);
        }

        @Override
        public String getUserName() {
            return "token-user";
        }

        @Override
        public boolean hasPermission(String domain, String instance, String action) {
            return false;
        }

        @Override
        public void checkPermission(String domain, String instance, String action) throws ForbiddenException {
            throw new ForbiddenException("User is not authorized to perform this operation");
        }

        @Override
        public String getToken() {
            return token.getValue();
        }

        @Override
        public String getUserId() {
            return "0000-00-0000";
        }

        @Override
        public boolean isTemporary() {
            return false;
        }
    }
}
