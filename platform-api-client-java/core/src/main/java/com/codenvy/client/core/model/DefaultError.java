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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The codenvy error envelope object model.
 *
 * @author Kevin Pollet
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultError implements com.codenvy.client.model.Error {

    private final String message;

    /**
     * Constructs an instance of {@link DefaultError}.
     *
     * @param message
     *         the error message.
     * @throws NullPointerException
     *         if message is {@code null}.
     */
    @JsonCreator
    public DefaultError(@JsonProperty("status") int status, @JsonProperty("message") String message) {
        checkNotNull(message);

        this.message = message;
    }

    @Override
    public String message() {
        return message;
    }
}
