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
package com.codenvy.selenium.core.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.user.TestUserImpl;

@Singleton
public class OnpremTestUserImpl implements TestUser {

  private final TestUser testUser;

  @Inject
  public OnpremTestUserImpl(TestUserImpl testUser) throws Exception {
    this.testUser = testUser;
  }

  @Override
  public String getEmail() {
    return testUser.getEmail();
  }

  @Override
  public String getPassword() {
    return testUser.getPassword();
  }

  @Override
  public String getAuthToken() {
    return testUser.getAuthToken();
  }

  @Override
  public String getName() {
    return testUser.getName();
  }

  @Override
  public String getId() {
    return testUser.getId();
  }

  @Override
  public void delete() {
    testUser.delete();
  }
}
