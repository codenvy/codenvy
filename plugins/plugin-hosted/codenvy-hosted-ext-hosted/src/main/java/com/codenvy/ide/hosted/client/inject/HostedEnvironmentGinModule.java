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
package com.codenvy.ide.hosted.client.inject;

import com.codenvy.ide.hosted.client.informers.HostedEnvConnectionClosedInformer;
import com.codenvy.ide.hosted.client.login.PromptToLoginView;
import com.codenvy.ide.hosted.client.login.PromptToLoginViewImpl;
import com.codenvy.ide.hosted.client.notifier.BadConnectionNotifierView;
import com.codenvy.ide.hosted.client.notifier.BadConnectionNotifierViewImpl;
import com.google.gwt.inject.client.AbstractGinModule;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.client.ConnectionClosedInformerImpl;

/**
 * @author Vitaly Parfonov
 */
@ExtensionGinModule
public class HostedEnvironmentGinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(ConnectionClosedInformerImpl.class).to(HostedEnvConnectionClosedInformer.class).in(javax.inject.Singleton.class);
        bind(PromptToLoginView.class).to(PromptToLoginViewImpl.class);
        bind(BadConnectionNotifierView.class).to(BadConnectionNotifierViewImpl.class);
    }
}
