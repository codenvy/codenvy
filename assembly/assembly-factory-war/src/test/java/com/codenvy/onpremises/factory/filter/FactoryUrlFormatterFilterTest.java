/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.onpremises.factory.filter;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

import static com.codenvy.onpremises.factory.filter.RemoveIllegalCharactersFactoryURLFilter.USER_NAME;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link RemoveIllegalCharactersFactoryURLFilter}.
 *
 * @author Anton Korneta
 */
@Listeners(value = {MockitoTestNGListener.class})
public class FactoryUrlFormatterFilterTest {

    @Mock
    private HttpServletRequest                  requestMock;
    @Mock
    private HttpServletResponse                 responseMock;
    @Mock
    private FilterChain                         chainMock;
    @Captor
    private ArgumentCaptor<HttpServletRequest>  requestCaptor;
    @Captor
    private ArgumentCaptor<HttpServletResponse> responseCaptor;

    private RemoveIllegalCharactersFactoryURLFilter filter;

    @BeforeMethod
    public void setup() {
        filter = new RemoveIllegalCharactersFactoryURLFilter();
    }

    @Test
    public void shouldReplaceUsernameInRequestQueryString() throws Exception {
        final String query = "name=test&user=user@codenvy.com";
        when(requestMock.getQueryString()).thenReturn(query);

        filter.doFilter(requestMock, responseMock, chainMock);

        verify(chainMock).doFilter(requestCaptor.capture(), responseCaptor.capture());
        assertEquals(requestCaptor.getValue().getQueryString(), "name=test&user=usercodenvycom");
    }

    @Test
    public void shouldReturnRequestQueryString() throws Exception {
        final String query = "user=usercodenvycom&user=user2codenvycom";
        when(requestMock.getQueryString()).thenReturn(query);

        filter.doFilter(requestMock, responseMock, chainMock);

        verify(chainMock).doFilter(requestCaptor.capture(), responseCaptor.capture());
        assertEquals(requestCaptor.getValue().getQueryString(), query);
    }

    @Test
    public void shouldReplaceUsernameParameter() throws Exception {
        final String username = "user@codenvy.com";
        when(requestMock.getParameter(USER_NAME)).thenReturn(username);

        filter.doFilter(requestMock, responseMock, chainMock);

        verify(chainMock).doFilter(requestCaptor.capture(), responseCaptor.capture());
        assertEquals(requestCaptor.getValue().getParameter(USER_NAME), "usercodenvycom");
    }

    @Test
    public void shouldReturnNotUsernameParameter() throws Exception {
        final String param = "param";
        when(requestMock.getParameter(param)).thenReturn(param);

        filter.doFilter(requestMock, responseMock, chainMock);

        verify(chainMock).doFilter(requestCaptor.capture(), responseCaptor.capture());
        assertEquals(requestCaptor.getValue().getParameter(param), param);
    }

    @Test
    public void shouldReplaceUsernameInRequestParameterMap() throws Exception {
        final String[] usernames = {"user@codenvy.com"};
        when(requestMock.getParameterMap()).thenReturn(new HashMap<>(singletonMap(USER_NAME, usernames)));

        filter.doFilter(requestMock, responseMock, chainMock);

        verify(chainMock).doFilter(requestCaptor.capture(), responseCaptor.capture());
        assertEquals(requestCaptor.getValue().getParameterMap().get(USER_NAME)[0], "usercodenvycom");
    }

    @Test
    public void shouldReplaceAllInvalidQueryParameters() throws Exception {
        final String query = "user=qwe@mail.com&name=johny&user=username&user=wtf@gmail.com";
        when(requestMock.getQueryString()).thenReturn(query);

        filter.doFilter(requestMock, responseMock, chainMock);

        verify(chainMock).doFilter(requestCaptor.capture(), responseCaptor.capture());
        assertEquals(requestCaptor.getValue().getQueryString(), "user=qwemailcom&name=johny&user=username&user=wtfgmailcom");
    }

    @Test
    public void shouldReturnNullWhenQueryNull() throws Exception {
        final String query = null;
        when(requestMock.getQueryString()).thenReturn(query);

        filter.doFilter(requestMock, responseMock, chainMock);

        verify(chainMock).doFilter(requestCaptor.capture(), responseCaptor.capture());
        assertEquals(requestCaptor.getValue().getQueryString(), query);
    }
}
