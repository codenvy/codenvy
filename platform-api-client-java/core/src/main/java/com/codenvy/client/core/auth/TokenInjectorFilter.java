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
package com.codenvy.client.core.auth;

import com.codenvy.client.auth.Token;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Filter used to inject {@link Token} in client request.
 *
 * @author Kevin Pollet
 */
@Provider
public class TokenInjectorFilter implements ClientRequestFilter {
    public static final String TOKEN_PROPERTY_NAME = "token";

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        final Token token = (Token)requestContext.getProperty(TOKEN_PROPERTY_NAME);
        if (token != null && token.value() != null) {

            requestContext.setUri(UriBuilder.fromUri(requestContext.getUri())
                                            .queryParam(TOKEN_PROPERTY_NAME, token.value())
                                            .build());
        }
    }
}
