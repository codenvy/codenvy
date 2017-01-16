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

import com.codenvy.client.CodenvyException;
import com.codenvy.client.Request;
import com.codenvy.client.Response;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link com.codenvy.client.Request} implementation adapting an API request response to another type.
 *
 * @param <T>
 *         the {@linkplain java.lang.reflect.Type Type} of the adapted request response.
 * @param <S>
 *         the {@linkplain java.lang.reflect.Type Type} of the request response to adapt.
 * @author Kevin Pollet
 */
public class RequestResponseAdaptor<T, S> implements Request<T> {
    private final Request<S>    adaptee;
    private final Adaptor<T, S> adaptor;

    /**
     * Constructs an instance of {@link RequestResponseAdaptor}.
     *
     * @param adaptee
     *         the {@link Request} to adapt.
     * @param adaptor
     *         the {@link Request} response adaptor.
     * @throws NullPointerException
     *         if adaptee or adaptor parameter is {@code null}.
     */
    RequestResponseAdaptor(Request<S> adaptee, Adaptor<T, S> adaptor) {
        checkNotNull(adaptee);
        checkNotNull(adaptor);

        this.adaptee = adaptee;
        this.adaptor = adaptor;
    }

    @Override
    public T execute() throws CodenvyException {
        return adaptor.adapt(adaptee.execute());
    }

    @Override
    public Response<T> response() throws CodenvyException {
        Response<S> details = adaptee.response();

        // adaptor is for the value
        return new DefaultResponseAdaptor<>(adaptor, details);
    }


}
