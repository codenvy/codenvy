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
package com.codenvy.api.workspace.server;

import com.codenvy.api.workspace.server.dao.WorkerDao;
import com.codenvy.api.workspace.server.model.WorkerImpl;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.event.WorkspaceCreatedEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;

/**
 * Adds permissions for creator after workspace creation
 *
 * @author Sergii Leschenko
 */
@Singleton
public class WorkspaceCreatorPermissionsProvider implements EventSubscriber<WorkspaceCreatedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspacePermissionsRemover.class);

    private final WorkerDao    workerDao;
    private final EventService eventService;

    @Inject
    public WorkspaceCreatorPermissionsProvider(EventService eventService, WorkerDao workerDao) {
        this.workerDao = workerDao;
        this.eventService = eventService;
    }

    @PostConstruct
    void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    void unsubscribe() {
        eventService.subscribe(this);
    }

    @Override
    public void onEvent(WorkspaceCreatedEvent event) {
        try {
            workerDao.store(new WorkerImpl(EnvironmentContext.getCurrent().getSubject().getUserId(),
                                           event.getWorkspace().getId(),
                                           new ArrayList<>(new WorkspaceDomain().getAllowedActions())));
        } catch (ServerException e) {
            LOG.error("Can't add creator's permissions for workspace with id '" + event.getWorkspace().getId() + "'", e);
        }
    }
}
