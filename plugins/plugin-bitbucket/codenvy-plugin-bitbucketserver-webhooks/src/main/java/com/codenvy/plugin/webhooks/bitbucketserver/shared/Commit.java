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
 * Represents a single commit from push.
 *
 * @author Igor Vinokur
 */
@DTO
public interface Commit {

    /**
     * Returns Id of the commit.
     */
    String getId();

    void setId(String id);

    Commit withId(String id);

    /**
     * Returns message of the commit.
     */
    String getMessage();

    void setMessage(String message);

    Commit withMessage(String message);
}
