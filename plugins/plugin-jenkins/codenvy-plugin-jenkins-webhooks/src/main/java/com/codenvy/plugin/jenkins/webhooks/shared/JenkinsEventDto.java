/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
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
