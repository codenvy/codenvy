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
package com.codenvy.plugin.webhooks;

import com.codenvy.plugin.webhooks.connectors.Connector;
import com.codenvy.plugin.webhooks.connectors.JenkinsConnector;

import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ForbiddenException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;

/**
 * Webhooks handler
 *
 * @author Stephane Tournie
 */
public abstract class BaseWebhookService extends Service {

    private static final Logger LOG                             = LoggerFactory.getLogger(BaseWebhookService.class);
    private static final String CONNECTORS_PROPERTIES_FILENAME  = "connectors.properties";
    private static final String CREDENTIALS_PROPERTIES_FILENAME = "credentials.properties";

    protected static final String FACTORY_URL_REL = "accept-named";

    private final AuthConnection authConnection;
    private final FactoryConnection factoryConnection;

    public BaseWebhookService(final AuthConnection authConnection, final FactoryConnection factoryConnection) {
        this.authConnection = authConnection;
        this.factoryConnection = factoryConnection;
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
    protected List<Factory> getFactoriesForRepositoryAndBranch(final Set<String> factoryIDs, final String headRepositoryUrl,
                                                               final String headBranch) throws ServerException {
        List<Factory> factories = new ArrayList<>();
        for (String factoryID : factoryIDs) {
            factories.add(factoryConnection.getFactory(factoryID));
        }

        return factories.stream()
                        .filter(f -> (f != null)
                                     && (f.getWorkspace().getProjects()
                                           .stream()
                                           .anyMatch(p -> isProjectMatching(p, headRepositoryUrl, headBranch))))
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
    protected Factory updateProjectInFactory(final Factory factory,
                                             final String headRepositoryUrl,
                                             final String headBranch,
                                             final String baseRepositoryUrl,
                                             final String headCommitId) throws ServerException {
        // Get projects in factory
        final List<ProjectConfigDto> factoryProjects = factory.getWorkspace().getProjects();

        factoryProjects.stream()
                       .filter(project -> isProjectMatching(project, headRepositoryUrl, headBranch))
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
    protected Factory updateProjectInFactory(final Factory factory, final String repositoryUrl, final String headBranch,
                                             final String headCommitId) throws ServerException {
        return updateProjectInFactory(factory, repositoryUrl, headBranch, repositoryUrl, headCommitId);
    }

    protected void updateFactory(final Factory factory) throws ServerException {
        final Factory persistedFactory = factoryConnection.updateFactory(factory);

        if (persistedFactory == null) {
            throw new ServerException(
                    String.format("Error during update of factory with id %s and name %s", factory.getId(), factory.getName()));
        }

        LOG.debug("Factory with id {} and name {} successfully updated", persistedFactory.getId(), persistedFactory.getName());
    }

    /**
     * Get all configured connectors
     *
     * Jenkins connector: [connector-name]=[connector-type],[factory-id],[jenkins-url],[jenkins-job-name]
     *
     * @param factoryId
     * @return the list of all connectors contained in CONNECTORS_PROPERTIES_FILENAME properties file
     */
    protected static List<Connector> getConnectors(String factoryId) throws ServerException {
        List<Connector> connectors = new ArrayList<>();
        Properties connectorsProperties = getProperties(CONNECTORS_PROPERTIES_FILENAME);
        Set<String> keySet = connectorsProperties.stringPropertyNames();
        keySet.stream()
              .filter(key -> factoryId.equals(connectorsProperties.getProperty(key).split(",")[1]))
              .forEach(key -> {
                  String value = connectorsProperties.getProperty(key);
                  String[] valueSplit = value.split(",");
                  switch (valueSplit[0]) {
                      case "jenkins":
                          JenkinsConnector jenkinsConnector = new JenkinsConnector(valueSplit[2], valueSplit[3]);
                          connectors.add(jenkinsConnector);
                          LOG.debug("new JenkinsConnector({}, {})", valueSplit[2], valueSplit[3]);
                          break;
                      default:
                          LOG.error("Unknown connector type {}", valueSplit[0]);
                          break;
                  }
              });
        return connectors;
    }

    /**
     * Get credentials
     *
     * @return the credentials contained in CREDENTIALS_PROPERTIES_FILENAME properties file
     * @throws ServerException
     */
    protected static Pair<String, String> getCredentials() throws ServerException {
        String[] credentials = new String[2];
        Properties credentialsProperties = getProperties(CREDENTIALS_PROPERTIES_FILENAME);
        Set<String> keySet = credentialsProperties.stringPropertyNames();
        keySet.forEach(key -> {
            String value = credentialsProperties.getProperty(key);
            switch (key) {
                case "username":
                    credentials[0] = value;
                    break;
                case "password":
                    credentials[1] = value;
                    break;
                default:
                    break;
            }
        });
        return Pair.of(credentials[0], credentials[1]);
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
    protected static void storeProperty(final String propertyKey, final String propertyValue, final String fileName) throws ServerException {
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
     * Whether or not a given project matches given repository and branch
     *
     * @param project
     *         the project to check
     * @param repositoryUrl
     *         the repo that the project source location has to match
     * @param branch
     *         the branch that the project has to match
     * @return the {@link java.util.function.Predicate} that matches relevant project(s)
     */
    private boolean isProjectMatching(final ProjectConfigDto project, final String repositoryUrl, final String branch) {

        if (isNullOrEmpty(repositoryUrl) || isNullOrEmpty(branch)) {
            return false;
        }

        final SourceStorageDto source = project.getSource();
        if (source == null) {
            return false;
        }

        final String projectType = source.getType();
        final String projectLocation = source.getLocation();
        final String projectBranch = source.getParameters().get("branch");

        if (isNullOrEmpty(projectType) || isNullOrEmpty(projectLocation)) {
            return false;
        }
        return (repositoryUrl.equals(projectLocation)
                || (repositoryUrl + ".git").equals(projectLocation))
               && ("master".equals(branch)
                   || (!isNullOrEmpty(projectBranch)
                       && branch.equals(projectBranch)));
    }

    /**
     * A user that only provides a token based on credentials configured in a property file
     */
    protected class TokenSubject implements Subject {

        private final Token token;

        public TokenSubject() throws ServerException {
            final Pair<String, String> credentials = getCredentials();
            token = authConnection.authenticateUser(credentials.first, credentials.second);
        }

        @Override
        public String getUserName() {
            return "token-user";
        }

        @Override
        public boolean isMemberOf(String role) {
            return false;
        }

        @Override
        public boolean hasPermission(String domain, String instance, String action) {
            return false;
        }

        @Override
        public void checkPermission(String domain, String instance, String action) throws ForbiddenException {
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
