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
package com.codenvy.plugin.jenkins.webhooks.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO object for describing Jenkins failed build event.
 *
 * @author Igor Vinokur
 */
@DTO
public interface JenkinsEventDto {
  String getJobName();

  void setJobName(String jobName);

  JenkinsEventDto withJobName(String jobName);

  int getBuildId();

  void setBuildId(int buildId);

  JenkinsEventDto withBuildId(int buildId);

  String getJenkinsUrl();

  void setJenkinsUrl(String jenkinsUrl);

  JenkinsEventDto withJenkinsUrl(String jenkinsUrl);

  String getRepositoryUrl();

  void setRepositoryUrl(String repositoryUrl);

  JenkinsEventDto withRepositoryUrl(String repositoryUrl);
}
