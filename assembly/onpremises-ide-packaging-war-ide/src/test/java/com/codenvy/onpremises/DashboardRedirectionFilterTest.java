/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.onpremises;

import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.omg.CORBA.ServerRequest;
import org.testng.annotations.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class DashboardRedirectionFilterTest {
    @Mock
    private FilterChain chain;

    @Mock
    HttpJsonHelper helper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private DashboardRedirectionFilter filter;

    @Test
    public void shouldSkipRequestToProject() throws Exception {
        //given
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/ws/ws-id/project1");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/ws/ws-id/project1"));
        EnvironmentContext context = new EnvironmentContext();
        context.setWorkspaceId("ws-id");
        EnvironmentContext.setCurrent(context);

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(chain).doFilter((ServletRequest)any(ServerRequest.class), any(ServletResponse.class));
    }

    @Test(dataProvider = "nonProjectPathProvider")
    public void shouldRedirectIfRequestToPersistentWsWithoutProject(String uri, String url) throws Exception {
        //given
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        EnvironmentContext context = new EnvironmentContext();
        context.setWorkspaceId("ws-id");
        context.setWorkspaceTemporary(false);
        context.setSubject(new SubjectImpl("id123", "name", "token123", Arrays.asList("user"), false));
        EnvironmentContext.setCurrent(context);

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).sendRedirect(eq("/dashboard/"));
    }

    @DataProvider(name = "nonProjectPathProvider")
    public Object[][] nonProjectPathProvider() {
        return new Object[][]{{"/ws/ws-id/", "http://localhost:8080/ws/ws-id/"},
                              {"/ws/ws-id", "http://localhost:8080/ws/ws-id"},
        };
    }

    @Test
    public void shouldSkipRequestToTemporaryWs() throws Exception {
        //given
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/ws/ws-id");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/ws/ws-id"));
        EnvironmentContext context = new EnvironmentContext();
        context.setWorkspaceId("ws-id");
        context.setWorkspaceTemporary(true);
        EnvironmentContext.setCurrent(context);

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(chain).doFilter((ServletRequest)any(ServerRequest.class), any(ServletResponse.class));
    }

    @Test(dataProvider = "notGETMethodProvider")
    public void shouldSkipNotGETRequest(String method) throws Exception {
        //given
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn("/ws/ws-id/project1");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/ws/ws-id/project1"));
        EnvironmentContext context = new EnvironmentContext();
        context.setWorkspaceId("ws-id");
        EnvironmentContext.setCurrent(context);

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(chain).doFilter((ServletRequest)any(ServerRequest.class), any(ServletResponse.class));
    }

    @DataProvider(name = "notGETMethodProvider")
    public Object[][] notGETMethodProvider() {
        return new Object[][]{{"POST"},
                              {"HEAD"},
                              {"DELETE"},
                              {"PUT"}
        };
    }
}
