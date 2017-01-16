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

import com.codenvy.client.model.Workspace;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class represents the workspace resource on Codenvy.
 *
 * @author Kevin Pollet
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultWorkspace implements Workspace {
    private final String  id;
    private final String  name;
    private final String  organizationId;
    private final boolean temporary;

    /**
     * Constructs an instance of {@linkplain com.codenvy.client.model.Workspace}.
     *
     * @param id
     *         the workspace reference id.
     * @param name
     *         the workspace reference name.
     * @param organizationId
     *         the workspace organization.
     * @param temporary
     *         true if the workspace is temporary
     * @throws NullPointerException
     *         if name parameter is {@code null}.
     */
    @JsonCreator
    public DefaultWorkspace(@JsonProperty("id") String id, @JsonProperty("name") String name,
                                     @JsonProperty("organizationId") String organizationId,
                                     @JsonProperty("temporary") boolean temporary) {
        checkNotNull(name);

        this.id = id;
        this.name = name;
        this.organizationId = organizationId;
        this.temporary = temporary;
    }


    @Override
    public String id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String organizationId() {
        return organizationId;
    }

    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((organizationId == null) ? 0 : organizationId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultWorkspace other = (DefaultWorkspace)obj;
        if (organizationId == null) {
            if (other.organizationId != null) {
                return false;
            }
        } else if (!organizationId.equals(other.organizationId)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return other.temporary == temporary;

    }


}
