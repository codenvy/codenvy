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
package com.codenvy.plugin.jenkins.webhooks.inject;

import com.codenvy.plugin.jenkins.webhooks.JenkinsConnectorFactory;
import com.codenvy.plugin.jenkins.webhooks.JenkinsWebhookService;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import org.eclipse.che.inject.DynaModule;

/**
 * Guice binding configurations for Jenkins webhooks module.
 *
 * @author Igor Vinokur
 */
@DynaModule
public class JenkinsWebhookModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JenkinsWebhookService.class);
        install(new FactoryModuleBuilder().build(JenkinsConnectorFactory.class));
    }
}
