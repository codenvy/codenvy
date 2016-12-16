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
package com.codenvy.api.license;

import com.codenvy.api.license.server.SystemLicenseManager;
import com.codenvy.api.license.server.SystemLicenseService;
import com.codenvy.api.license.shared.dto.LegalityDto;
import com.codenvy.api.permission.server.SystemDomain;
import com.codenvy.auth.sso.client.filter.RequestFilter;
import com.codenvy.auth.sso.client.filter.UriStartFromRequestFilter;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.PrintWriter;

import static com.codenvy.api.license.shared.model.Issue.Status.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED;

/**
 * Checks system license conditions for login. If fair source license isn't accepted, then:
 * 1) if ("no.user.interaction" property == false),
 *     then if (user has permission to perform MANAGE_SYSTEM_ACTION): send redirection to accept-fair-source-license page in response;
 *          otherwise: send redirection to fair-source-license-is-not-accepted-error page in response;
 * 2) if ("no.user.interaction" property == true),
 *     then return error 403 FORBIDDEN.
 * @author Dmytro Nochevnov
 */
@Singleton
public class SystemLicenseLoginFilter implements Filter {
    public static final String NO_USER_INTERACTION                                = "no.user.interaction";
    public static final String ACCEPT_FAIR_SOURCE_LICENSE_PAGE_URL                = "license.system.accept_fair_source_license_page_url";
    public static final String FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_ERROR_PAGE_URL = "license.system.fair_source_license_is_not_accepted_error_page_url";

    public static final ImmutableList<String> URIS_TO_SKIP = ImmutableList.of("/api/permissions",
                                                                              "/api/user/settings",
                                                                              "/api/license/system/legality",
                                                                              "/api/license/system/fair-source-license");

    @Inject
    protected RequestFilter          requestFilter;
    @Inject
    protected HttpJsonRequestFactory requestFactory;
    @Inject
    @Named("che.api")
    protected String                 apiEndpoint;
    @Inject()
    @Named(NO_USER_INTERACTION)
    protected boolean                noUserInteraction;
    @Inject(optional = true)
    @Named(ACCEPT_FAIR_SOURCE_LICENSE_PAGE_URL)
    protected String                 acceptFairSourceLicensePageUrl;
    @Inject(optional = true)
    @Named(FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_ERROR_PAGE_URL)
    protected String                 fairSourceLicenseIsNotAcceptedErrorPageUrl;


    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest)request;
        final HttpServletResponse httpResponse = (HttpServletResponse)response;
        if (shouldSkip(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            if (isFairSourceLicenseNotAccepted()) {
                if (noUserInteraction) {
                    createForbiddenAccessResponse(httpResponse);
                    return;
                }

                if (isAdmin()) {
                    sendRedirection(httpResponse, acceptFairSourceLicensePageUrl);
                } else {
                    sendRedirection(httpResponse, fairSourceLicenseIsNotAcceptedErrorPageUrl);
                }
            } else {
                chain.doFilter(request, response);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private boolean shouldSkip(HttpServletRequest request) {
        return requestFilter.shouldSkip(request) || new UriStartFromRequestFilter(URIS_TO_SKIP).shouldSkip(request);
    }

    private void createForbiddenAccessResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(DtoFactory.getInstance()
                                   .toJson(new UnauthorizedException(SystemLicenseManager.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE).getServiceError()));
        }
    }

    private boolean isAdmin() {
        return EnvironmentContext.getCurrent().getSubject().hasPermission(SystemDomain.DOMAIN_ID, null, SystemDomain.MANAGE_SYSTEM_ACTION);
    }

    private void sendRedirection(HttpServletResponse response, String url) throws IOException {
        UriBuilder redirectUrl = UriBuilder.fromPath(url);
        response.sendRedirect(redirectUrl.build().toString());
    }

    private boolean isFairSourceLicenseNotAccepted() throws
                                                     ForbiddenException,
                                                     BadRequestException, IOException,
                                                     ConflictException,
                                                     NotFoundException,
                                                     ServerException, UnauthorizedException {
        LegalityDto legality = requestFactory.fromUrl(UriBuilder.fromUri(apiEndpoint)
                                                                .path(SystemLicenseService.class)
                                                                .path(SystemLicenseService.class,
                                                                      "isSystemUsageLegal").build().toString())
                                             .useGetMethod()
                                             .request()
                                             .asDto(LegalityDto.class);

        return legality.getIssues().stream().anyMatch(issue -> issue.getStatus().equals(FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED));
    }

    @Override
    public void destroy() {
    }
}
