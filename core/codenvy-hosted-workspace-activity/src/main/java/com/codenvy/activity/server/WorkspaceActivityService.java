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
package com.codenvy.activity.server;

import org.eclipse.che.api.core.rest.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Monitors the activity of the runtime workspace.
 *
 * @author Anton Korneta
 */
@Singleton
@Path("/activity")
public class WorkspaceActivityService extends Service {

    private final WorkspaceActivityManager workspaceActivityManager;

    @Inject
    public WorkspaceActivityService(WorkspaceActivityManager workspaceActivityManager) {
        this.workspaceActivityManager = workspaceActivityManager;
    }

    @PUT
    @Path("/{wsId}")
    @Consumes(APPLICATION_JSON)
    public void active(@PathParam("wsId") String wsId) {
        workspaceActivityManager.update(wsId, System.currentTimeMillis());
    }
}
