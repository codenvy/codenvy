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
package com.codenvy.ldap;

import com.google.inject.AbstractModule;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactory;

public class TestConnectionModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ConnectionFactory.class).toProvider(LdapConnectionFactoryProvider.class);
    bind(PooledConnectionFactory.class).toProvider(LdapConnectionFactoryProvider.class);
  }
}
