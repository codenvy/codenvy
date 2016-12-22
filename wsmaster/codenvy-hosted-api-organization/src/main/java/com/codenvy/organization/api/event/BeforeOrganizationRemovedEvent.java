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

import com.codenvy.organization.spi.impl.OrganizationImpl;

import org.eclipse.che.core.db.cascade.event.RemoveEvent;

/**
 * Published before {@link OrganizationImpl organization} removed.
 *
 * @author Sergii Leschenko
 */
public class BeforeOrganizationRemovedEvent extends RemoveEvent {

    private final OrganizationImpl organization;

    public BeforeOrganizationRemovedEvent(OrganizationImpl organization) {
        this.organization = organization;
    }

    /** Returns organization which is going to be removed. */
    public OrganizationImpl getOrganization() {
        return organization;
    }
}
