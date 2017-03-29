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
package com.codenvy.auth.sso.server;

import com.codenvy.auth.sso.server.handler.BearerTokenAuthenticationHandler;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import org.eclipse.che.inject.DynaModule;

/**
 * Setup BearerTokenAuthenticationService  in guice container.
 *
 * @author Sergii Kabashniuk
 */
@DynaModule
public class BearerSecurityModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(com.codenvy.auth.sso.server.BearerTokenAuthenticationService.class);
        bind(BearerTokenAuthenticationHandler.class);
        bind(EmailValidator.class);
        bindConstant().annotatedWith(Names.named(EmailValidator.EMAIL_BLACKLIST_FILE)).to("cloud-ide-user-mail-blacklist.txt");
    }
}
