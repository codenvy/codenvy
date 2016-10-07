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
package com.codenvy.api.machine.server.recipe;

import com.codenvy.api.permission.server.PermissionsManager;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.server.event.RecipePersistedEvent;
import org.eclipse.che.api.machine.shared.ManagedRecipe;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import static java.lang.String.format;

/**
 * Adds recipe permissions for current subject if there is no subject present
 * in {@link EnvironmentContext#getCurrent() current} context
 * then no permissions will be added.
 *
 * @author Anton Korneta
 */
@Singleton
public class RecipeCreatorPermissionsProvider implements EventSubscriber<RecipePersistedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(RecipeCreatorPermissionsProvider.class);

    @Inject
    private PermissionsManager permManager;

    @Inject
    private EventService eventService;

    @Override
    public void onEvent(RecipePersistedEvent event) {
        final Subject subject = EnvironmentContext.getCurrent().getSubject();
        if (subject != null) {
            final ManagedRecipe recipe = event.getRecipe();
            try {
                permManager.storePermission(new RecipePermissionsImpl(subject.getUserId(),
                                                                     recipe.getId(),
                                                                     RecipeDomain.getActions()));
            } catch (Exception x) {
                LOG.error(format("Couldn't grant permissions for user with id '%s' to recipe with id '%s'",
                                 subject.getUserId(),
                                 recipe.getId()),
                          x);
            }
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
