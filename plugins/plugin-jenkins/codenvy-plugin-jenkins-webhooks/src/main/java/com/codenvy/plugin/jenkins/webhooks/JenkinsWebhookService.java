/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.plugin.jenkins.webhooks;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codenvy.plugin.jenkins.webhooks.shared.JenkinsEventDto;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
