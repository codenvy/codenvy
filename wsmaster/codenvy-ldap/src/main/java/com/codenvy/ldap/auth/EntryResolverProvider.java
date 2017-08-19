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
package com.codenvy.ldap.auth;

import com.google.common.base.Strings;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.ldaptive.ad.handler.ObjectGuidHandler;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.auth.PooledSearchEntryResolver;
import org.ldaptive.pool.PooledConnectionFactory;

/**
 * Provider of EntryResolver
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class EntryResolverProvider implements Provider<EntryResolver> {

  private final PooledSearchEntryResolver entryResolver;

  @Inject
  public EntryResolverProvider(
      PooledConnectionFactory connFactory,
      @NotNull @Named("ldap.base_dn") String baseDn,
      @Nullable @Named("ldap.auth.user.filter") String userFilter,
      @Nullable @Named("ldap.auth.subtree_search") String subtreeSearch) {
    this.entryResolver = new PooledSearchEntryResolver();
    this.entryResolver.setBaseDn(baseDn);
    this.entryResolver.setUserFilter(userFilter);
    this.entryResolver.setSubtreeSearch(
        Strings.isNullOrEmpty(subtreeSearch) ? false : Boolean.valueOf(subtreeSearch));
    this.entryResolver.setConnectionFactory(connFactory);
    this.entryResolver.setSearchEntryHandlers(new ObjectGuidHandler());
  }

  @Override
  public EntryResolver get() {
    return entryResolver;
  }
}
