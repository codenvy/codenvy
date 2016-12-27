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
package com.codenvy.api.workspace.server.jpa.listener;

import com.codenvy.api.permission.server.jpa.listener.RemovePermissionsOnLastUserRemovedEventSubscriber;
import com.codenvy.api.workspace.server.spi.jpa.JpaStackPermissionsDao;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.jpa.JpaStackDao;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Listens for {@link UserImpl} removal events, and checks if the removing user is the last who have "setPermissions"
 * role to particular stack, and if it is, then removes stack itself.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class RemoveStackOnLastUserRemovedEventSubscriber extends RemovePermissionsOnLastUserRemovedEventSubscriber<JpaStackPermissionsDao> {

    @Inject
    private JpaStackDao stackDao;

    @Override
    public void remove(String instanceId) throws ServerException{
        stackDao.remove(instanceId);
    }
}
