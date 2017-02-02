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
package com.codenvy.onpremises.factory;

import com.codenvy.onpremises.factory.filter.FactoryRetrieverFilter;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Field;

import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 6/24/15.
 *
 */
@Listeners(value = {MockitoTestNGListener.class})
public class FactoryRetrieverFilterTest {

    @Mock
    private HttpServletRequest     req;

    @Mock
    private HttpServletResponse    res;

    @Mock
    private FilterChain            chain;

    @Mock
    private HttpJsonRequestFactory requestFactory;

    private HttpJsonRequest        request;

    @Mock
    private RequestDispatcher      requestDispatcher;

    FactoryRetrieverFilter filter;

    @BeforeMethod
    public void setup() throws Exception {
        
        request = mock(HttpJsonRequest.class, (Answer) invocation -> {
            if (invocation.getMethod().getReturnType().isInstance(invocation.getMock())) {
                return invocation.getMock();
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });
        when(requestFactory.fromUrl(anyString())).thenReturn(request);
        
        filter = new FactoryRetrieverFilter();
        
        Field f = filter.getClass().getDeclaredField("httpRequestFactory");
        f.setAccessible(true);
        f.set(filter, requestFactory);

        Field f2 = filter.getClass().getDeclaredField("INVALID_FACTORY_URL_PAGE");
        f2.setAccessible(true);
        f2.set(filter, "/resources/error-invalid-factory-url.jsp");

        Field f3 = filter.getClass().getDeclaredField("apiEndPoint");
        f3.setAccessible(true);
        f3.set(filter, "http://codenvy.com/api");
    }


    @Test
    public void shouldForwardToErrorPageIfGetFactoryThrowsException() throws Exception {
        when(req.getParameter("id")).thenReturn("12345");
        when(request.request()).thenThrow(new ServerException("get factory exception message"));
        when(req.getRequestDispatcher(eq("/resources/error-invalid-factory-url.jsp"))).thenReturn(requestDispatcher);

        filter.doFilter(req, res, chain);

        verify(req).setAttribute(eq(RequestDispatcher.ERROR_MESSAGE), eq("get factory exception message"));
        verify(requestDispatcher).forward(req, res);
    }
}
