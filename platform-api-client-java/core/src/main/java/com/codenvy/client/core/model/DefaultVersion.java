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

import com.codenvy.client.model.Version;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class represents the version resource on Codenvy.
 *
 * @author Florent Benoit
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultVersion implements Version {
    private final String specificationVendor;
    private final String implementationVendor;
    private final String specificationTitle;
    private final String specificationVersion;
    private final String implementationVersion;
    private final String scmRevision;


    /**
     * Constructs an instance of {@linkplain com.codenvy.client.core.model.DefaultVersion}.
     *
     * @param specificationVendor
     * @param implementationVendor
     * @param specificationTitle
     * @param specificationVersion
     * @param implementationVersion
     * @param scmRevision
     */
    @JsonCreator
    public DefaultVersion(@JsonProperty("specificationVendor") String specificationVendor,
                          @JsonProperty("implementationVendor") String implementationVendor,
                          @JsonProperty("specificationTitle") String specificationTitle,
                          @JsonProperty("specificationVersion") String specificationVersion,
                          @JsonProperty("implementationVersion") String implementationVersion,
                          @JsonProperty("scmRevision") String scmRevision) {
        checkNotNull(specificationVendor);
        checkNotNull(implementationVendor);
        checkNotNull(specificationTitle);
        checkNotNull(specificationVersion);
        checkNotNull(implementationVersion);
        checkNotNull(scmRevision);

        this.specificationVendor = specificationVendor;
        this.implementationVendor = implementationVendor;
        this.specificationTitle = specificationTitle;
        this.specificationVersion = specificationVersion;
        this.implementationVersion = implementationVersion;
        this.scmRevision = scmRevision;
    }

    /**
     * @return specification vendor
     */
    @Override
    public String specificationVendor() {
        return specificationVendor;
    }

    /**
     * @return implementation vendor
     */
    @Override
    public String implementationVendor() {
        return implementationVendor;
    }

    /**
     * @return specification title
     */
    @Override
    public String specificationTitle() {
        return specificationTitle;
    }

    /**
     * @return specification version
     */
    @Override
    public String specificationVersion() {
        return specificationVersion;
    }

    /**
     * @return implementation version
     */
    @Override
    public String implementationVersion() {
        return implementationVersion;
    }

    /**
     * @return scm revision
     */
    @Override
    public String scmRevision() {
        return scmRevision;
    }
}
