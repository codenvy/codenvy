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

import com.codenvy.machine.authentication.server.MachineTokenRegistry;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves master {@link Subject} based on the machine token Machine token detection is simple and
 * based on the machine token prefix, so if token is prefixed with 'machine' then the mechanism is
 * triggered otherwise method call delegated to the super {@link
 * HttpSsoServerClient#getSubject(String, String)}.
 *
 * <p>Note that this component must be deployed to api war only.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class MachineSsoServerClient extends HttpSsoServerClient {
  private static final Logger LOG = LoggerFactory.getLogger(MachineSsoServerClient.class);

  private final MachineTokenRegistry tokenRegistry;
  private final UserManager userManager;

  @Inject
  public MachineSsoServerClient(
      @Named("che.api") String apiEndpoint,
      HttpJsonRequestFactory requestFactory,
      MachineTokenRegistry tokenRegistry,
      UserManager userManager) {
    super(apiEndpoint, requestFactory);
    this.tokenRegistry = tokenRegistry;
    this.userManager = userManager;
  }

  @Override
  public Subject getSubject(String token, String clientUrl) {
    if (!token.startsWith("machine")) {
      return super.getSubject(token, clientUrl);
    }
    try {
      final User user = userManager.getById(tokenRegistry.getUserId(token));
      return new SubjectImpl(user.getName(), user.getId(), token, false);
    } catch (ApiException x) {
      LOG.warn(x.getLocalizedMessage(), x);
    }
    return null;
  }
}
