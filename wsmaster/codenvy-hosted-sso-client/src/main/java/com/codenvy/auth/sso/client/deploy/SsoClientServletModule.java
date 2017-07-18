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
package com.codenvy.auth.sso.client.deploy;

import com.google.inject.servlet.ServletModule;

/**
 * Install sso client servlets.
 *
 * @author Sergii Kabashniuk
 */
public class SsoClientServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        serve("/_sso/client/logout").with(com.codenvy.auth.sso.client.SSOLogoutServlet.class);
    }
}
