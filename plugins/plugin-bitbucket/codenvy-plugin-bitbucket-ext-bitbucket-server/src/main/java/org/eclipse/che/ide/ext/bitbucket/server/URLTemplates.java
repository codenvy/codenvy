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

/**
 * Url templates for Bitbucket rest API.
 *
 * @author Igor Vinokur
 */
public interface URLTemplates {

  String repositoryUrl(String owner, String repositorySlug);

  String userUrl();

  String pullRequestUrl(String owner, String repositorySlug);

  String updatePullRequestUrl(String owner, String repositorySlug, int pullRequestId);

  String forksUrl(String owner, String repositorySlug);

  String forkRepositoryUrl(String owner, String repositorySlug);
}
