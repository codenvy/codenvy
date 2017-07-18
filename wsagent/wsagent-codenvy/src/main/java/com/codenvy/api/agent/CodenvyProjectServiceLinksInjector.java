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
package com.codenvy.api.agent;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.api.project.server.ProjectService;
import org.eclipse.che.api.project.server.ProjectServiceLinksInjector;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Helps to inject {@link ProjectService} related links.
 *
 * @author Valeriy Svydenko
 */
public class CodenvyProjectServiceLinksInjector extends ProjectServiceLinksInjector {
    private final String host;

    @Inject
    public CodenvyProjectServiceLinksInjector(@Named("che.api") String apiEndpoint) {
        host = UriBuilder.fromUri(apiEndpoint).build().getHost();
    }

    @Override
    protected String tuneUrl(URI url) {
        return UriBuilder.fromUri(url).host(host).build().toString();
    }
}
