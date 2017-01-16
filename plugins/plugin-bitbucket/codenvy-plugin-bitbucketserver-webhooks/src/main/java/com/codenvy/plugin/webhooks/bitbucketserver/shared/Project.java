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
 * Represents a Bitbucket Server project.
 *
 * @author Igor Vinokur
 */
@DTO
public interface Project {

    /**
     * Returns project's key.
     */
    String getKey();

    void setKey(String key);

    Project withKey(String key);

    /**
     * Returns {@link User} object that represent's project owner.
     */
    User getOwner();

    void setOwner(User owner);

    Project withOwner(User owner);
}
