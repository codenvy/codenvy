/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
public class OrganizationPersistedEvent extends PersistEvent {
    private final Organization organization;

    public OrganizationPersistedEvent(Organization organization) {
        this.organization = organization;
    }

    public Organization getOrganization() {
        return organization;
    }
}
