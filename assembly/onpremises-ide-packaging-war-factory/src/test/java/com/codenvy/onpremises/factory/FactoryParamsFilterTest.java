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

import com.codenvy.onpremises.factory.filter.FactoryParamsFilter;

import org.eclipse.che.api.factory.server.FactoryConstants;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 *
 */
@Listeners(value = {MockitoTestNGListener.class})
public class FactoryParamsFilterTest {

    @Mock
    private HttpServletRequest  req;

    @Mock
    private HttpServletResponse res;

    @Mock
    private FilterChain         chain;

    @Mock
    private RequestDispatcher   requestDispatcher;

    FactoryParamsFilter filter;

    @BeforeMethod
    public void setup() throws Exception {
        filter = new FactoryParamsFilter();
        Field f = filter.getClass().getDeclaredField("INVALID_FACTORY_URL_PAGE");
        f.setAccessible(true);
        f.set(filter, "/resources/error-invalid-factory-url.jsp");
    }


    @Test
    public void shouldThrowExceptionIfMoreThan1ParamPresentInFactory() throws Exception {
        Map<String, String[]> parametersMap = new HashMap<>();
        parametersMap.put("id", new String[] {"12344"});
        parametersMap.put("v", new String[] {"2.0"});
        when(req.getParameterMap()).thenReturn(parametersMap);
        when(req.getRequestDispatcher(eq("/resources/error-invalid-factory-url.jsp"))).thenReturn(requestDispatcher);

        filter.doFilter(req, res, chain);

        verify(req).setAttribute(eq(RequestDispatcher.ERROR_MESSAGE), eq(FactoryConstants.INVALID_PARAMETER_MESSAGE));
        verify(requestDispatcher).forward(req, res);
        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void shouldNotThrowExceptionIfNoIdParamPresentInFactory() throws Exception {
        Map<String, String[]> parametersMap = new HashMap<>();
        parametersMap.put("any", new String[] {"anything"});
        when(req.getParameterMap()).thenReturn(parametersMap);
        when(req.getRequestDispatcher(eq("/resources/error-invalid-factory-url.jsp"))).thenReturn(requestDispatcher);

        filter.doFilter(req, res, chain);

        verify(req, never()).setAttribute(eq(RequestDispatcher.ERROR_MESSAGE), eq(FactoryConstants.INVALID_PARAMETER_MESSAGE));
        verify(requestDispatcher, never()).forward(req, res);
        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void shouldThrowExceptionIfIdParamHas2Values() throws Exception {
        Map<String, String[]> parametersMap = new HashMap<>();
        parametersMap.put("id", new String[]{"12344", "56780"});
        when(req.getParameterMap()).thenReturn(parametersMap);
        when(req.getRequestDispatcher(eq("/resources/error-invalid-factory-url.jsp"))).thenReturn(requestDispatcher);

        filter.doFilter(req, res, chain);

        verify(req).setAttribute(eq(RequestDispatcher.ERROR_MESSAGE),
                                 matches("The parameter .* has a value submitted .* with a value that is unexpected. .*"));
        verify(requestDispatcher).forward(req, res);
    }
    
    @Test
    public void shouldThrowExceptionIfUserParamWithoutName() throws Exception {
        Map<String, String[]> parametersMap = new HashMap<>();
        parametersMap.put("user", new String[]{"user1"});
        when(req.getParameterMap()).thenReturn(parametersMap);
        when(req.getRequestDispatcher(eq("/resources/error-invalid-factory-url.jsp"))).thenReturn(requestDispatcher);

        filter.doFilter(req, res, chain);

        verify(req).setAttribute(eq(RequestDispatcher.ERROR_MESSAGE),eq(FactoryConstants.INVALID_PARAMETER_MESSAGE));
        verify(requestDispatcher).forward(req, res);
    }
}
