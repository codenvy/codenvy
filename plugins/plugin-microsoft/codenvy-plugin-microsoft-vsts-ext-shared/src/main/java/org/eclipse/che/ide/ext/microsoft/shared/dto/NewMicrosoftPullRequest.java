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
package org.eclipse.che.ide.ext.microsoft.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/** @author Mihail Kuznyetsov */
@DTO
public interface NewMicrosoftPullRequest {

  String getTitle();

  void setTitle(String title);

  NewMicrosoftPullRequest withTitle(String title);

  String getSourceRefName();

  void setSourceRefName(String sourceRefName);

  NewMicrosoftPullRequest withSourceRefName(String sourceRefName);

  String getTargetRefName();

  void setTargetRefName(String targetRefName);

  NewMicrosoftPullRequest withTargetRefName(String targetRefName);

  String getDescription();

  void setDescription(String description);

  NewMicrosoftPullRequest withDescription(String description);
}
