/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.security;

import com.codenvy.client.CodenvyClient;
import com.codenvy.client.auth.Credentials;

/**
 * @author Stéphane Daviet
 */
public final class CredentialsHelper {

    private CodenvyClient                  codenvyClient;

    public CredentialsHelper(CodenvyClient codenvyClient) {
        this.codenvyClient = codenvyClient;
    }

    public RemoteCredentials convert(Credentials credentials) {
        RemoteCredentials remoteCredentials = new RemoteCredentials();
        remoteCredentials.setUsername(credentials.username());
        remoteCredentials.setToken(credentials.token().value());
        return remoteCredentials;
    }

    public Credentials convert(RemoteCredentials remoteCredentials) {
        return remoteCredentials != null ? codenvyClient.newCredentialsBuilder()
                                                        .withUsername(remoteCredentials.getUsername())
                                                        .withToken(codenvyClient.newTokenBuilder(remoteCredentials.getToken())
                                                                                .build())
                                                        .build() : null;
    }

}
