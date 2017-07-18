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
package com.codenvy.api.invite;

import com.codenvy.api.invite.email.EmailInviteSender;
import com.codenvy.api.invite.subscriber.InviteToPermissionsConverter;
import com.codenvy.api.invite.subscriber.RemoveInvitesBeforeOrganizationRemovedEventSubscriber;
import com.codenvy.api.invite.subscriber.RemoveInvitesBeforeWorkspaceRemovedEventSubscriber;
import com.google.inject.AbstractModule;

/**
 * @author Sergii Leschenko
 */
public class InviteApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(InviteService.class);
        bind(InviteServicePermissionsFilter.class);

        bind(RemoveInvitesBeforeOrganizationRemovedEventSubscriber.class).asEagerSingleton();
        bind(RemoveInvitesBeforeWorkspaceRemovedEventSubscriber.class).asEagerSingleton();

        bind(InviteToPermissionsConverter.class).asEagerSingleton();
        bind(EmailInviteSender.class).asEagerSingleton();
    }
}
