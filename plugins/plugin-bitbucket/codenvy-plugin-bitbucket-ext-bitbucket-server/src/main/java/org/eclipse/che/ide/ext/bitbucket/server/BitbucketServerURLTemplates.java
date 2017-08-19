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
package org.eclipse.che.ide.ext.bitbucket.server;

import static java.lang.String.format;

/**
 * Defines URL templates for BitBucket Server.
 *
 * @author Igor Vinokur
 */
public class BitbucketServerURLTemplates implements URLTemplates {

  private static final String REPOSITORY = "projects/%s/repos/%s";

  private final String restUrl;

  BitbucketServerURLTemplates(String hostUrl) {
    this.restUrl = hostUrl + "/rest/api/latest/";
  }

  @Override
  public String repositoryUrl(String owner, String repositorySlug) {
    return format(restUrl + REPOSITORY, owner, repositorySlug);
  }

  @Override
  public String userUrl() {
    return restUrl + "users/";
  }

  @Override
  public String pullRequestUrl(String owner, String repositorySlug) {
    return format(restUrl + REPOSITORY + "/pull-requests", owner, repositorySlug);
  }

  @Override
  public String updatePullRequestUrl(String owner, String repositorySlug, int pullRequestId) {
    return format(restUrl + REPOSITORY + "/pull-requests/%d", owner, repositorySlug, pullRequestId);
  }

  @Override
  public String forksUrl(String owner, String repositorySlug) {
    return format(restUrl + REPOSITORY + "/forks", owner, repositorySlug);
  }

  @Override
  public String forkRepositoryUrl(String owner, String repositorySlug) {
    return format(restUrl + REPOSITORY + "/pull-requests", owner, repositorySlug);
  }
}
