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
package org.eclipse.che.security.oauth;

import org.eclipse.che.security.oauth.shared.User;

/** @author Max Shaposhnik */
public class MicrosoftUser implements User {

  private String email;
  private String name;

  @Override
  public final String getId() {
    return email;
  }

  @Override
  public final void setId(String id) {
    // JSON response from API contains key 'id' but it has different purpose.
    // Ignore calls of this method. Email address is used as user identifier.
  }

  @Override
  public String getEmail() {
    return email;
  }

  @Override
  public void setEmail(String email) {
    setId(email);
    this.email = email;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }
}
