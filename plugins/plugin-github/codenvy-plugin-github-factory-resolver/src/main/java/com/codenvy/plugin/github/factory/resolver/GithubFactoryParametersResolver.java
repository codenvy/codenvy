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
package com.codenvy.plugin.github.factory.resolver;

import com.codenvy.plugin.urlfactory.CreateFactoryParams;
import com.codenvy.plugin.urlfactory.ProjectConfigDtoMerger;
import com.codenvy.plugin.urlfactory.URLFactoryBuilder;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.server.FactoryParametersResolver;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Provides Factory Parameters resolver for github repositories.
 *
 * @author Florent Benoit
 */
public class GithubFactoryParametersResolver implements FactoryParametersResolver {

    /**
     * Parameter name.
     */
    protected static final String URL_PARAMETER_NAME = "url";

    /**
     * Parser which will allow to check validity of URLs and create objects.
     */
    @Inject
    private GithubUrlParser githubUrlParser;

    /**
     * Builder allowing to build objects from github URL.
     */
    @Inject
    private GithubSourceStorageBuilder githubSourceStorageBuilder;


    @Inject
    private URLFactoryBuilder urlFactoryBuilder;

    /**
     * ProjectDtoMerger
     */
    @Inject
    private ProjectConfigDtoMerger projectConfigDtoMerger;


    /**
     * Check if this resolver can be used with the given parameters.
     *
     * @param factoryParameters
     *         map of parameters dedicated to factories
     * @return true if it will be accepted by the resolver implementation or false if it is not accepted
     */
    @Override
    public boolean accept(@NotNull final Map<String, String> factoryParameters) {
        // Check if url parameter is a github URL
        return factoryParameters.containsKey(URL_PARAMETER_NAME) && githubUrlParser.isValid(factoryParameters.get(URL_PARAMETER_NAME));
    }

    /**
     * Create factory object based on provided parameters
     *
     * @param factoryParameters
     *         map containing factory data parameters provided through URL
     * @throws BadRequestException
     *         when data are invalid
     */
    @Override
    public FactoryDto createFactory(@NotNull final Map<String, String> factoryParameters) throws BadRequestException {

        // no need to check null value of url parameter as accept() method has performed the check
        final GithubUrl githubUrl = githubUrlParser.parse(factoryParameters.get("url"));

        // create factory from the following location if location exists, else create default factory
        FactoryDto factory = urlFactoryBuilder.createFactory(
                CreateFactoryParams.create().codenvyJsonFileLocation(githubUrl.codenvyFactoryJsonFileLocation()));

        // add workspace configuration if not defined
        if (factory.getWorkspace() == null) {
            factory.setWorkspace(urlFactoryBuilder.buildWorkspaceConfig(githubUrl.repository(),
                                                                        githubUrl.username(),
                                                                        githubUrl.codenvyDockerFileLocation()));
        }

        // Compute project configuration
        ProjectConfigDto projectConfigDto = newDto(ProjectConfigDto.class).withSource(githubSourceStorageBuilder.build(githubUrl))
                                                                          .withName(githubUrl.repository())
                                                                          .withType("blank")
                                                                          .withPath("/".concat(githubUrl.repository()));

        // apply merging operation from existing and computed settings
        return projectConfigDtoMerger.merge(factory, projectConfigDto);
    }

}
