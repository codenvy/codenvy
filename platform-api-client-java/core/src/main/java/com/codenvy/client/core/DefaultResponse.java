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
package com.codenvy.client.core;

import com.codenvy.client.Response;

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link com.codenvy.client.Response}
 * It provides the data for providing the value, headers and status code
 *
 * @author Florent Benoit
 */
public class DefaultResponse<T> implements Response<T> {

    /**
     * Wrapped object.
     */
    private final T value;

    /**
     * JAX-RS Response
     */
    private final javax.ws.rs.core.Response jaxRsResponse;


    /**
     * Default constructor wrapping value and response
     *
     * @param jaxRsResponse
     *         the JAX-RS response
     * @param value
     *         the value unwrapped from the response
     */
    public DefaultResponse(javax.ws.rs.core.Response jaxRsResponse, T value) {
        this.jaxRsResponse = jaxRsResponse;
        this.value = value;
    }


    /**
     * Returns the wrapped value
     *
     * @return the API request result.
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * @return the headers of the response
     */
    @Override
    public Map<String, List<Object>> getHeaders() {
        return jaxRsResponse.getHeaders();
    }

    /**
     * @return the status code of the response
     */
    @Override
    public int getStatusCode() {
        return jaxRsResponse.getStatus();
    }
}
