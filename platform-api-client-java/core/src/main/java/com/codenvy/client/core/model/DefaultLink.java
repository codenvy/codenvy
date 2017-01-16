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

import com.codenvy.client.model.Link;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The link model class.
 *
 * @author Kevin Pollet
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultLink implements Link {
    public static final String WEB_LINK_REL_ATTRIBUTE_VALUE      = "web url";
    public static final String DOWNLOAD_LINK_REL_ATTRIBUTE_VALUE = "download result";

    private final String href;
    private final String rel;
    private final String produces;
    private final String consumes;
    private final String method;

    @JsonCreator
    public DefaultLink(@JsonProperty("href") String href,
                       @JsonProperty("rel") String rel,
                       @JsonProperty("produces") String produces,
                       @JsonProperty("consumes") String consumes,
                       @JsonProperty("method") String method) {

        this.href = href;
        this.rel = rel;
        this.produces = produces;
        this.consumes = consumes;
        this.method = method;
    }

    @Override
    public String toString() {
        return "Link [href=" + href + ", rel=" + rel + ", produces=" + produces + ", consumes=" + consumes + ", method=" + method + "]";
    }

    @Override
    public String href() {
        return href;
    }

    @Override
    public String rel() {
        return rel;
    }

    @Override
    public String produces() {
        return produces;
    }

    @Override
    public String consumes() {
        return consumes;
    }

    @Override
    public String method() {
        return method;
    }
}
