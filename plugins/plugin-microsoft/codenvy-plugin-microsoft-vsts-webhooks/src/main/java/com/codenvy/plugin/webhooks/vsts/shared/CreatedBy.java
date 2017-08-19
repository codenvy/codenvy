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
package com.codenvy.plugin.webhooks.vsts.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface CreatedBy {

  /**
   * Get createdby id.
   *
   * @return {@link String} id
   */
  String getId();

  void setId(final String id);

  CreatedBy withId(final String id);

  /**
   * Get createdby displayName.
   *
   * @return {@link String} displayName
   */
  String getDisplayName();

  void setDisplayName(final String displayName);

  CreatedBy withDisplayName(final String displayName);

  /**
   * Get createdby uniqueName.
   *
   * @return {@link String} uniqueName
   */
  String getUniqueName();

  void setUniqueName(final String uniqueName);

  CreatedBy withUniqueName(final String uniqueName);

  /**
   * Get createdby url.
   *
   * @return {@link String} url
   */
  String getUrl();

  void setUrl(final String url);

  CreatedBy withUrl(final String url);

  /**
   * Get createdby image url.
   *
   * @return {@link String} imageUrl
   */
  String getImageUrl();

  void setImageUrl(final String imageUrl);

  CreatedBy withImageUrl(final String imageUrl);
}
