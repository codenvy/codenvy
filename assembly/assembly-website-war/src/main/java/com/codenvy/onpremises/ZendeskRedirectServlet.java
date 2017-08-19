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
package com.codenvy.onpremises;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.user.shared.dto.ProfileDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Max Shaposhnik */
@Singleton
public class ZendeskRedirectServlet extends HttpServlet {
  @Inject
  @Named("che.api")
  private String apiEndpoint;

  @Inject
  @Named("zendesk.shared.key")
  @Nullable
  private String shared_key;

  @Inject
  @Named("zendesk.subdomain")
  @Nullable
  private String subdomain;

  private static final Logger LOG = LoggerFactory.getLogger(ZendeskRedirectServlet.class);

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    if (shared_key == null || subdomain == null)
      throw new ServletException("Zendesk is not configured.");
    // Given a user instance
    // Compose the JWT claims set
    JWTClaimsSet jwtClaims = new JWTClaimsSet();
    jwtClaims.setIssueTime(new Date());
    jwtClaims.setJWTID(UUID.randomUUID().toString());
    Subject subject = EnvironmentContext.getCurrent().getSubject();
    jwtClaims.setCustomClaim("name", getName());
    jwtClaims.setCustomClaim("email", subject.getUserName());
    // Create JWS header with HS256 algorithm
    JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
    JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaims.toJSONObject()));
    // Create HMAC signer
    JWSSigner signer = new MACSigner(shared_key.getBytes());
    try {
      jwsObject.sign(signer);
    } catch (JOSEException e) {
      String msg = String.format("Error signing JWT: %s", e.getMessage());
      LOG.warn(msg);
      response.sendError(500, msg);
    }
    // Serialise to JWT compact form
    String jwtString = jwsObject.serialize();
    String redirectUrl = "https://" + subdomain + ".zendesk.com/access/jwt?jwt=" + jwtString;
    response.sendRedirect(redirectUrl);
  }

  private String getName() {
    try {
      Link link =
          DtoFactory.getInstance()
              .createDto(Link.class)
              .withMethod("GET")
              .withHref(UriBuilder.fromUri(apiEndpoint).path("profile").build().toString());
      final ProfileDto profile = HttpJsonHelper.request(ProfileDto.class, link);

      String name = profile.getAttributes().get("firstName");
      String lastName = profile.getAttributes().get("lastName");

      if (null != lastName) {
        name = null != name ? name + " " + lastName : lastName;
      }
      return name;

    } catch (IOException
        | ServerException
        | UnauthorizedException
        | ForbiddenException
        | NotFoundException
        | ConflictException e) {
      LOG.warn(e.getLocalizedMessage());
    }
    return EnvironmentContext.getCurrent().getSubject().getUserId();
  }
}
