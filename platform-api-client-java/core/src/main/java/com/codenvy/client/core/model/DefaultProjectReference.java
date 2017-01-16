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

import com.codenvy.client.model.ProjectProblem;
import com.codenvy.client.model.ProjectReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * This class represents the project reference resource on Codenvy.
 *
 * @author Kevin Pollet
 */
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultProjectReference implements ProjectReference {
    private final String url;
    private final String visibility;
    private final String type;
    private final String workspaceId;
    private final String typeName;
    private final String name;
    private final String path;
    private final String description;
    private final String workspaceName;
    private final Date   modificationDate;
    private final Date   creationDate;
    private final String ideUrl;
    private final List<ProjectProblem> problems;


    /**
     * Constructs an instance of {@linkplain DefaultProjectReference}.
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
    public DefaultProjectReference(
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
            @JsonProperty("problems") List<DefaultProjectProblem> problems) {
        this.url = url;
        this.visibility = visibility;
        this.type = type;
        this.workspaceId = workspaceId;
        this.typeName = typeName;
        this.name = name;
        this.path = path;
        this.description = description;
        this.workspaceName = workspaceName;
        this.modificationDate = modificationDate != null ? new Date(modificationDate.getTime()) : null;
        this.creationDate = creationDate != null ? new Date(creationDate.getTime()) : null;
        this.ideUrl = ideUrl;

        this.problems = ImmutableList.copyOf(problems == null ? new ArrayList<ProjectProblem>() : problems);
    }


    @JsonProperty("url")
    @Override
    public String url() {
        return url;
    }

    @JsonProperty("visibility")
    @Override
    public String visibility() {
        return visibility;
    }

    @JsonProperty("type")
    @Override
    public String type() {
        return type;
    }

    @JsonProperty("workspaceId")
    @Override
    public String workspaceId() {
        return workspaceId;
    }

    /**
     * @return the problems of the given project
     */
    @JsonProperty("problems")
    @Override
    public List<ProjectProblem> getProblems() {
        return problems;
    }

    @JsonProperty("typeName")
    @Override
    public String typeName() {
        return typeName;
    }

    /**
     * @return path of the project
     */
    @JsonProperty("path")
    @Override
    public String path() {
        return path;
    }

    @JsonProperty("name")
    @Override
    public String name() {
        return name;
    }

    @JsonProperty("description")
    @Override
    public String description() {
        return description;
    }

    @JsonProperty("workspaceName")
    @Override
    public String workspaceName() {
        return workspaceName;
    }

    @JsonProperty("modificationDate")
    @Override
    public Date modificationDate() {
        return modificationDate;
    }

    @JsonProperty("creationDate")
    @Override
    public Date creationDate() {
        return creationDate;
    }

    @JsonProperty("ideUrl")
    @Override
    public String ideUrl() {
        return ideUrl;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((workspaceId == null) ? 0 : workspaceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultProjectReference other = (DefaultProjectReference)obj;
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }

        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (workspaceId == null) {
            if (other.workspaceId != null) {
                return false;
            }
        } else if (!workspaceId.equals(other.workspaceId)) {
            return false;
        }
        return true;
    }
}
