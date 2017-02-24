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
package com.codenvy.organization.shared.event;

/**
 * Defines organizations event types.
 *
 * @author Anton Korneta
 */
public enum EventType {

    /**
     * Published when organization name changed.
     */
    ORGANIZATION_RENAMED,

    /**
     * Published when organization removed.
     */
    ORGANIZATION_REMOVED,

    /**
     * Published when new member added to organization.
     */
    MEMBER_ADDED,

    /**
     * Published when member removed from organization.
     */
    MEMBER_REMOVED

}
