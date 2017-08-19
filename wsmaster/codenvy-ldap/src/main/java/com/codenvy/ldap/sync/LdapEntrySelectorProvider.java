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

import static java.lang.String.format;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;

/**
 * Chooses a strategy of ldap entries selection based on configuration properties.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class LdapEntrySelectorProvider implements Provider<LdapEntrySelector> {

  private static final int DEFAULT_PAGE_SIZE = 1000;
  private static final long DEFAULT_PAGE_READ_TIMEOUT = 30_000L;

  private final LdapEntrySelector selector;

  @Inject
  public LdapEntrySelectorProvider(
      @Named("ldap.base_dn") String baseDn,
      @Named("ldap.sync.user.filter") String usersFilter,
      @Named("ldap.sync.user.additional_dn") @Nullable String additionalUserDn,
      @Named("ldap.sync.group.filter") @Nullable String groupFilter,
      @Named("ldap.sync.group.additional_dn") @Nullable String additionalGroupDn,
      @Named("ldap.sync.group.attr.members") @Nullable String membersAttrName,
      @Named("ldap.sync.page.size") int pageSize,
      @Named("ldap.sync.page.read_timeout_ms") long pageReadTimeoutMs,
      @Named("ldap.sync.profile.attrs") @Nullable Pair<String, String>[] profileAttributes,
      @Named("ldap.sync.user.attr.id") String userIdAttr,
      @Named("ldap.sync.user.attr.name") String userNameAttr,
      @Named("ldap.sync.user.attr.email") String userEmailAttr) {
    if (groupFilter != null && membersAttrName == null) {
      throw new NullPointerException(
          format(
              "Value of 'ldap.group.filter' is set to '%s', which means that groups search "
                  + "is enabled that also requires 'ldap.group.attr.members' to be set",
              groupFilter));
    }

    // getting attribute names which should be synchronized
    final ArrayList<String> attrsList = new ArrayList<>();
    attrsList.add(userIdAttr);
    attrsList.add(userNameAttr);
    attrsList.add(userEmailAttr);
    if (profileAttributes != null) {
      for (Pair<String, String> profileAttribute : profileAttributes) {
        attrsList.add(profileAttribute.second);
      }
    }
    final String[] syncAttributes = attrsList.toArray(new String[attrsList.size()]);

    if (groupFilter == null) {
      selector =
          new LookupSelector(
              pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize,
              pageReadTimeoutMs <= 0 ? DEFAULT_PAGE_READ_TIMEOUT : pageReadTimeoutMs,
              normalizeDn(additionalUserDn, baseDn),
              usersFilter,
              syncAttributes);
    } else {
      selector =
          new MembershipSelector(
              normalizeDn(additionalGroupDn, baseDn),
              groupFilter,
              usersFilter,
              membersAttrName,
              syncAttributes);
    }
  }

  @Override
  public LdapEntrySelector get() {
    return selector;
  }

  private static String normalizeDn(String additionalDn, String baseDn) {
    if (additionalDn == null) {
      return baseDn;
    }
    return additionalDn + ',' + baseDn;
  }
}
