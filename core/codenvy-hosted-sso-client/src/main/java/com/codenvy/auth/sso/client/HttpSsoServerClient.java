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
package com.codenvy.auth.sso.client;

import com.codenvy.auth.sso.server.SsoService;
import com.codenvy.auth.sso.shared.dto.SubjectDto;
import com.google.inject.name.Named;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

/**
 * Communicates with sso server by http calls.
 *
 * @author Sergii Kabashniuk
 */
public class HttpSsoServerClient implements ServerClient {
    private static final Logger LOG = LoggerFactory.getLogger(HttpSsoServerClient.class);

    private final String                 apiEndpoint;
    private final HttpJsonRequestFactory requestFactory;

    @Inject
    public HttpSsoServerClient(@Named("api.endpoint") String apiEndpoint,
                               HttpJsonRequestFactory requestFactory) {
        this.apiEndpoint = apiEndpoint;
        this.requestFactory = requestFactory;
    }

    @Override
    public Subject getSubject(String token, String clientUrl, String workspaceId, String accountId) {
        try {
            final HttpJsonRequest currentPrincipalRequest = requestFactory.fromUrl(UriBuilder.fromUri(apiEndpoint)
                                                                                             .path(SsoService.class)
                                                                                             .path(SsoService.class, "getCurrentPrincipal")
                                                                                             .build(token)
                                                                                             .toString())
                                                                          .useGetMethod()
                                                                          .addQueryParam("clienturl", clientUrl);

            if (workspaceId != null) {
                currentPrincipalRequest.addQueryParam("workspaceid", workspaceId);
            }

            if (accountId != null) {
                currentPrincipalRequest.addQueryParam("accountid", accountId);
            }

            final SubjectDto subjectDto = currentPrincipalRequest.request()
                                                           .asDto(SubjectDto.class);
            return new SubjectImpl(subjectDto.getName(), subjectDto.getId(), subjectDto.getToken(), subjectDto.getRoles(), subjectDto.isTemporary());
        } catch (ApiException | IOException e) {
            LOG.warn(e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public void unregisterClient(String token, String clientUrl) {
        try {
            requestFactory.fromUrl(UriBuilder.fromUri(apiEndpoint)
                                             .path(SsoService.class)
                                             .path(SsoService.class, "unregisterToken")
                                             .build(token)
                                             .toString())
                          .useDeleteMethod()
                          .addQueryParam("clienturl", clientUrl)
                          .request();
        } catch (ApiException | IOException e) {
            LOG.warn(e.getLocalizedMessage());
        }
    }
}
