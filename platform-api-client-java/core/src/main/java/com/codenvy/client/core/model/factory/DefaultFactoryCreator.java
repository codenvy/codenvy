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
package com.codenvy.client.core.model.factory;

import com.codenvy.client.model.factory.FactoryCreator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Factory creator
 * @author Florent Benoit
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultFactoryCreator implements FactoryCreator {

    private long created;

    @JsonCreator
    public DefaultFactoryCreator(@JsonProperty("created") long created) {
        this.created = created;
    }

    @Override
    public long created() {
        return created;
    }
}
