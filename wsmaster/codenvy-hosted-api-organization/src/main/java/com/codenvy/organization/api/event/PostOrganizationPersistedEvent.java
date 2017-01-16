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
package com.codenvy.organization.api.event;

import com.codenvy.organization.shared.model.Organization;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.core.db.cascade.event.PersistEvent;

/**
 * Published after organization instance is persisted.
 *
 * @author Sergii Leschenko
 */
@EventOrigin("organization")
public class PostOrganizationPersistedEvent extends PersistEvent {
    private final Organization organization;

    public PostOrganizationPersistedEvent(Organization organization) {
        this.organization = organization;
    }

    public Organization getOrganization() {
        return organization;
    }
}
