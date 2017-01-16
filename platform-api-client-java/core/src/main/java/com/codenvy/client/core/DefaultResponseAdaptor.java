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
 * @param <T>
 *         the {@linkplain java.lang.reflect.Type Type} of the adapted request response.
 * @author Florent benoit
 */
public class DefaultResponseAdaptor<T, S> implements Response<T> {

    private final Response<S> wrappedResponse;

    private final Adaptor<T, S> adaptor;

    public DefaultResponseAdaptor(Adaptor<T, S> adaptor, Response<S> wrappedResponse) {
        this.adaptor = adaptor;
        this.wrappedResponse = wrappedResponse;
    }

    /**
     * Returns the wrapped value
     *
     * @return the API request result.
     */
    @Override
    public T getValue() {
        return adaptor.adapt(wrappedResponse.getValue());
    }

    /**
     * @return the headers of the response
     */
    @Override
    public Map<String, List<Object>> getHeaders() {
        return wrappedResponse.getHeaders();
    }

    /**
     * @return the status code of the response
     */
    @Override
    public int getStatusCode() {
        return wrappedResponse.getStatusCode();
    }

}
