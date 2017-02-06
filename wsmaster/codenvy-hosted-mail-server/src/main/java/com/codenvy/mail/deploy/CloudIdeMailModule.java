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
package com.codenvy.mail.deploy;

import com.codenvy.mail.MailSender;
import com.google.inject.AbstractModule;

import org.eclipse.che.api.core.rest.CoreRestModule;
import org.eclipse.che.inject.DynaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice container configuration file. Replaces old REST application composers and servlet context listeners.
 */
@DynaModule
public class CloudIdeMailModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(CloudIdeMailModule.class);

    @Override
    protected void configure() {
        bind(MailSender.class);
        install(new CoreRestModule());
    }
}
