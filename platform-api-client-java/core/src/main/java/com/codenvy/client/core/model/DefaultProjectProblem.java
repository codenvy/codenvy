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
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * @author Florent Benoit
 */
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultProjectProblem implements ProjectProblem {

    /**
     * Error code.
     */
    private int code;

    /**
     * Error message.
     */
    private String message;

    /**
     * Build a new instance.
     * @param code the given error code
     * @param message the error message
     */
    @JsonCreator
    public DefaultProjectProblem(
            @JsonProperty("code") int code,
            @JsonProperty("message") String message) {
        this.code = code;
        this.message = message;

    }

    /**
     * @return error code
     */
    @Override
    @JsonProperty("code")
    public int getCode() {
        return code;
    }

    /**
     * @return the problem's message
     */
    @Override
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }
}
