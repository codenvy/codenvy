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
import com.codenvy.api.workspace.server.recipe.RecipeDomain;
import com.codenvy.api.workspace.server.stack.StackDomain;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.server.jpa.JpaRecipeDao;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.jpa.JpaStackDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.codenvy.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;
import static java.lang.String.format;

/**
 * Listens for {@link UserImpl} removal events, and checks if the removing user is the last who have "setPermissions"
 * role to any of the permission domain, and if it is, then removes domain entity itself.
 *
 * @author Max Shaposhnik
 */
public class RemovePermissionsOnLastUserRemovedEventSubscriber implements EventSubscriber<BeforeUserRemovedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(RemovePermissionsOnLastUserRemovedEventSubscriber.class);

    private final Set<String> supportedDomains = Stream.of(StackDomain.DOMAIN_ID, RecipeDomain.DOMAIN_ID)
                                                       .collect(Collectors.toSet());

    private JpaStackDao stackDao;

    private JpaRecipeDao recipeDao;

    private EventService eventService;

    private List<PermissionsDao<? extends AbstractPermissions>> supportedStorages;

    @Inject
    public RemovePermissionsOnLastUserRemovedEventSubscriber(Set<PermissionsDao<? extends AbstractPermissions>> storages,
                                                             JpaStackDao stackDao,
                                                             JpaRecipeDao recipeDao,
                                                             EventService eventService) {
        this.stackDao = stackDao;
        this.recipeDao = recipeDao;
        this.eventService = eventService;
        this.supportedStorages = storages.stream().filter(storage -> supportedDomains.contains(storage.getDomain().getId()))
                                                                                     .collect(Collectors.toList());
    }

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
            for (PermissionsDao<? extends AbstractPermissions> storage : supportedStorages) {
                for (AbstractPermissions permissions : storage.getByUser(event.getUser().getId())) {
                    // This method can  potentially be source of race conditions,
                    // e.g. when performing search by permissions, another thread can add/or remove another setPermission,
                    // so appropriate domain object (stack or recipe) will not be deleted, or vice versa,
                    // deleted when it's not required anymore.
                    // As a result, a solitary objects may be present in the DB.
                    if (storage.getByInstance(permissions.getInstanceId())
                               .stream()
                               .noneMatch(permissions1 -> permissions1.getActions().contains(SET_PERMISSIONS) &&
                                                          !permissions1.getUserId().equals(event.getUser().getId()))) {
                        switch (permissions.getDomainId()) {
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
                    } else {
                        storage.remove(event.getUser().getId(), permissions.getInstanceId());
                    }
                }
            }
        } catch (Exception x) {
            LOG.error(format("Couldn't remove permissions before user '%s' is removed", event.getUser().getId()), x);
        }
    }
}
