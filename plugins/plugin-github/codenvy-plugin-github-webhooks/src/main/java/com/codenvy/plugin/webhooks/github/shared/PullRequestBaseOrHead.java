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
package com.codenvy.plugin.webhooks.github.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface PullRequestBaseOrHead {
  /**
   * Get pull request head (or base) label.
   *
   * @return {@link String} label
   */
  String getLabel();

  void setLabel(String label);

  PullRequestBaseOrHead withLabel(String label);

  /**
   * Get pull request head (or base) ref.
   *
   * @return {@link String} ref
   */
  String getRef();

  void setRef(String ref);

  PullRequestBaseOrHead withRef(String ref);

  /**
   * Get pull request head (or base) sha.
   *
   * @return {@link String} sha
   */
  String getSha();

  void setSha(String sha);

  PullRequestBaseOrHead withSha(String sha);

  /**
   * Get pull request head (or base) repo.
   *
   * @return {@link Repository} repo
   */
  Repository getRepo();

  void setRepo(Repository repo);

  PullRequestBaseOrHead withRepo(Repository repo);
}
