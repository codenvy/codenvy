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

import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;

/**
 * Normalizes identifier to be compatible with system.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class LdapUserIdNormalizer {

  private static final Pattern NOT_VALID_ID_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9-_]");

  private final String idAttributeName;

  @Inject
  public LdapUserIdNormalizer(@Named("ldap.sync.user.attr.id") String idAttributeName) {
    this.idAttributeName = idAttributeName;
  }

  /**
   * Normalizes user identifier by modifying a {@link LdapEntry}, does nothing if the entry doesn't
   * contain id attribute.
   *
   * @param entry the entry to normalize
   */
  public void normalize(@Nullable LdapEntry entry) {
    if (entry != null) {
      final LdapAttribute idAttr = entry.getAttribute(idAttributeName);
      if (idAttr != null) {
        final String normalizedId = normalize(idAttr.getStringValue());
        idAttr.clear();
        idAttr.addStringValue(normalizedId);
      }
    }
  }

  /**
   * Retrieves user identifier from the given {@code entry} and returns a normalized value of it.
   *
   * @param entry the entry to retrieve id from
   * @return normalized id value or null if {@code entry} is null or id is missing from entry
   */
  @Nullable
  public String retrieveAndNormalize(@Nullable LdapEntry entry) {
    if (entry == null) {
      return null;
    }
    final LdapAttribute idAttr = entry.getAttribute(idAttributeName);
    if (idAttr == null) {
      return null;
    }
    return normalize(idAttr.getStringValue());
  }

  /**
   * Removes all the characters different from <i>a-zA-Z0-9-_</i>.
   *
   * @param id identifier to normalize
   * @return normalized identifier or null if {@code id} is null
   */
  @Nullable
  public String normalize(@Nullable String id) {
    if (id == null) {
      return null;
    }
    return NOT_VALID_ID_CHARS_PATTERN.matcher(id).replaceAll("");
  }

  /** Returns the name of user id attribute. */
  public String getIdAttributeName() {
    return idAttributeName;
  }
}
