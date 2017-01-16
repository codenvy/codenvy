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

import com.codenvy.client.core.model.factory.DefaultFactoryCreator;
import com.codenvy.client.core.model.factory.DefaultFactoryProject;
import com.codenvy.client.model.Factory;
import com.codenvy.client.model.Link;
import com.codenvy.client.model.factory.FactoryCreator;
import com.codenvy.client.model.factory.FactoryProject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the factory resource on Codenvy.
 *
 * @author Florent Benoit
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultFactory implements Factory {

    /**
     * Name of the factory.
     */
    private String name;

    /**
     * Version.
     */
    private String version;

    /**
     * Links
     */
    private final List<Link> links;

    /**
     * Creator
     */
    private FactoryCreator factoryCreator;

    /**
     * Creator
     */
    private FactoryProject factoryProject;

    /**
     * Constructs an instance of {@linkplain com.codenvy.client.model.Factory}.
     *
     */
    @JsonCreator
    public DefaultFactory(@JsonProperty("links") List<DefaultLink> links, @JsonProperty("v") String version,
                          @JsonProperty("name") String name, @JsonProperty("creator") DefaultFactoryCreator factoryCreator, @JsonProperty("project") DefaultFactoryProject factoryProject) {
        this.links = ImmutableList.copyOf(links == null ? new ArrayList<Link>() : links);
        this.version = version;
        this.factoryCreator = factoryCreator;
        this.factoryProject = factoryProject;
    }

    @JsonProperty("v")
    @Override
    public String getV() {
        return version;
    }

    @JsonProperty("links")
    @Override
    public List<Link> getLinks() {
        return links;
    }

    @Override
    public Factory withWarnonclose(boolean warnonclose) {
        return null;
    }

    @Override
    public String getKeepdirectory() {
        return null;
    }


    /**
     * @return the creator
     */
    @Override
    public FactoryCreator creator() {
        return factoryCreator;
    }

    /**
     * @return the project
     */
    @Override
    public FactoryProject project() {
        return factoryProject;
    }

}
