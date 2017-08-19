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
package com.codenvy.plugin.webhooks.bitbucketserver.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents a Bitbucket Server repository.
 *
 * @author Igor Vinokur
 */
@DTO
public interface Repository {
  /** Returns the repository's slug. */
  String getSlug();

  void setSlug(String slug);

  Repository withSlug(String slug);

  /** Returns the repository's name. */
  String getName();

  void setName(String name);

  Repository withName(String name);

  /** Returns {@link Project} object of the repository's project. */
  Project getProject();

  void setProject(Project project);

  Repository withProject(Project project);
}
