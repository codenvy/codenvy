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

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.ClientRequestContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.codenvy.client.core.auth.TokenInjectorFilter.TOKEN_PROPERTY_NAME;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link TokenInjectorFilter} tests.
 * 
 * @author Kevin Pollet
 */
public class TokenInjectorFilterTest {
    private final URI dummyUri;
    private final URI dummyUriWithToken;

    public TokenInjectorFilterTest() throws URISyntaxException {
        dummyUri = new URI("http://dummy.com");
        dummyUriWithToken = new URI("http://dummy.com/?token=123123");
    }

    @Test
    public void testFilterWithoutTokenProperty() throws IOException {
        final ClientRequestContext clientRequestContext = mock(ClientRequestContext.class);
        when(clientRequestContext.getProperty(TOKEN_PROPERTY_NAME)).thenReturn(null);
        when(clientRequestContext.getUri()).thenReturn(dummyUri);

        final TokenInjectorFilter tokenInjectorFilter = new TokenInjectorFilter();
        tokenInjectorFilter.filter(clientRequestContext);

        verify(clientRequestContext, times(0)).getUri();
        verify(clientRequestContext, times(0)).setUri(any(URI.class));
        verify(clientRequestContext, times(1)).getProperty(TOKEN_PROPERTY_NAME);

        Assert.assertEquals(dummyUri, clientRequestContext.getUri());
    }

    @Test
    public void testFilterWithTokenProperty() throws IOException {
        final ClientRequestContext clientRequestContext = mock(ClientRequestContext.class);
        when(clientRequestContext.getProperty(TOKEN_PROPERTY_NAME)).thenReturn(new DefaultToken("123123"));
        when(clientRequestContext.getUri()).thenReturn(dummyUri);

        final TokenInjectorFilter tokenInjectorFilter = new TokenInjectorFilter();
        tokenInjectorFilter.filter(clientRequestContext);

        verify(clientRequestContext, times(1)).getUri();
        verify(clientRequestContext, times(1)).setUri(dummyUriWithToken);
        verify(clientRequestContext, times(1)).getProperty(TOKEN_PROPERTY_NAME);
    }
}
