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
public interface RepositoryLinks {

  /**
   * Get self link.
   *
   * @return {@link Link} self
   */
  Link getSelf();

  void setSelf(final Link self);

  RepositoryLinks withSelf(final Link self);

  /**
   * Get project link.
   *
   * @return {@link Link} project
   */
  Link getProject();

  void setProject(final Link project);

  RepositoryLinks withProject(final Link project);

  /**
   * Get web link.
   *
   * @return {@link Link} web
   */
  Link getWeb();

  void setWeb(final Link web);

  RepositoryLinks withWeb(final Link web);

  /**
   * Get commits link.
   *
   * @return {@link Link} commits
   */
  Link getCommits();

  void setCommits(final Link commits);

  RepositoryLinks withCommits(final Link commits);

  /**
   * Get refs link.
   *
   * @return {@link Link} refs
   */
  Link getRefs();

  void setRefs(final Link refs);

  RepositoryLinks withRefs(final Link refs);

  /**
   * Get pullRequests link.
   *
   * @return {@link Link} self
   */
  Link getPullRequests();

  void setPullRequests(final Link pullRequests);

  RepositoryLinks withPullRequests(final Link pullRequests);

  /**
   * Get items link.
   *
   * @return {@link Link} items
   */
  Link getItems();

  void setItems(final Link items);

  RepositoryLinks withItems(final Link items);

  /**
   * Get pushes link.
   *
   * @return {@link Link} pushes
   */
  Link getPushes();

  void setPushes(final Link pushes);

  RepositoryLinks withPushes(final Link pushes);
}
