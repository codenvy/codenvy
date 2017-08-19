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
package org.eclipse.che.ide.ext.bitbucket.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents a Bitbucket user.
 *
 * @author Kevin Pollet
 */
@DTO
public interface BitbucketUser {
  String getUsername();

  void setUsername(String username);

  BitbucketUser withUsername(String username);

  String getDisplayName();

  void setDisplayName(String displayName);

  BitbucketUser withDisplayName(String displayName);

  String getUuid();

  void setUuid(String uuid);

  BitbucketUser withUuid(String uuid);

  BitbucketUserLinks getLinks();

  void setLinks(BitbucketUserLinks links);

  BitbucketUser withLinks(BitbucketUserLinks links);

  @DTO
  interface BitbucketUserLinks {
    BitbucketLink getSelf();

    void setSelf(BitbucketLink self);

    BitbucketUserLinks withSelf(BitbucketLink self);
  }
}
