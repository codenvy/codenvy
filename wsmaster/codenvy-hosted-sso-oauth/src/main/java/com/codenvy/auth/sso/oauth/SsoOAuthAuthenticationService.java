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
package com.codenvy.auth.sso.oauth;

import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.security.oauth.OAuthAuthenticationException;
import org.eclipse.che.security.oauth.OAuthAuthenticationService;
import org.eclipse.che.security.oauth.OAuthAuthenticator;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.eclipse.che.commons.lang.UrlUtils.getParameter;
import static org.eclipse.che.commons.lang.UrlUtils.getQueryParametersFromState;
import static org.eclipse.che.commons.lang.UrlUtils.getRequestUrl;
import static org.eclipse.che.commons.lang.UrlUtils.getState;

/**
 * RESTful wrapper for OAuthAuthenticator.
 *
 * @author Sergii Kabashniuk
 */
@Path("oauth")
public class SsoOAuthAuthenticationService extends OAuthAuthenticationService {
    @Inject
    BearerTokenAuthenticationHandler authenticationHandler;

    @GET
    @Path("callback")
    @Override
    public Response callback(@QueryParam("errorValues") List<String> errorValues) throws OAuthAuthenticationException, BadRequestException {
        URL requestUrl = getRequestUrl(uriInfo);
        Map<String, List<String>> params = getQueryParametersFromState(getState(requestUrl));
        if (errorValues != null && errorValues.contains("access_denied")) {
            return Response.temporaryRedirect(
                    uriInfo.getRequestUriBuilder().replacePath(errorPage).replaceQuery(null).build()).build();
        }
        final String providerName = getParameter(params, "oauth_provider");
        OAuthAuthenticator oauth = getAuthenticator(providerName);
        final List<String> scopes = params.get("scope");
        final String userId = oauth.callback(requestUrl, scopes == null ? emptyList() : scopes);
        String redirectAfterLogin = getParameter(params, "redirect_after_login");
        try {
            redirectAfterLogin = URLDecoder.decode(redirectAfterLogin, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new OAuthAuthenticationException(e.getLocalizedMessage(), e);
        }
        final String mode = getParameter(params, "mode");
        //federated_login left for backward compatibility
        if ("sso".equals(mode) || "federated_login".equals(mode)) {
            Map<String, String> payload = new HashMap<>();
            payload.put("provider", providerName);

            return Response.temporaryRedirect(
                    UriBuilder.fromUri(redirectAfterLogin)
                              .queryParam("email", userId)
                              .queryParam("oauth_provider", providerName)
                              .queryParam("oauthbearertoken", authenticationHandler.generateBearerToken(userId, userId, payload))
                              .build())
                           .build();

        }
        return Response.temporaryRedirect(URI.create(redirectAfterLogin)).build();
    }

}
