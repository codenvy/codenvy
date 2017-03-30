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
package com.codenvy.shared.invite.model;

import java.util.List;

/**
 * Represents permissions which will be belong to user after invitations accepting.
 *
 * @author Sergii Leschenko
 */
public interface Invite {
    /**
     * Return user email.
     */
    String getEmail();

    /**
     * Returns domain id.
     */
    String getDomainId();

    /**
     * Returns instance id.
     */
    String getInstanceId();

    /**
     * List of actions which user will be able to perform after accepting.
     */
    List<String> getActions();
}
