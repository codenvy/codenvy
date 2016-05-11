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
package com.codenvy.service.http;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.everrest.test.mock.MockHttpServletRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Listeners(value = MockitoTestNGListener.class)
public class WorkspaceNameRequestParamInitializationFilterTest {
    @Mock
    WorkspaceInfoCache cache;
    @Mock
    Workspace          workspace;
    @Mock
    WorkspaceConfig    workspaceConfig;

    @Mock
    FilterChain         chain;
    @Mock
    HttpServletResponse response;


    @InjectMocks
    WorkspaceNameRequestParamInitializationFilter filter;

    @BeforeMethod
    public void setUpRedirect() throws NoSuchFieldException, IllegalAccessException {
        Field field = WorkspaceEnvironmentInitializationFilter.class.getDeclaredField("wsNotFoundRedirectUrl");
        field.setAccessible(true);
        field.set(filter, "http://codenvy.com/wsnotfound");
    }

    @AfterMethod
    public void clearContext() {
        EnvironmentContext.reset();
    }

    //FIXME: enable true
    @Test(enabled = false)
    public void shouldSetContextFromQueryParam() throws IOException, ServletException, ServerException, NotFoundException {
        //then
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                assertEquals(EnvironmentContext.getCurrent()
                                               .getWorkspaceName(), "myWorkspace");
                assertEquals(EnvironmentContext.getCurrent()
                                               .getWorkspaceId(), "wsId");

                return null;
            }
        })
               .when(chain)
               .doFilter(any(ServletRequest.class), any(ServletResponse.class));

        //given
        when(cache.getByName("myWorkspace", null)).thenReturn(workspace);
        when(workspace.getConfig()).thenReturn(workspaceConfig);
        when(workspaceConfig.getName()).thenReturn("myWorkspace");
        when(workspace.getId()).thenReturn("wsId");


        ServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/api/workspace?name=myWorkspace", null, 0, "GET", null);
        //when
        filter.doFilter(request, response, chain);
        //then
        verify(chain).doFilter(eq(request), eq(response));
    }

    @Test
    public void shouldContinueChainIfParameterIsNotSet() throws IOException, ServletException {
        //given
        ServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/api/workspace?token=t234", null, 0, "GET", null);
        //when
        filter.doFilter(request, response, chain);

        //then
        assertNull(EnvironmentContext.getCurrent().getWorkspaceName());
        assertNull(EnvironmentContext.getCurrent().getWorkspaceId());
        verify(chain).doFilter(eq(request), eq(response));
    }

    @Test
    public void shouldContinueChainIfWorkspaceCantBeFound() throws IOException, ServletException {
        //given
        ServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/api/workspace?name=myWorkspace", null, 0, "GET", null);
        //when
        filter.doFilter(request, response, chain);
        //then
        verify(chain).doFilter(eq(request), eq(response));
    }

    @Test
    public void shouldContinueChainIfFailToGetWorkspaceFromCache() throws IOException, ServletException, ServerException, NotFoundException {
        //given
        when(cache.getByName("myWorkspace", null)).thenThrow(NotFoundException.class);
        ServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/api/workspace?name=myWorkspace", null, 0, "GET", null);
        //when
        filter.doFilter(request, response, chain);
        //then
        verify(chain).doFilter(eq(request), eq(response));
    }

    @Test
    public void shouldContinueChainIfErrorToGetWorkspaceFromCache() throws IOException, ServletException, ServerException, NotFoundException {
        //given
        when(cache.getByName("myWorkspace", null)).thenThrow(ServerException.class);
        ServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/api/workspace?name=myWorkspace", null, 0, "GET", null);
        //when
        filter.doFilter(request, response, chain);
        //then
        verify(chain).doFilter(eq(request), eq(response));
    }
}
