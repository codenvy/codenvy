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
package com.codenvy.plugin.urlfactory;

import com.google.common.base.Strings;

import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Handle the creation of some elements used inside a {@link Factory}
 *
 * @author Florent Benoit
 */
@Singleton
public class URLFactoryBuilder {

    /**
     * Default docker image (if repository has no dockerfile)
     */
    protected static final String DEFAULT_DOCKER_IMAGE = "codenvy/ubuntu_jdk8";

    /**
     * Default docker type (if repository has no dockerfile)
     */
    protected static final String MEMORY_LIMIT_BYTES  = Long.toString(2000L * 1024L * 1024L);
    protected static final String MACHINE_NAME        = "ws-machine";

    /**
     * Check if URL is existing or not
     */
    @Inject
    private URLChecker URLChecker;

    /**
     * Grab content of URLs
     */
    @Inject
    private URLFetcher URLFetcher;

    /**
     * Build a default factory using the provided json file or create default one
     *
     * @param createFactoryParams
     *         optional parameters
     * @return a factory
     */
    public Factory createFactory(CreateFactoryParams createFactoryParams) {

        // Check if there is factory json file inside the repository
        if (createFactoryParams != null && createFactoryParams.codenvyJsonFileLocation() != null) {
            String factoryJsonContent = URLFetcher.fetch(createFactoryParams.codenvyJsonFileLocation());
            if (!Strings.isNullOrEmpty(factoryJsonContent)) {
                return DtoFactory.getInstance().createDtoFromJson(factoryJsonContent, Factory.class);
            }
        }

        // else return a default factory
        return newDto(Factory.class).withV("4.0");
    }


    /**
     * Help to generate default workspace configuration
     *
     * @param environmentName
     *         the name of the environment to create
     * @param name
     *         the name of the workspace
     * @param dockerFileLocation
     *         the optional location for codenvy dockerfile to use
     * @return a workspace configuration
     */
    public WorkspaceConfigDto buildWorkspaceConfig(String environmentName,
                                                   String name,
                                                   String dockerFileLocation) {

        // if remote repository contains a codenvy docker file, use it
        // else use the default image.
        EnvironmentRecipeDto recipeDto;
        if (dockerFileLocation != null && URLChecker.exists(dockerFileLocation)) {
            recipeDto = newDto(EnvironmentRecipeDto.class).withLocation(dockerFileLocation)
                                                          .withType("dockerfile")
                                                          .withContentType("text/x-dockerfile");
        } else {
            recipeDto = newDto(EnvironmentRecipeDto.class).withLocation(DEFAULT_DOCKER_IMAGE)
                                                          .withType("dockerimage");
        }
        ExtendedMachineDto machine = newDto(ExtendedMachineDto.class).withAgents(singletonList("org.eclipse.che.ws-agent"))
                                                                     .withAttributes(singletonMap("memoryLimitBytes", MEMORY_LIMIT_BYTES));

        // setup environment
        EnvironmentDto environmentDto = newDto(EnvironmentDto.class).withRecipe(recipeDto)
                                                                    .withMachines(singletonMap(MACHINE_NAME, machine));

        // workspace configuration using the environment
        return newDto(WorkspaceConfigDto.class)
                .withDefaultEnv(environmentName)
                .withEnvironments(singletonMap(environmentName, environmentDto))
                .withName(name);
    }
}
