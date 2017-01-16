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
package com.codenvy.auth.sso.client;

import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.codenvy.auth.sso.client.token.RequestTokenExtractor;

import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.everrest.core.impl.RuntimeDelegateImpl;
import org.everrest.test.mock.MockHttpServletRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.security.Principal;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/** Test related to @LoginFilter class. */
@Listeners(value = MockitoTestNGListener.class)
public class LoginFilterTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoginFilterTest.class);


    @Mock
    SessionStore          sessionStore;
    @Mock
    RequestTokenExtractor tokenExtractor;
    @Mock
    ClientUrlExtractor    clientUrlExtractor;
    @Spy
    RequestWrapper requestWrapper = new RequestWrapper();
    @Mock
    ServerClient        ssoServerClient;
    @Mock
    HttpServletResponse response;
    @Mock
    HttpSession         session;
    @Mock
    FilterChain         chain;
    @Mock
    FilterConfig        filterConfig;
    @Mock
    ServletContext      servletContext;
    @Mock
    ServletConfig       servletConfig;
    @Mock
    RequestFilter       requestFilter;
    @Mock
    TokenHandler        tokenHandler;

    @InjectMocks
    LoginFilter filter;

    private static void setFieldValue(LoginFilter filter, String fieldName, Object fieldValue) {

        try {
            Field field = LoginFilter.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(filter, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    private static void setSession(MockHttpServletRequest request, HttpSession session) {
        try {
            Field sessionField = MockHttpServletRequest.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(request, session);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    @BeforeMethod
    public void prepare() throws ServletException {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
        when(filterConfig.getServletContext()).thenReturn(servletContext);
        setFieldValue(filter, "apiEndpoint", "http://localhost:8080/api");
        setFieldValue(filter, "loginPageUrl", "/site/login");
        setFieldValue(filter, "cookiesDisabledErrorPageUrl", "/site/cookiesDisabledErrorPageUrl");
        setFieldValue(filter, "allowAnonymous", false);
//        setFieldValue(filter, "attemptToGetNewTokenIfNotExist", false);
//        setFieldValue(filter, "attemptToGetNewTokenIfInvalid", false);
//
        EnvironmentContext.reset();

    }

    @Test
    public void shouldRedirectToSsoServerIfSessionDoesntContainsPrincipalOnGet()
            throws IOException, ServletException, URISyntaxException {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/ws/myworkspace", null, 0, "GET", null);

        when(clientUrlExtractor.getClientUrl(eq(request))).thenReturn("http://localhost:8080/ws/myworkspace");
        setFieldValue(filter, "tokenHandler", new RecoverableTokenHandler(requestWrapper, clientUrlExtractor, true));

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).sendRedirect(eq("/api/internal/sso/server/refresh?" +
                                         "redirect_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fmyworkspace" +
                                         "&client_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fmyworkspace&" +
                                         "allowAnonymous=true"));
    }

    @Test
    public void shouldRedirectToSsoServerAnonymousIsNotAllowed()
            throws IOException, ServletException, URISyntaxException {
        //given
        setFieldValue(filter, "attemptToGetNewTokenIfNotExist", true);
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/ws/myworkspace", null, 0, "GET", null);
        when(clientUrlExtractor.getClientUrl(eq(request))).thenReturn("http://localhost:8080/ws/myworkspace");
        setFieldValue(filter, "tokenHandler", new RecoverableTokenHandler(requestWrapper, clientUrlExtractor, false));

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).sendRedirect(eq("/api/internal/sso/server/refresh?" +
                                         "redirect_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fmyworkspace" +
                                         "&client_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fmyworkspace" +
                                         "&allowAnonymous=false"));
    }

    //
    @Test
    public void shouldRedirectToSsoServerWithCorrectRedirectAndClientUrl()
            throws IOException, ServletException, URISyntaxException {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/ws/mypersonal.Workspace", null, 0, "GET", null);
        when(clientUrlExtractor.getClientUrl(eq(request))).thenReturn("http://localhost:8080/ws/mypersonal.Workspace");
        setFieldValue(filter, "tokenHandler", new RecoverableTokenHandler(requestWrapper, clientUrlExtractor, false));

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).sendRedirect(eq("/api/internal/sso/server/refresh?" +
                                         "redirect_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fmypersonal.Workspace" +
                                         "&client_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fmypersonal.Workspace" +
                                         "&allowAnonymous=false"));
    }

    @Test
    public void shouldAppendQueryUrlToRedirectURLToSsoServer()
            throws IOException, ServletException, URISyntaxException {
        //given
        HttpServletRequest request = new MockHttpServletRequest(
                "http://localhost:8080/ws/mypersonal.Workspace?myparam=myValue&param2=value2", null, 0, "GET",
                null);
        when(clientUrlExtractor.getClientUrl(eq(request))).thenReturn("http://localhost:8080/ws/mypersonal.Workspace");
        filter.init(filterConfig);
        setFieldValue(filter, "tokenHandler", new RecoverableTokenHandler(requestWrapper, clientUrlExtractor, false));

        //when
        filter.doFilter(request, response, chain);

        //then

        verify(response).sendRedirect(eq("/api/internal/sso/server/refresh?" +
                                         "redirect_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fmypersonal" +
                                         ".Workspace%3Fmyparam%3DmyValue%26param2%3Dvalue2" +
                                         "&client_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fmypersonal.Workspace" +
                                         "&allowAnonymous=false"));
    }

    @Test
    public void shouldRedirectToLoginIfPrincipalIsNotNullButQueryParameterLoginExists()
            throws IOException, ServletException, URISyntaxException {
        //given
        HttpServletRequest request = new MockHttpServletRequest(
                "http://localhost:8080/ws/mypersonal.Workspace?myparam=myValue&param2=value2&login", null, 0,
                "GET",
                null);
        when(clientUrlExtractor.getClientUrl(eq(request))).thenReturn("http://localhost:8080/ws/mypersonal.Workspace");
        request.getSession().setAttribute("principal", new SsoClientPrincipal("token",
                                                                              "http://localhost:8080/ws/mypersonal" +
                                                                              ".Workspace",
                                                                              createSubject("user@domain")
        ));
        filter.init(filterConfig);

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).sendRedirect(eq("/site/login?" +
                                         "redirect_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fmypersonal" +
                                         ".Workspace%3Fmyparam%3DmyValue%26param2%3Dvalue2" +
                                         "&client_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fmypersonal.Workspace"));


    }

    @Test
    public void shouldRedirectToLoginIfPrincipalIsNullButQueryParameterLoginExists()
            throws IOException, ServletException, URISyntaxException {
        //given
        HttpServletRequest request = new MockHttpServletRequest(
                "http://localhost:8080/ws/mypersonal.Workspace?myparam=myValue&param2=value2&login", null, 0,
                "GET",
                null);
        when(clientUrlExtractor.getClientUrl(eq(request))).thenReturn("http://localhost:8080/ws/mypersonal.Workspace");
        filter.init(filterConfig);

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).sendRedirect(eq("/site/login?" +
                                         "redirect_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fmypersonal" +
                                         ".Workspace%3Fmyparam%3DmyValue%26param2%3Dvalue2" +
                                         "&client_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fmypersonal.Workspace"));


    }


    @Test
    public void shouldPutPrincipalInSession() throws IOException, ServletException {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/ws/ws?token=t13f", null, 0, "GET", null);

        when(tokenExtractor.getToken(eq(request))).thenReturn("t13f");
        when(ssoServerClient.getSubject(eq("t13f"), anyString())).thenReturn(createSubject("user@domain"));
        //when
        filter.doFilter(request, response, chain);

        //then
        SsoClientPrincipal actual = (SsoClientPrincipal)request.getSession().getAttribute("principal");
        assertEquals(actual.getName(), "user@domain");
        verify(chain).doFilter(any(ServletRequest.class), eq(response));

    }

    @Test
    public void shouldBeAbleToReplaceAnonymousWithCorrectUserPrincipal() throws IOException, ServletException {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/ws/ws?token=t13f", null, 0, "GET", null);

        when(tokenExtractor.getToken(eq(request))).thenReturn("t13f");
        when(ssoServerClient.getSubject(eq("t13f"), anyString())).thenReturn(createSubject("user@domain"));
        when(ssoServerClient.getSubject(eq("t12f"), anyString())).thenReturn(createSubject("Anonymous123@domain"));
        when(clientUrlExtractor.getClientUrl(eq(request))).thenReturn("http://localhost:8080/ws/ws");

        SsoClientPrincipal anonymous = new SsoClientPrincipal("t12f",
                                                              "http://localhost:8080/ws/ws",
                                                              createSubject("Anonymous123@domain"));
        request.getSession().setAttribute("principal", anonymous);

        //when
        filter.doFilter(request, response, chain);

        //then
        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain).doFilter(captor.capture(), eq(response));
        assertEquals(((HttpServletRequest)captor.getValue()).getUserPrincipal().getName(), "user@domain");


    }


    @Test
    public void shouldRedirectToRefreshTokenIfTokenIsInvalidAndRecoverConfigured() throws IOException, ServletException {
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/ws/ws?token=t13f", null, 0, "GET", null);
        when(clientUrlExtractor.getClientUrl(eq(request))).thenReturn("http://localhost:8080/ws/ws");
        when(tokenExtractor.getToken(eq(request))).thenReturn("t13f");
        when(ssoServerClient.getSubject(eq("t13f"), anyString())).thenReturn(null);
        setFieldValue(filter, "tokenHandler", new RecoverableTokenHandler(requestWrapper, clientUrlExtractor, false));

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).sendRedirect(eq("/api/internal/sso/server/refresh?" +
                                         "redirect_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fws" +
                                         "&client_url=http%3A%2F%2Flocalhost%3A8080%2Fws%2Fws" +
                                         "&allowAnonymous=false"));
    }

    @Test
    public void shouldWrapPrincipalInRequest() throws IOException, ServletException {
        //given
        HttpServletRequest request = new MockHttpServletRequest("http://localhost:8080/ws/ws", null, 0, "GET", null);

        when(tokenExtractor.getToken(eq(request))).thenReturn("t13f");
        when(ssoServerClient.getSubject(eq("t13f"), anyString())).thenReturn(createSubject("user@domain"));
        when(clientUrlExtractor.getClientUrl(eq(request))).thenReturn("http://localhost:8080/ws/ws");
        SsoClientPrincipal principal = new SsoClientPrincipal("t13f",
                                                              "http://localhost:8080/ws/ws",
                                                              createSubject("user@domain"));
        request.getSession().setAttribute("principal", principal);

        //when
        filter.doFilter(request, response, chain);

        //then
        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any(ServletResponse.class));
        HttpServletRequest actual = captor.getValue();

        Assert.assertNotEquals(actual, request);
        assertEquals(actual.getRemoteUser(), "user@domain");

    }

    @Test
    public void shouldWrappedPrincipalShouldNotBeTheSameAsInRequest() throws IOException, ServletException {
        //given
        HttpServletRequest request = new MockHttpServletRequest("http://localhost:8080/ws/ws", null, 0, "GET", null);
        when(tokenExtractor.getToken(eq(request))).thenReturn("t13f");
        when(ssoServerClient.getSubject(eq("t13f"), anyString())).thenReturn(createSubject("user@domain"));
        when(clientUrlExtractor.getClientUrl(eq(request))).thenReturn("http://localhost:8080/ws/ws");
        SsoClientPrincipal principal = new SsoClientPrincipal("t13f", "http://localhost:8080/ws/ws",
                                                              createSubject("user@domain"));
        request.getSession().setAttribute("principal", principal);

        //when
        filter.doFilter(request, response, chain);

        //then
        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any(ServletResponse.class));
        HttpServletRequest actual = captor.getValue();

        Principal actualUserPrincipal = actual.getUserPrincipal();
        Assert.assertNotEquals(actualUserPrincipal, principal);
    }

    @Test
    public void shouldReuseSessionAssociatedWithToken() throws IOException, ServletException {
        //given
        HttpServletRequest request =
                spy(new MockHttpServletRequest("http://localhost:8080/ws/ws?token=t13f", null, 0, "GET", null));
        HttpSession session1 = mock(HttpSession.class);
        SsoClientPrincipal principal = mock(SsoClientPrincipal.class);

        when(tokenExtractor.getToken(eq(request))).thenReturn("t13f");
        when(request.getSession()).thenReturn(session);
        when(session1.getAttribute(eq("principal"))).thenReturn(principal);
        when(principal.getToken()).thenReturn("t13f");
        when(sessionStore.getSession(eq("t13f"))).thenReturn(session1);
        request.getSession().setAttribute("principal", principal);

        //when
        filter.doFilter(request, response, chain);

        //then
        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(chain).doFilter(captor.capture(), any(ServletResponse.class));
        HttpServletRequest actual = captor.getValue();
        assertEquals(actual.getSession(), session1);
        Assert.assertNotEquals(actual.getSession(), session);
        verify(sessionStore).getSession(eq("t13f"));
        verifyNoMoreInteractions(sessionStore);

    }


    public void shouldReplaceUpdateSessionStoreIfClientUsedSameSessionButDifferentToken()
            throws IOException, ServletException {
        //given
        HttpServletRequest request =
                spy(new MockHttpServletRequest("http://localhost:8080/ws/ws?token=t13f", null, 0, "GET", null));

        SsoClientPrincipal principal = mock(SsoClientPrincipal.class);

        when(tokenExtractor.getToken(eq(request))).thenReturn("t13f");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(eq("principal"))).thenReturn(principal);
        when(principal.getToken()).thenReturn("t12f");
        when(sessionStore.getSession(eq("t13f"))).thenReturn(session);
        request.getSession().setAttribute("principal", principal);
        setFieldValue(filter, "tokenHandler", tokenHandler);

        //when
        filter.doFilter(request, response, chain);

        //then

        ArgumentCaptor<HttpSession> captor = ArgumentCaptor.forClass(HttpSession.class);
        verify(tokenHandler).handleValidToken(any(HttpServletRequest.class),
                                              any(HttpServletResponse.class),
                                              any(FilterChain.class),
                                              captor.capture(),
                                              any(SsoClientPrincipal.class));

        assertEquals(captor.getValue(), session);
        verify(sessionStore).getSession(eq("t13f"));
        verify(sessionStore).removeSessionByToken(eq("t12f"));
        verify(sessionStore).saveSession(eq("t13f"), eq(session));
        verifyNoMoreInteractions(sessionStore);
    }

    @Test
    public void shouldRespond401IfPostRequestHasNoToken() throws IOException, ServletException {
        //given
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bos);
        when(response.getWriter()).thenReturn(writer);
        HttpServletRequest request =
                spy(new MockHttpServletRequest("http://localhost:8080/ws/ws", null, 0, "POST", null));

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertEquals(new String(bos.toByteArray()), "{\"message\":\"User not authorized to call this method.\"}");
    }

    @Test
    public void shouldRespond401IfGetRequestHasNoToken() throws IOException, ServletException {
        //given
        HttpServletRequest request =
                spy(new MockHttpServletRequest("http://localhost:8080/ws/ws", null, 0, "GET", null));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bos);
        when(response.getWriter()).thenReturn(writer);

        setFieldValue(filter, "tokenHandler", new NoUserInteractionTokenHandler(requestWrapper));

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertEquals(new String(bos.toByteArray()), "{\"message\":\"User not authorized to call this method.\"}");
    }

    @Test
    public void shouldRedirectToCookieErrorPageIfNotTokenInRequestButCookiesCheckRequested()
            throws IOException, ServletException {

        //given
        HttpServletRequest request =
                spy(new MockHttpServletRequest("http://localhost:8080/ws/ws?par=val&cookiePresent", null, 0, "GET",
                                               null));

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).sendRedirect(eq("/site/cookiesDisabledErrorPageUrl"));
    }

    @Test
    public void shouldRespond403ForPostRequestWithInvalidToken() throws ServletException, IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bos);
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/ws/ws?token=t13f", null, 0, "POST", null);
        setFieldValue(filter, "attemptToGetNewTokenIfInvalid", false);
        when(tokenExtractor.getToken(eq(request))).thenReturn("t13f");
        when(ssoServerClient.getSubject(eq("t13f"), anyString())).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);
        setFieldValue(filter, "tokenHandler", new NoUserInteractionTokenHandler(requestWrapper));

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertEquals(new String(bos.toByteArray()), "{\"message\":\"Provided token t13f is invalid\"}");
        Assert.assertTrue(bos.size() > 0);
    }

    @Test
    public void shouldRespond403ForGetRequestWithInvalidToken() throws ServletException, IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bos);
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/ws/ws?token=t13f", null, 0, "GET", null);
        setFieldValue(filter, "attemptToGetNewTokenIfInvalid", false);
        when(tokenExtractor.getToken(eq(request))).thenReturn("t13f");
        when(ssoServerClient.getSubject(eq("t13f"), anyString())).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);
        setFieldValue(filter, "tokenHandler", new NoUserInteractionTokenHandler(requestWrapper));

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertEquals(new String(bos.toByteArray()), "{\"message\":\"Provided token t13f is invalid\"}");
        Assert.assertTrue(bos.size() > 0);
    }

    @Test
    public void shouldRemoveCookiePresentParamFromValidGetRequestWithTokenAndRedirect()
            throws IOException, ServletException {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/ws/ws?token=t13f&cookiePresent&cookiePresent=true",
                                           null, 0, "GET", null);

        when(tokenExtractor.getToken(eq(request))).thenReturn("t13f");
        when(ssoServerClient.getSubject(eq("t13f"), anyString())).thenReturn(createSubject("user@domain"));
        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).sendRedirect(eq("http://localhost:8080/ws/ws?token=t13f"));
    }

    @Test
    public void shouldSkipRequestByFilter()
            throws IOException, ServletException, URISyntaxException {
        //given
        HttpServletRequest request =
                new MockHttpServletRequest("http://localhost:8080/ws/myworkspace", null, 0, "GET", null);
        when(requestFilter.shouldSkip(eq(request))).thenReturn(true);

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(chain).doFilter(request, response);
        verifyNoMoreInteractions(sessionStore, tokenExtractor, clientUrlExtractor, ssoServerClient);
    }

    private SubjectImpl createSubject(String email) {
        return new SubjectImpl(email, "user123", null, false);
    }
}
