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
 * Modifies ws-agent machine server to proxy requests to it.
 *
 * @author Alexander Garagatyi
 */
public class WsAgentServerProxyTransformer extends UriTemplateServerProxyTransformer {
    @Inject
    public WsAgentServerProxyTransformer(@Named("machine.proxy_wsagent_server_url_template") String serverUrlTemplate, @Named("codenvy.host") String codenvyHost, @Nullable @Named("che.docker.ip.external") String cheDockerIpExternal) {
        super(serverUrlTemplate, codenvyHost, cheDockerIpExternal);
    }
}
