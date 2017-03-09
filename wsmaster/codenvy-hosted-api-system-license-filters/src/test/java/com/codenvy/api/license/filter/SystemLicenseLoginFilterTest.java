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
package com.codenvy.api.license.filter;

import com.codenvy.api.license.shared.dto.IssueDto;
import com.codenvy.api.license.shared.dto.LegalityDto;
import com.codenvy.api.license.shared.model.Constants;
import com.codenvy.api.license.shared.model.Issue;
import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.auth.sso.client.filter.RequestFilter;

import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.codenvy.api.license.shared.model.Issue.Status.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED;
import static com.codenvy.api.permission.server.SystemDomain.MANAGE_SYSTEM_ACTION;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

/** Test related to @SystemLicenseLoginFilter class. */
@Listeners(value = MockitoTestNGListener.class)
public class SystemLicenseLoginFilterTest {
    public static final String API_ENDPOINT = "http://localhost:8080/api";

    public static final String ACCEPT_FAIR_SOURCE_LICENSE_PAGE_URL                = "/site/auth/accept-fair-source-license";
    public static final String FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_ERROR_PAGE_URL = "/site/error/fair-source-license-is-not-accepted";

    @Mock
    private HttpServletRequest servletRequest;
    @Mock
    HttpServletResponse    servletResponse;
    @Mock
    ServletContext         servletContext;
    @Mock
    FilterChain            chain;
    @Mock
    FilterConfig           filterConfig;
    @Mock
    RequestFilter          requestFilter;
    @Mock
    HttpJsonRequestFactory requestFactory;
    @Mock
    HttpJsonRequest        request;
    @Mock
    HttpJsonResponse       response;
    @Mock
    Subject                subject;
    @Mock
    PrintWriter            servletResponseWriter;
    @Mock
    LegalityDto            legalityDto;

    @InjectMocks
    SystemLicenseLoginFilter filter;

    @BeforeMethod
    public void setup() throws IOException {
        EnvironmentContext.reset();
        EnvironmentContext.getCurrent().setSubject(subject);
        filter = new SystemLicenseLoginFilter();
        filter.requestFilter = requestFilter;
        filter.requestFactory = requestFactory;

        when(filterConfig.getServletContext()).thenReturn(servletContext);
        setFieldValue(filter, "apiEndpoint", API_ENDPOINT);
        setFieldValue(filter, "acceptFairSourceLicensePageUrl", ACCEPT_FAIR_SOURCE_LICENSE_PAGE_URL);
        setFieldValue(filter, "fairSourceLicenseIsNotAcceptedErrorPageUrl", FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_ERROR_PAGE_URL);

        when(servletResponse.getWriter()).thenReturn(servletResponseWriter);
        when(requestFilter.shouldSkip(eq(servletRequest))).thenReturn(false);
    }

