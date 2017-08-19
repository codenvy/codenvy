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
public interface Project {

  /**
   * Get project id.
   *
   * @return {@link String} id
   */
  String getId();

  void setId(final String id);

  Project withId(final String id);

  /**
   * Get project name.
   *
   * @return {@link String} name
   */
  String getName();

  void setName(final String name);

  Project withName(final String name);

  /**
   * Get project url.
   *
   * @return {@link String} url
   */
  String getUrl();

  void setUrl(final String url);

  Project withUrl(final String url);

  /**
   * Get project state.
   *
   * @return {@link String} state
   */
  String getState();

  void setState(final String state);

  Project withState(final String state);
}
