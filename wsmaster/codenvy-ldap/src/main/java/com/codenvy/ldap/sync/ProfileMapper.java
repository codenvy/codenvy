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

import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;

/**
 * Maps {@link LdapEntry} to {@link ProfileImpl}.
 *
 * @author Yevhenii Voevodin
 */
public class ProfileMapper implements Function<LdapEntry, ProfileImpl> {

  /** App attribute name -> ldap attribute name . */
  private final ImmutableMap<String, String> appToLdapAttrNames;

  private final String idAttr;

  public ProfileMapper(String idAttr, Pair<String, String>[] attributes) {
    this.idAttr = idAttr;
    if (attributes == null) {
      this.appToLdapAttrNames = ImmutableMap.of();
    } else {
      this.appToLdapAttrNames =
          ImmutableMap.copyOf(
              Arrays.stream(attributes)
                  .filter(p -> !p.first.isEmpty() && p.second != null)
                  .collect(toMap(p -> p.first, p -> p.second.toLowerCase())));
    }
  }

  @Override
  public ProfileImpl apply(LdapEntry entry) {
    final ProfileImpl profile = new ProfileImpl();
    profile.setUserId(entry.getAttribute(idAttr).getStringValue());
    for (Map.Entry<String, String> attrMapping : appToLdapAttrNames.entrySet()) {
      final LdapAttribute ldapAttr = entry.getAttribute(attrMapping.getValue());
      if (ldapAttr != null) {
        profile.getAttributes().put(attrMapping.getKey(), ldapAttr.getStringValue());
      }
    }
    return profile;
  }
}
