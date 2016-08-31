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
package com.codenvy.api.workspace.server.jpa;

import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.api.workspace.server.recipe.RecipeDomain;
import com.codenvy.api.workspace.server.stack.StackDomain;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.server.jpa.JpaRecipeDao;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.jpa.JpaStackDao;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import java.util.Set;

import static com.codenvy.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;
import static java.lang.String.format;

/**
 * Listens for {@link UserImpl} removal events, and checks if the removing user is the last who have "setPermissions"
 * role to any of the permission domain, and if it is, then removes domain entity itself.
 *
 * @author Max Shaposhnik
 */
public class RemovePermissionsOnLastSetPermissionsUserRemovedEventSubscriber implements EventSubscriber<BeforeUserRemovedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(RemovePermissionsOnLastSetPermissionsUserRemovedEventSubscriber.class);

    @Inject
    Set<PermissionsDao<? extends AbstractPermissions>> storages;

    @Inject
    JpaWorkspaceDao workspaceDao;

    @Inject
    JpaStackDao stackDao;

    @Inject
    JpaRecipeDao recipeDao;

    @Inject
    private EventService eventService;

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    public void unsubscribe() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(BeforeUserRemovedEvent event) {
        try {
            for (PermissionsDao<? extends AbstractPermissions> storage : storages) {
                for (AbstractPermissions permissions : storage.getByUser(event.getUser().getId())) {
                    if (storage.getByInstance(permissions.getInstanceId())
                               .stream()
                               .noneMatch(permissions1 -> permissions1.getActions().contains(SET_PERMISSIONS) &&
                                                          !permissions1.getUserId().equals(event.getUser().getId()))) {
                        switch (permissions.getDomainId()) {
                            case WorkspaceDomain.DOMAIN_ID: {
                                workspaceDao.remove(permissions.getInstanceId());
                                break;
                            }
                            case StackDomain.DOMAIN_ID: {
                                stackDao.remove(permissions.getInstanceId());
                                break;
                            }
                            case RecipeDomain.DOMAIN_ID: {
                                recipeDao.remove(permissions.getInstanceId());
                                break;
                            }
                            default: {
                            }
                        }

                    }
                }
            }

        } catch (Exception x) {
            LOG.error(format("Couldn't remove permissions before user '%s' is removed", event.getUser().getId()), x);
        }
    }
}
