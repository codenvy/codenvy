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
package com.codenvy.plugin.webhooks;

public enum FactoryType {
  DEVELOP("develop"),
  REVIEW("review");

  private final String name;

  FactoryType(final String name) {
    this.name = name;
  }

  public String toString() {
    return this.name;
  }
}
