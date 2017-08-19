/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.auth.sso.client;

import com.codenvy.auth.sso.server.SsoService;
import com.codenvy.auth.sso.shared.dto.SubjectDto;
import com.google.inject.name.Named;
import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Communicates with sso server by http calls.
 *
 * @author Sergii Kabashniuk
 */
public class HttpSsoServerClient implements ServerClient {
  private static final Logger LOG = LoggerFactory.getLogger(HttpSsoServerClient.class);

  protected final String apiEndpoint;
  protected final HttpJsonRequestFactory requestFactory;

  @Inject
  public HttpSsoServerClient(
      @Named("che.api") String apiEndpoint, HttpJsonRequestFactory requestFactory) {
    this.apiEndpoint = apiEndpoint;
    this.requestFactory = requestFactory;
  }

  @Override
  public Subject getSubject(String token, String clientUrl) {
    try {
      final HttpJsonRequest currentPrincipalRequest =
          requestFactory
              .fromUrl(
                  UriBuilder.fromUri(apiEndpoint)
                      .path(SsoService.class)
                      .path(SsoService.class, "getCurrentPrincipal")
                      .build(token)
                      .toString())
              .useGetMethod()
              .addQueryParam("clienturl", clientUrl);

      final SubjectDto subjectDto = currentPrincipalRequest.request().asDto(SubjectDto.class);
      return new SubjectImpl(
          subjectDto.getName(), subjectDto.getId(), subjectDto.getToken(), false);
    } catch (ApiException | IOException e) {
      LOG.warn(e.getLocalizedMessage());
    }
    return null;
  }

  @Override
  public void unregisterClient(String token, String clientUrl) {
    try {
      requestFactory
          .fromUrl(
              UriBuilder.fromUri(apiEndpoint)
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
