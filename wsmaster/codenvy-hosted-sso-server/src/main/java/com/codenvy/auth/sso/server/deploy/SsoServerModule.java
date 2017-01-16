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
package com.codenvy.auth.sso.server.deploy;

import com.codenvy.auth.sso.server.ticket.InMemoryTicketManager;
import com.google.inject.AbstractModule;

/**
 * Install major sso server component in guice
 *
 * @author Sergii Kabashniuk
 */
public class SsoServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(com.codenvy.api.dao.authentication.TicketManager.class).to(InMemoryTicketManager.class);
        bind(com.codenvy.api.dao.authentication.TokenGenerator.class).to(com.codenvy.auth.sso.server.SecureRandomTokenGenerator.class);
        bind(com.codenvy.api.dao.authentication.CookieBuilder.class).to(com.codenvy.auth.sso.server.SsoCookieBuilder.class);
        bind(com.codenvy.auth.sso.server.SsoService.class);

        bind(com.codenvy.auth.sso.server.ticket.AccessTicketInvalidator.class);
        bind(com.codenvy.auth.sso.server.ticket.LogoutOnUserRemoveSubscriber.class).asEagerSingleton();
        bind(org.eclipse.che.api.auth.AuthenticationExceptionMapper.class);


    }
}