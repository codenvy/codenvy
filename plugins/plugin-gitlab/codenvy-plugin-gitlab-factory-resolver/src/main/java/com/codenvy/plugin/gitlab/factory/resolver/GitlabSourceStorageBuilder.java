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
public class GitlabSourceStorageBuilder {

    /**
     * Create SourceStorageDto DTO by using data of a gitlab url
     *
     * @param gitlabUrl
     *         an instance of {@link GitlabUrl}
     * @return newly created source storage DTO object
     */
    public SourceStorageDto build(GitlabUrl gitlabUrl) {
        // Create map for source storage dto
        Map<String, String> parameters = new HashMap<>();
        parameters.put("branch", gitlabUrl.getBranch());

        if (!Strings.isNullOrEmpty(gitlabUrl.getSubfolder())) {
            parameters.put("keepDir", gitlabUrl.getSubfolder());
        }
        return newDto(SourceStorageDto.class).withLocation(gitlabUrl.repositoryLocation()).withType("git").withParameters(parameters);
    }
}
