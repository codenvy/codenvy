/*
 *  [2012] - [2016] Codenvy, S.A.
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
