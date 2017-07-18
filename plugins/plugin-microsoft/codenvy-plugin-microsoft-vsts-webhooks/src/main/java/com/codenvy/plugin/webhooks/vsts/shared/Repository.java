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
package com.codenvy.plugin.webhooks.vsts.shared;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.JsonFieldName;

@DTO
public interface Repository {

    public String LINKS_FIELD = "_links";

    /**
     * Get repository id.
     *
     * @return {@link String} id
     */
    String getId();

    void setId(final String id);

    Repository withId(final String id);

    /**
     * Get repository name.
     *
     * @return {@link String} name
     */
    String getName();

    void setName(final String name);

    Repository withName(final String name);

    /**
     * Get repository url.
     *
     * @return {@link String} url
     */
    String getUrl();

    void setUrl(final String url);

    Repository withUrl(final String url);

    /**
     * Get repository project.
     *
     * @return {@link Project} project
     */
    Project getProject();

    void setProject(final Project project);

    Repository withProject(final Project project);

    /**
     * Get repository defaultBranch.
     *
     * @return {@link String} defaultBranch
     */
    String getDefaultBranch();

    void setDefaultBranch(final String defaultBranch);

    Repository withDefaultBranch(final String defaultBranch);

    /**
     * Get repository remoteUrl.
     *
     * @return {@link String} remoteUrl
     */
    String getRemoteUrl();

    void setRemoteUrl(final String remoteUrl);

    Repository withRemoteUrl(final String remoteUrl);

    /**
     * Get resource links.
     *
     * @return {@link RepositoryLinks} links
     */
    @JsonFieldName(LINKS_FIELD)
    RepositoryLinks getLinks();

    void setLinks(final RepositoryLinks links);

    Repository withLinks(final RepositoryLinks links);
}
