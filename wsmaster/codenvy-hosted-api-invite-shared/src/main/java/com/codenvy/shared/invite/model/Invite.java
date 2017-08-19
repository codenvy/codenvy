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
package com.codenvy.shared.invite.model;

import java.util.List;

/**
 * Represents permissions which will be belong to user after invitations accepting.
 *
 * @author Sergii Leschenko
 */
public interface Invite {
  /** Return user email. */
  String getEmail();

  /** Returns domain id. */
  String getDomainId();

  /** Returns instance id. */
  String getInstanceId();

  /** List of actions which user will be able to perform after accepting. */
  List<String> getActions();
}
