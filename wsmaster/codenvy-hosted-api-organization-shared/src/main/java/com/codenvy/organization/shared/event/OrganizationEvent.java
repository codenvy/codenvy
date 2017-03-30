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

import com.codenvy.organization.shared.model.Organization;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * The base interface for organization event.
 *
 * @author Anton Korneta
 */
public interface OrganizationEvent {

    /**
     * Returns organization related to this event.
     */
    Organization getOrganization();

    /**
     * Returns type of this event.
     */
    EventType getType();

    /**
     * Returns name of user who acted with organization or null if user is undefined.
     */
    @Nullable
    String getInitiator();
}
