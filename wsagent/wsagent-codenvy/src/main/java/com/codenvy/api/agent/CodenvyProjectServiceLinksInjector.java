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
