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
package com.codenvy.service.bitbucket;

import org.eclipse.che.api.core.rest.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Service for retrieving configured Bitbucket properties from ws-master.
 *
 * @author Igor Vinokur
 */
@Path("bitbucket")
public class BitbucketConfigurationService extends Service {

    private final String bitbucketEndpoint;

    @Inject
    public BitbucketConfigurationService(@Named("bitbucket.endpoint") String bitbucketEndpoint) {
        this.bitbucketEndpoint = bitbucketEndpoint;
    }

    @GET
    @Path("endpoint")
    public String getEndpoint() {
        return bitbucketEndpoint;
    }
}
