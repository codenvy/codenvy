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
public interface Commit {

  /**
   * Get commit id.
   *
   * @return {@link String} commitId
   */
  String getCommitId();

  void setCommitId(final String commitId);

  Commit withCommitId(final String commitId);

  /**
   * Get commit url.
   *
   * @return {@link String} url
   */
  String getUrl();

  void setUrl(final String url);

  Commit withUrl(final String url);
}
