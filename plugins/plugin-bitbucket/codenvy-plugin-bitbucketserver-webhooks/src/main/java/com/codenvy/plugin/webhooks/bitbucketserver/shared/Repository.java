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
package com.codenvy.plugin.webhooks.bitbucketserver.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents a Bitbucket Server repository.
 *
 * @author Igor Vinokur
 */
@DTO
public interface Repository {
    /**
     * Returns the repository name.
     */
    String getName();

    void setName(String name);

    Repository withName(String name);

    /**
     * Returns {@link Project} object of the repository's project.
     */
    Project getProject();

    void setProject(Project project);

    Repository withProject(Project project);
}
