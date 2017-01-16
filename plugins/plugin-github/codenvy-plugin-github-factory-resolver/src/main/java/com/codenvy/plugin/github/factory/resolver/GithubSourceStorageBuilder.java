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

import com.google.common.base.Strings;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Create {@link ProjectConfigDto} object from objects
 *
 * @author Florent Benoit
 */
public class GithubSourceStorageBuilder {

    /**
     * Create SourceStorageDto DTO by using data of a github url
     *
     * @param githubUrl
     *         an instance of {@link GithubUrl}
     * @return newly created source storage DTO object
     */
    public SourceStorageDto build(GithubUrl githubUrl) {
        // Create map for source storage dto
        Map<String, String> parameters = new HashMap<>(2);
        parameters.put("branch", githubUrl.branch());

        if (!Strings.isNullOrEmpty(githubUrl.subfolder())) {
            parameters.put("keepDir", githubUrl.subfolder());
        }
        return newDto(SourceStorageDto.class).withLocation(githubUrl.repositoryLocation()).withType("git").withParameters(parameters);
    }
}
