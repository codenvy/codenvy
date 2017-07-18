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