/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.plugin.gitlab.factory.resolver;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;

/**
 * Create {@link ProjectConfigDto} object from objects
 *
 * @author Florent Benoit
 */
public class GitlabSourceStorageBuilder {

  /**
   * Create SourceStorageDto DTO by using data of a gitlab url
   *
   * @param gitlabUrl an instance of {@link GitlabUrl}
   * @return newly created source storage DTO object
   */
  public SourceStorageDto build(GitlabUrl gitlabUrl) {
    // Create map for source storage dto
    Map<String, String> parameters = new HashMap<>();
    parameters.put("branch", gitlabUrl.getBranch());

    if (!Strings.isNullOrEmpty(gitlabUrl.getSubfolder())) {
      parameters.put("keepDir", gitlabUrl.getSubfolder());
    }
    return newDto(SourceStorageDto.class)
        .withLocation(gitlabUrl.repositoryLocation())
        .withType("git")
        .withParameters(parameters);
  }
}
