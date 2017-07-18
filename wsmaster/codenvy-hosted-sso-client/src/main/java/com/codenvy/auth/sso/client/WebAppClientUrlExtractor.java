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
package com.codenvy.auth.sso.client;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;

/**
 * Take path to the web application as sso client url path.
 *
 * @author Sergii Kabashniuk
 */
public class WebAppClientUrlExtractor implements ClientUrlExtractor {
    @Override
    public String getClientUrl(HttpServletRequest req) throws MalformedURLException {
        return UriBuilder.fromUri(req.getRequestURL().toString()).replacePath(req.getContextPath()).replaceQuery(null).build().toString();
    }
}
