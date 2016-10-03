/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.organization.shared.model;

import java.util.List;

/**
 * Describes relations of user and organization
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 */
public interface Member {
    /**
     * Returns id of user
     */
    String getUserId();

    /**
     * Returns id of organization
     */
    String getOrganizationId();

    /**
     * Returns list of actions that user can perform in organization
     */
    List<String> getActions();
}
