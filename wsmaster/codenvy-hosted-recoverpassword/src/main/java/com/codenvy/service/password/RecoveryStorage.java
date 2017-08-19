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
package com.codenvy.service.password;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/** @author Michail Kuznyetsov */
@Singleton
public class RecoveryStorage {
  private final Cache<String, String> storage;

  @Inject
  public RecoveryStorage(
      @Named("password.recovery.expiration_timeout_hours") long validationMaxAge) {
    if (validationMaxAge <= 0) {
      throw new IllegalArgumentException("Expiration timeout must not be less or equal 0");
    }
    this.storage =
        CacheBuilder.<String, String>newBuilder()
            .expireAfterAccess(validationMaxAge, TimeUnit.HOURS)
            .maximumSize(10000)
            .build();
  }

  /**
   * Verify that uuid has corresponding user email in the storage
   *
   * @param uuid unique identifier that points to user email in storage
   * @return true if there is user email, false otherwise
   */
  public boolean isValid(String uuid) {
    return storage.getIfPresent(uuid) != null;
  }

  /**
   * Put user email to storage, and return generated uuid for it.
   *
   * @param userEmail user email that needs to be stored
   * @return uuid related to this email
   */
  public String generateRecoverToken(String userEmail) {
    String uuid = UUID.randomUUID().toString();

    storage.put(uuid, userEmail);

    return uuid;
  }

  /**
   * Remove user email from storage by its uuid.
   *
   * @param uuid unique identifier of user email
   */
  public void remove(String uuid) {
    storage.invalidate(uuid);
  }

  /**
   * Get user email from storage by uuid.
   *
   * @param uuid unique identifier of user email
   * @return string with user name
   */
  public String get(String uuid) {
    return storage.getIfPresent(uuid);
  }
}
