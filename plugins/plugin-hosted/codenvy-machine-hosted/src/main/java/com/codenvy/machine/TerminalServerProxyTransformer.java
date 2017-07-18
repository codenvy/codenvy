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
package com.codenvy.machine;

import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Modifies websocket terminal/exec machine servers to proxy requests to them.
 *
 * @author Alexander Garagatyi
 */
public class TerminalServerProxyTransformer extends UriTemplateServerProxyTransformer {
    @Inject
    public TerminalServerProxyTransformer(@Named("machine.proxy_terminal_server_url_template") String serverUrlTemplate,
                                          @Named("codenvy.host") String codenvyHost,
                                          @Nullable @Named("che.docker.ip.external") String cheDockerIpExternal) {
        super(serverUrlTemplate, codenvyHost, cheDockerIpExternal);
    }
}
