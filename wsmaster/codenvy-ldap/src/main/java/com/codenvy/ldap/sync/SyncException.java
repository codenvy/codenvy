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
package com.codenvy.ldap.sync;

import java.util.Iterator;
import org.ldaptive.LdapException;

/**
 * Thrown when any synchronization error occurred. Usually rethrows {@link LdapException} or another
 * exceptions occurred during operation executions, this exception extends {@link RuntimeException}
 * as it makes easier to create custom {@link Iterator} implementations. As the {@link
 * LdapSynchronizer} is independent component this exception is used for synchronizer only, its
 * instances are not published.
 *
 * @author Yevhenii Voevodin
 */
public class SyncException extends RuntimeException {

  public SyncException(String message) {
    super(message);
  }

  public SyncException(String message, Throwable cause) {
    super(message, cause);
  }
}