    @Test(dataProvider = "testData")
    public void testOnFilteringAddresses(boolean isAdmin, boolean isNoInteraction, boolean isFairSourceLicenseAccepted,
                                         Runnable verification) throws Exception {
        //given
        when(servletRequest.getRequestURI()).thenReturn("/api/user");

        setIsFairSourceLicenseAccepted(isFairSourceLicenseAccepted);
        setIsAdminAndNotInteractive(isAdmin, isNoInteraction);

        //when
        filter.doFilter(servletRequest, servletResponse, chain);

        //then
        verification.run();
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][] {
                // should redirect to 'accept fair source license' page if user is admin and with interaction
                {true, false, false, verifySendRedirection(ACCEPT_FAIR_SOURCE_LICENSE_PAGE_URL)},

                // should return Forbidden error if user is admin and without interaction
                {true, true, false, verifyForbiddenError()},

                // should redirect to 'fair source license is not accepted error' page if user isn't admin and with interaction
                {false, false, false, verifySendRedirection(FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_ERROR_PAGE_URL)},

                // should return Forbidden error if user isn't admin and without interaction
                {false, true, false, verifyForbiddenError()},

                // shouldn't return redirection or error if fair source license has accepted
                {true, false, true, verifyNoRedirectionAndError()},

                // shouldn't return redirection or error if fair source license has accepted
                {true, true, true, verifyNoRedirectionAndError()},

                // shouldn't return redirection or error if fair source license has accepted
                {false, false, true, verifyNoRedirectionAndError()},

                // shouldn't return redirection or error if fair source license has accepted
                {false, true, true, verifyNoRedirectionAndError()},
                };
    }

    @Test
    public void shouldSkipRequestByFilter() throws Exception {
        //given
        when(servletRequest.getRequestURI()).thenReturn("/api/license/system/legality");

        //when
        filter.doFilter(servletRequest, servletResponse, chain);

        //then
        verifyNoMoreInteractions(requestFactory);
        verifyNoRedirectionAndError().run();
    }

    @Test
    public void shouldNotMakeAdditionalRequestOnLicenseCheck() throws Exception {
        //given
        when(servletRequest.getRequestURI()).thenReturn("/api/user");
        when(requestFactory.fromUrl(anyString())).thenReturn(request);
        when(request.useGetMethod()).thenReturn(request);
        when(request.request()).thenReturn(response);
        when(response.asDto(LegalityDto.class)).thenReturn(legalityDto);
        when(legalityDto.getIssues()).thenReturn(Collections.emptyList());

        //when
        filter.doFilter(servletRequest, servletResponse, chain);
        filter.doFilter(servletRequest, servletResponse, chain);
        filter.doFilter(servletRequest, servletResponse, chain);
        filter.doFilter(servletRequest, servletResponse, chain);
        filter.doFilter(servletRequest, servletResponse, chain);

        //then
        verify(request, times(1)).request();
    }

    @Test
    public void shouldCheckLicenseEachTimeWhenLicenceIsNotAccepted() throws Exception {
        //given
        final IssueDto issue = mock(IssueDto.class);
        final ArrayList<IssueDto> issues = new ArrayList<>();
        issues.add(issue);

        when(servletRequest.getRequestURI()).thenReturn("/api/user");
        when(requestFactory.fromUrl(anyString())).thenReturn(request);
        when(request.useGetMethod()).thenReturn(request);
        when(request.request()).thenReturn(response);
        when(response.asDto(LegalityDto.class)).thenReturn(legalityDto);
        when(legalityDto.getIssues()).thenReturn(issues);
        when(issue.getStatus()).thenReturn(FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED);

        //when
        filter.doFilter(servletRequest, servletResponse, chain);
        filter.doFilter(servletRequest, servletResponse, chain);
        filter.doFilter(servletRequest, servletResponse, chain);
        filter.doFilter(servletRequest, servletResponse, chain);
        filter.doFilter(servletRequest, servletResponse, chain);

        //then
        verify(request, times(5)).request();
    }

    private Runnable verifySendRedirection(String url) {
        return () -> {
            try {
                verify(servletResponse).sendRedirect(eq(url));
                verify(chain, never()).doFilter(servletRequest, servletResponse);
            } catch (Exception e) {
                fail(e.getMessage(), e);
            }
        };
    }

    private Runnable verifyForbiddenError() {
        return () -> {
            try {
                verify(servletResponse).setStatus(eq(HttpServletResponse.SC_FORBIDDEN));
                verify(servletResponseWriter).write(eq(String.format("{\"message\":\"%s\"}",
                                                                     Constants.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE)));

                verify(chain, never()).doFilter(servletRequest, servletResponse);
            } catch (Exception e) {
                fail(e.getMessage(), e);
            }
        };
    }

    private Runnable verifyNoRedirectionAndError() {
        return () -> {
            try {
                verify(chain).doFilter(servletRequest, servletResponse);
                verifyNoMoreInteractions(servletResponse);
            } catch (Exception e) {
                fail(e.getMessage(), e);
            }
        };
    }

    public void setIsFairSourceLicenseAccepted(boolean hasFairSourceLicenseAccepted) throws Exception {
        List<IssueDto> issues;
        if (hasFairSourceLicenseAccepted) {
            issues = Collections.emptyList();
        } else {
            IssueDto issueDto = mock(IssueDto.class);
            when(issueDto.getStatus()).thenReturn(Issue.Status.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED);
            when(issueDto.getMessage()).thenReturn(Constants.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE);
            issues = Collections.singletonList(issueDto);
        }

        LegalityDto legalityDto = mock(LegalityDto.class);
        when(legalityDto.getIssues()).thenReturn(issues);
        when(legalityDto.getIsLegal()).thenReturn(false);

        when(requestFactory.fromUrl(API_ENDPOINT + "/license/system/legality")).thenReturn(request);
        when(request.useGetMethod()).thenReturn(request);
        when(request.request()).thenReturn(response);
        when(response.asDto(LegalityDto.class)).thenReturn(legalityDto);
    }

    private void setIsAdminAndNotInteractive(boolean isAdmin, boolean noUserInteraction) {
        setFieldValue(filter, "noUserInteraction", noUserInteraction);
        when(subject.hasPermission(SystemDomain.DOMAIN_ID, null, MANAGE_SYSTEM_ACTION)).thenReturn(isAdmin);
    }

    private static void setFieldValue(Filter filter, String fieldName, Object fieldValue) {
        try {
            Field field = filter.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(filter, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // ignored
        }
    }

}
