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
package com.codenvy.api.workspace.server.model;


import java.util.List;

/**
 * Describes relations between user and workspace
 *
 * @author Sergii Leschenko
 */
public interface Worker {
    /**
     * Returns user id
     */
    String getUserId();

    /**
     * Returns workspace id
     */
    String getWorkspaceId();

    /**
     * Returns list of workspace actions which can be performed by current user
     */
    List<String> getActions();
}
