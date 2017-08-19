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
 * Represents reference change.
 *
 * @author Igor Vinokur
 */
@DTO
public interface RefChange {
  /** Returns reference Id. */
  String getRefId();

  void setRefId(String refId);

  RefChange withRefId(String refId);

  /** Returns hash of the head commit before the change was performed. */
  String getFromHash();

  void setFromHash(String fromHash);

  /** Returns hash of the head commit after the change was performed. */
  String getToHash();

  void setToHash(String toHash);

  RefChange withToHash(String toHash);

  /** Returns reference change type. */
  String getType();

  void setType(String type);

  RefChange withType(String type);
}
