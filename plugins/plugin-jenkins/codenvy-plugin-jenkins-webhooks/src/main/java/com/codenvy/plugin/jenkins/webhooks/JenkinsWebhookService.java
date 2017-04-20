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
package com.codenvy.plugin.jenkins.webhooks;

import com.codenvy.plugin.jenkins.webhooks.shared.JenkinsEventDto;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/jenkins-webhook")
public class JenkinsWebhookService extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(JenkinsWebhookService.class);

    private final JenkinsWebhookManager manager;

    @Inject
    public JenkinsWebhookService(JenkinsWebhookManager manager) {
        this.manager = manager;
    }

    @POST
    @Consumes(APPLICATION_JSON)
    public void handleWebhookEvent(JenkinsEventDto jenkinsEvent) throws ServerException {
        LOG.debug("{}", jenkinsEvent);
        manager.handleFailedJobEvent(jenkinsEvent);
    }
}
