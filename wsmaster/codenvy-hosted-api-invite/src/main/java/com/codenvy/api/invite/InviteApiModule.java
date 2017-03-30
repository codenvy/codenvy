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
