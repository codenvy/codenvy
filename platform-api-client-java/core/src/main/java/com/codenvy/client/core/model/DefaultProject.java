/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client.core.model;

import com.codenvy.client.core.model.project.DefaultBuildersDescription;
import com.codenvy.client.core.model.project.DefaultRunnersDescription;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.project.BuildersDescription;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * This class represents the project resource on Codenvy.
 *
 * @author Florent Benoit
 */
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultProject extends DefaultProjectReference implements Project {

    private final List<String>               permissions;
    private final Map<String, List<String>>  attributes;
    private final DefaultRunnersDescription  runnersDescription;
    private final DefaultBuildersDescription buildersDescription;

    /**
     * Constructs an instance of {@linkplain com.codenvy.client.core.model.DefaultProject}.
     *
     * @param url
     *         the project url.
     * @param visibility
     *         the project visibility (private or public).
     * @param type
     *         the project type id (e.g. spring, java, ...).
     * @param workspaceId
     *         the project workspace id.
     * @param typeName
     *         the project type name (e.g. Spring application, ...).
     * @param name
     *         the project name.
     * @param description
     *         the project description.
     * @param workspaceName
     *         the project workspace name.
     * @param modificationDate
     *         the project modification date.
     * @param creationDate
     *         the project creation date.
     * @param ideUrl
     *         the project ide url.
     */
    @JsonCreator
    public DefaultProject(
            @JsonProperty("url") String url,
            @JsonProperty("visibility") String visibility,
            @JsonProperty("type") String type,
            @JsonProperty("workspaceId") String workspaceId,
            @JsonProperty("typeName") String typeName,
            @JsonProperty("name") String name,
            @JsonProperty("path") String path,
            @JsonProperty("description") String description,
            @JsonProperty("workspaceName") String workspaceName,
            @JsonProperty("modificationDate") Date modificationDate,
            @JsonProperty("creationDate") Date creationDate,
            @JsonProperty("ideUrl") String ideUrl,
            @JsonProperty("problems") List<DefaultProjectProblem> problems,
            @JsonProperty("permissions") List<String> permissions,
            @JsonProperty("attributes") Map<String, List<String>> attributes,
            @JsonProperty("builders") DefaultBuildersDescription buildersDescription,
        @JsonProperty("runners") DefaultRunnersDescription runnersDescription) {
        super(url, visibility, type, workspaceId, typeName, name, path, description, workspaceName, modificationDate,
              creationDate, ideUrl, problems);

        this.permissions = ImmutableList.copyOf(permissions == null ? new ArrayList<String>() : permissions);
        this.attributes = ImmutableMap.copyOf(attributes == null ? new HashMap<String, List<String>>() : attributes);
        this.buildersDescription = buildersDescription;
        this.runnersDescription = runnersDescription;
    }

    @JsonProperty("permissions")
    @Override
    public List<String> permissions() {
        return permissions;
    }

    @JsonProperty("attributes")
    @Override
    public Map<String, List<String>> attributes() {
        return attributes;
    }

    /**
     * @return the description of the runners
     */
    @JsonProperty("runners")
    @Override
    public DefaultRunnersDescription runners() {
        return runnersDescription;
    }

    /**
     * @return the description of the builders
     */
    @JsonProperty("builders")
    @Override
    public BuildersDescription builders() {
        return buildersDescription;
    }


}
