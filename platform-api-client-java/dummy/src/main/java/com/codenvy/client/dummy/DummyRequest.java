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
package com.codenvy.client.dummy;

import com.codenvy.client.CodenvyException;
import com.codenvy.client.Request;
import com.codenvy.client.Response;

import java.util.List;
import java.util.Map;

/**
 * @author Florent Benoit
 */
public class DummyRequest<T> implements Request<T> {

    private T wrapped;

    public DummyRequest(T wrapped) {
        this.wrapped = wrapped;

    }

    public T call() {
        return wrapped;
    }

    /**
     * Executes the Codenvy API request.
     *
     * @return the API request result.
     * @throws com.codenvy.client.CodenvyException
     *         if something goes wrong with the API call.
     */
    @Override
    public T execute() throws CodenvyException {
        return call();
    }

    /**
     * Executes the Codenvy API request and then return details on the response.
     *
     * @return the API response result.
     * @throws com.codenvy.client.CodenvyException
     *         if something goes wrong with the API call.
     */
    @Override
    public Response<T> response() throws CodenvyException {
        return new Response<T>() {
            @Override
            public T getValue() {
                return call();
            }

            @Override
            public Map<String, List<Object>> getHeaders() {
                return null;
            }

            @Override
            public int getStatusCode() {
                return 200;
            }
        };
    }
}
