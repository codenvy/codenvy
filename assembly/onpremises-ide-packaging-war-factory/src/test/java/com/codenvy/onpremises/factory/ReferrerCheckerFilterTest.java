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
package com.codenvy.onpremises.factory;

import com.codenvy.onpremises.factory.filter.ReferrerCheckerFilter;

import org.eclipse.che.api.factory.server.FactoryConstants;
import org.eclipse.che.api.factory.shared.dto.Author;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.factory.shared.dto.Policies;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 6/24/15.
 *
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ReferrerCheckerFilterTest {

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse res;

    @Mock
    private FilterChain chain;

    @Mock
    private Factory factory;

    @Mock
    private RequestDispatcher requestDispatcher;

    ReferrerCheckerFilter filter;

    @BeforeMethod
    public void setup() throws Exception {
        filter = new ReferrerCheckerFilter();
        when(req.getAttribute("factory")).thenReturn(factory);
        Field f = filter.getClass().getDeclaredField("INVALID_FACTORY_URL_PAGE");
        f.setAccessible(true);
        f.set(filter, "/resources/error-invalid-factory-url.jsp");
    }


    @Test
    public void shouldGetReferrerFromPoliciesIfV2_x() throws Exception {
        Policies policies = mock(Policies.class);
        when(factory.getPolicies()).thenReturn(policies);

        filter.doFilter(req, res, chain);

        verify(factory, atLeastOnce()).getPolicies();
        verify(policies, atLeastOnce()).getReferer();
    }

    @Test
    public void shouldThrowExceptionIfReferrerHostDiffersFromReferrerHeaderHost() throws Exception {
        when(req.getHeader("Referer")).thenReturn("http://facebook.com/index.php");
        Policies policies = mock(Policies.class);
        when(factory.getPolicies()).thenReturn(policies);
        when(policies.getReferer()).thenReturn("stackoverflow.com");
        Author creator = mock(Author.class);
        when(factory.getCreator()).thenReturn(creator);
        when(req.getRequestDispatcher(eq("/resources/error-invalid-factory-url.jsp"))).thenReturn(requestDispatcher);

        filter.doFilter(req, res, chain);

        verify(req).setAttribute(eq(RequestDispatcher.ERROR_MESSAGE), eq(FactoryConstants.ILLEGAL_HOSTNAME_MESSAGE));
        verify(requestDispatcher).forward(req, res);
    }

    @Test
    public void shouldThrowExceptionIfReferrerHeaderIsNullAndParameterIsNot() throws Exception {
        when(req.getHeader("Referer")).thenReturn(null);
        Policies policies = mock(Policies.class);
        when(factory.getPolicies()).thenReturn(policies);
        when(policies.getReferer()).thenReturn("stackoverflow.com");
        Author creator = mock(Author.class);
        when(factory.getCreator()).thenReturn(creator);
        when(req.getRequestDispatcher(eq("/resources/error-invalid-factory-url.jsp"))).thenReturn(requestDispatcher);

        filter.doFilter(req, res, chain);

        verify(req).setAttribute(eq(RequestDispatcher.ERROR_MESSAGE), eq(FactoryConstants.ILLEGAL_HOSTNAME_MESSAGE));
        verify(requestDispatcher).forward(req, res);
    }
}
