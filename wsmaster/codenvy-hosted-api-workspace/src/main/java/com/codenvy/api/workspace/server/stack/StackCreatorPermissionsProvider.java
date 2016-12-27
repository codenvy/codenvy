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
package com.codenvy.api.workspace.server.stack;

import com.codenvy.api.permission.server.PermissionsManager;
import com.google.inject.Inject;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.event.StackPersistedEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.core.db.cascade.CancelableEventSubscriber;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

/**
 * Grants access to stack which is created by user who is {@link EnvironmentContext#getSubject() subject},
 * if there is no subject present in {@link EnvironmentContext#getCurrent() current} context
 * then no permissions will be added.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class StackCreatorPermissionsProvider extends CancelableEventSubscriber<StackPersistedEvent> {

    @Inject
    private PermissionsManager permissionsManager;

    @Inject
    private EventService eventService;

    @Override
    public void onCancelableEvent(StackPersistedEvent event) throws Exception {
        final Subject subject = EnvironmentContext.getCurrent().getSubject();
        if (subject != null) {
            permissionsManager.storePermission(new StackPermissionsImpl(subject.getUserId(),
                                                                        event.getStack().getId(),
                                                                        StackDomain.getActions()));
        }
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    public void unsubscribe() {
        eventService.unsubscribe(this);
    }
}
