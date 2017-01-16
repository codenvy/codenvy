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

import com.codenvy.client.CodenvyErrorException;
import com.codenvy.client.core.model.DefaultError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper class used to convert JAXRS exception into CodenvyException
 *
 * @author Kevin Pollet
 */
public class CodenvyErrorExceptionHelper {

    /**
     * Utility class so no public constructor.
     */
    private CodenvyErrorExceptionHelper() {

    }

    /**
     * Reads the {@code Response} body and constructs an instance of {@link CodenvyErrorException}.
     *
     * @param response
     *         the rest API {@link javax.ws.rs.core.Response}.
     * @throws NullPointerException
     *         if response parameter is {@code null}.
     */
    public static CodenvyErrorException from(Response response) {

        // Handle case where we recevied an internal error
        MediaType mediaType = response.getMediaType();
        if ("text".equals(mediaType.getType())) {
            return new CodenvyErrorException(response.getStatus(), response.getStatusInfo().getReasonPhrase().concat(":").concat(response.readEntity(String.class)));
        }
        final com.codenvy.client.model.Error codenvyError = checkNotNull(response).readEntity(DefaultError.class);
        return new CodenvyErrorException(response.getStatus(), codenvyError.message());
    }
}
