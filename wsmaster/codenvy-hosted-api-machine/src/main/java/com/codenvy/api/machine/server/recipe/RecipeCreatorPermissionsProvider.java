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
package com.codenvy.api.machine.server.recipe;

import com.codenvy.api.permission.server.PermissionsManager;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.event.RecipePersistedEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Adds recipe permissions for current subject, if there is no subject present
 * in {@link EnvironmentContext#getCurrent() current} context
 * then no permissions will be added.
 *
 * @author Anton Korneta
 */
@Singleton
public class RecipeCreatorPermissionsProvider extends CascadeEventSubscriber<RecipePersistedEvent> {

    @Inject
    private PermissionsManager permissionsManager;

    @Inject
    private EventService eventService;

    @Override
    public void onCascadeEvent(RecipePersistedEvent event) throws Exception {
        final Subject subject = EnvironmentContext.getCurrent().getSubject();
        if (!subject.isAnonymous()) {
            permissionsManager.storePermission(new RecipePermissionsImpl(subject.getUserId(),
                                                                         event.getRecipe().getId(),
                                                                         RecipeDomain.getActions()));
        }
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(this, RecipePersistedEvent.class);
    }

    @PreDestroy
    public void unsubscribe() {
        eventService.unsubscribe(this, RecipePersistedEvent.class);
    }
}
