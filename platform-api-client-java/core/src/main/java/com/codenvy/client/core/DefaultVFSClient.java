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
package com.codenvy.client.core;

import com.codenvy.client.Request;
import com.codenvy.client.VFSClient;
import com.codenvy.client.core.auth.AuthenticationManager;
import com.codenvy.client.core.model.vfs.DefaultVFSInfo;
import com.codenvy.client.model.vfs.VfsInfo;

import javax.ws.rs.client.Invocation;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * The Codenvy vfs API client.
 *
 * @author Florent Benoit
 */
public class DefaultVFSClient extends AbstractClient implements VFSClient {

    /**
     * Constructs an instance of {@link VFSClient}.
     *
     * @param url
     *         the Codenvy platform URL.
     * @param authenticationManager
     *         the {@link AuthenticationManager}.
     * @throws NullPointerException
     *         if url or authenticationManager parameter is {@code null}.
     */
    DefaultVFSClient(String url, AuthenticationManager authenticationManager) {
        super(url, "vfs-directory-mapping", authenticationManager);
    }


    /**
     * Sets a VFS folder
     *
     * @param workspaceId
     *         the workspace ID.
     * @param path
     *         the VFS root folder.
     * @return a GIT URL that allow to clone the given project.
     */
    @Override
    public Request<VfsInfo> directoryMapping(String workspaceId, String path) {
        final Invocation request = getWebTarget().path(workspaceId)
                                                 .queryParam("mountPath", path)
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPost(null);
        return new SimpleRequest<VfsInfo>(request, DefaultVFSInfo.class, getAuthenticationManager());
    }
}
