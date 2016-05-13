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

import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.factory.shared.dto.Policies;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.everrest.core.impl.RuntimeDelegateImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.RuntimeDelegate;
import java.security.Principal;
import java.util.Collections;

import static org.eclipse.che.api.core.rest.HttpJsonHelper.HttpJsonHelperImpl;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Test of {@link com.codenvy.onpremises.factory.FactoryServlet} functionality. */
@Listeners(value = {MockitoTestNGListener.class})
public class FactoryServletTest {

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse res;

    @Mock
    private Subject subject;

    @Mock
    private Principal principal;

    @Mock
    private ServletConfig servletConfig;

    @Mock
    private HttpJsonHelperImpl httpJsonHelper;

    @Mock
    private ServletContext servletContext;

    @Mock
    private RequestDispatcher requestDispatcher;

    private FactoryServlet servlet;

    @BeforeMethod
    public void setup() throws Exception {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
        this.servlet = new FactoryServlet("/resources/error-factory-creation.html");

        String requestUrl = "http://codenvy.com/factory";
        when(req.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
        EnvironmentContext.getCurrent().setSubject(subject);
        when(subject.getUserId()).thenReturn("userId");
    }

    @Test(dataProvider = "validFactoryProvider")
    public void shouldBeAbleToAcceptFactory(String host, String referrer, Factory factory) throws Exception {
        when(req.getAttribute("factory")).thenReturn(factory);
        when(req.getServerName()).thenReturn(host);
        when(req.getHeader("Referer")).thenReturn(referrer);
        when(req.getAttribute("factory")).thenReturn(factory);

        servlet.doGet(req, res);

        verify(res).sendRedirect(contains("/dashboard/#load-factory/" + factory.getId()));
    }

    @DataProvider(name = "validFactoryProvider")
    public Object[][] validFactoryProvider() {
        return new Object[][] {
                // encoded
                {"codenvy.com", null, prepareFactory()},
                // referrer hostname is equal
                {"codenvy.com",
                 "http://stackoverflow.com/some/page",
                 prepareFactory().withPolicies(newDto(Policies.class).withReferer("stackoverflow.com"))},
                // referrer is relative
                {"codenvy.com",
                 "/some/page/at/codenvy",
                 prepareFactory().withPolicies(newDto(Policies.class).withReferer("codenvy.com"))}
        };
    }


    @DataProvider(name = "FactoryTypeProvider")
    public Object[][] factoryTypeProvider() {
        return new Object[][] {
                // default params
                {"codenvy.com", null, prepareFactory()},
        };
    }

    private Factory prepareFactory() {
        return newDto(Factory.class)
                .withV("4.0")
                .withId("ygfskjsjdaqws")
                .withWorkspace(newDto(WorkspaceConfigDto.class)
                                       .withProjects(Collections.singletonList(newDto(
                                               ProjectConfigDto.class)
                                                                                       .withSource(
                                                                                               newDto(
                                                                                                       SourceStorageDto.class)
                                                                                                       .withType(
                                                                                                               "git")
                                                                                                       .withLocation(
                                                                                                               "https://github.com/codenvy/test.git")))));
    }
}
