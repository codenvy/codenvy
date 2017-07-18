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
package com.codenvy.plugin.webhooks.vsts.inject;

import com.codenvy.plugin.webhooks.vsts.VSTSWebhookService;
import com.google.inject.AbstractModule;

import org.eclipse.che.inject.DynaModule;

/**
 * Guice binding for the VSTS webhook service
 *
 * @author Stephane Tournie
 */
@DynaModule
public class VSTSWebhookModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(VSTSWebhookService.class);
    }
}
