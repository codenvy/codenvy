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
public interface MicrosoftPullRequest {

  Integer getPullRequestId();

  void setPullRequestId(Integer pullRequestId);

  MicrosoftPullRequest withPullRequestId(Integer pullRequestId);

  MicrosoftRepository getRepository();

  void setRepository(MicrosoftRepository repository);

  MicrosoftPullRequest withRepository(MicrosoftRepository repository);

  String getStatus();

  void setStatus(String status);

  MicrosoftPullRequest withStatus(String status);

  String getSourceRefName();

  void setSourceRefName(String sourceRefName);

  MicrosoftPullRequest withSourceRefName(String sourceRefName);

  String getTargetRefName();

  void setTargetRefName(String targetRefName);

  MicrosoftPullRequest withTargetRefName(String targetRefName);

  String getUrl();

  void setUrl(String url);

  MicrosoftPullRequest withUrl(String url);

  String getHtmlUrl();

  void setHtmlUrl(String htmlUrl);

  MicrosoftPullRequest withHtmlUrl(String htmlUrl);

  String getDescription();

  void setDescription(String description);

  MicrosoftPullRequest withDescription(String description);
}
