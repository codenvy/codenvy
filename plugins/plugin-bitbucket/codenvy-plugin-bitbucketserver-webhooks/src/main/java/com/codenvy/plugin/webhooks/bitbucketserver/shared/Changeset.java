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
 * Represents a single change-set object from the list of pushed changes.
 *
 * @author Igor Vinokur
 */
@DTO
public interface Changeset {
  /** Returns {@link Commit} object of the new head commit. */
  Commit getToCommit();

  void setToCommit(Commit toToCommit);

  Changeset withToCommit(Commit toToCommit);

  /** Returns {@link Commit} object of the head commit before the change. */
  Commit getFromCommit();

  void setFromCommit(Commit toToCommit);

  Changeset withFromCommit(Commit toToCommit);
}
