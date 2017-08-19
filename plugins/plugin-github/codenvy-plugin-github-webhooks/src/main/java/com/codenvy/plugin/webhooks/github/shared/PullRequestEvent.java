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
import org.eclipse.che.dto.shared.JsonFieldName;

@DTO
public interface PullRequestEvent {

  public String PULL_REQUEST_FIELD = "pull_request";

  String getAction();

  void setAction(String action);

  PullRequestEvent withAction(String action);

  int getNumber();

  void setNumber(int number);

  PullRequestEvent withNumber(int number);

  @JsonFieldName(PULL_REQUEST_FIELD)
  PullRequest getPullRequest();

  void setPullRequest(PullRequest pullRequest);

  PullRequestEvent withPullRequest(PullRequest pullRequest);
}
