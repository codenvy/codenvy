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
package com.codenvy.client;

/**
 * The API request contract returned by the Codenvy client API.
 *
 * @param <T>
 *         the API request return {@linkplain java.lang.reflect.Type Type}
 * @author Kevin Pollet
 */
public interface Request<T> {

    /**
     * Executes the Codenvy API request.
     *
     * @return the API request result.
     * @throws CodenvyException
     *         if something goes wrong with the API call.
     */
    T execute() throws CodenvyException;

    /**
     * Executes the Codenvy API request and then return details on the response.
     *
     * @return the API response result.
     * @throws CodenvyException
     *         if something goes wrong with the API call.
     */
    Response<T> response() throws CodenvyException;

}
