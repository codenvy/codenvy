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
package com.codenvy.api.dao.authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs validation of user country by blacklist and blocks login if matched.
 *
 * @author Max Shaposhnik
 */
public class PassportValidator {

  private static final Logger LOG = LoggerFactory.getLogger(PassportValidator.class);

  private ProfileDao profileDao;
  private List<String> blockedList;

  @Inject
  public PassportValidator(
      ProfileDao profileDao, @Nullable @Named("auth.blocked_country_names") String[] blockedList) {
    this.profileDao = profileDao;
    if (blockedList == null || blockedList.length == 0) {
      this.blockedList = new ArrayList<>();
    } else {
      this.blockedList = Arrays.asList(blockedList);
    }
  }

  public void validate(String userId) throws AuthenticationException {
    try {
      final Profile profile = profileDao.getById(userId);
      final String country = profile.getAttributes().get("country");
      if (country != null && blockedList.stream().anyMatch(country::equalsIgnoreCase)) {
        throw new AuthenticationException("Authentication failed. Please contact support.");
      }
    } catch (ServerException | NotFoundException e) {
      LOG.warn("Unable to validate user's passport.", e);
    }
  }
}
