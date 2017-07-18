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
package com.codenvy.plugin.webhooks.github.inject;

import com.codenvy.plugin.webhooks.github.GitHubWebhookService;
import com.google.inject.AbstractModule;

import org.eclipse.che.inject.DynaModule;

/**
 * Guice binding for the GitHub webhook service
 *
 * @author Stephane Tournie
 */
@DynaModule
public class GitHubWebhookModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GitHubWebhookService.class);
    }
}
