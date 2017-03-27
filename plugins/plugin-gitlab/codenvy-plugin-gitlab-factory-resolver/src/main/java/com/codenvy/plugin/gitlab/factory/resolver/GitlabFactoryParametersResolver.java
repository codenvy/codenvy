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

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.server.FactoryParametersResolver;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.plugin.urlfactory.ProjectConfigDtoMerger;
import org.eclipse.che.plugin.urlfactory.URLFactoryBuilder;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Provides Factory Parameters resolver for gitlab repositories.
 *
 * @author Florent Benoit
 */
public class GitlabFactoryParametersResolver implements FactoryParametersResolver {

    /**
     * Parameter name.
     */
    protected static final String URL_PARAMETER_NAME = "url";

    /**
     * Parser which will allow to check validity of URLs and create objects.
     */
    @Inject
    private GitlabURLParser gitlabUrlParser;

    /**
     * Builder allowing to build objects from gitlab URL.
     */
    @Inject
    private GitlabSourceStorageBuilder gitlabSourceStorageBuilder;


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
        return factoryParameters.containsKey(URL_PARAMETER_NAME) && gitlabUrlParser.isValid(factoryParameters.get(URL_PARAMETER_NAME));
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
        final GitlabUrl gitlabUrl = gitlabUrlParser.parse(factoryParameters.get("url"));

        // create factory from the following location if location exists, else create default factory
        FactoryDto factory = urlFactoryBuilder.createFactory(gitlabUrl.factoryJsonFileLocation());

        // add workspace configuration if not defined
        if (factory.getWorkspace() == null) {
            factory.setWorkspace(urlFactoryBuilder.buildWorkspaceConfig(gitlabUrl.getRepository(),
                                                                        gitlabUrl.getUsername(),
                                                                        gitlabUrl.dockerFileLocation()));
        }

        // Compute project configuration
        ProjectConfigDto projectConfigDto = newDto(ProjectConfigDto.class).withSource(gitlabSourceStorageBuilder.build(gitlabUrl))
                                                                          .withName(gitlabUrl.getRepository())
                                                                          .withType("blank")
                                                                          .withPath("/".concat(gitlabUrl.getRepository()));

        // apply merging operation from existing and computed settings
        return projectConfigDtoMerger.merge(factory, projectConfigDto);
    }

}
