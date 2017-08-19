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
public interface Relation {

  /**
   * Get relation type.
   *
   * @return {@link String} rel
   */
  String getRel();

  void setRel(final String rel);

  Relation withRel(final String rel);

  /**
   * Get relation url.
   *
   * @return {@link String} url
   */
  String getUrl();

  void setUrl(final String url);

  Relation withUrl(final String url);

  /**
   * Get relation attributes.
   *
   * @return {@link Attributes} attributes
   */
  Attributes getAttributes();

  void setAttributes(final Attributes attributes);

  Relation withAttributes(final Attributes attributes);
}
