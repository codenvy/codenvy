/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.api.workspace.server.jpa.listener;

import com.codenvy.api.permission.server.jpa.listener.RemovePermissionsOnLastUserRemovedEventSubscriber;
import com.codenvy.api.workspace.server.spi.jpa.JpaStackPermissionsDao;

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
    public void remove(String instanceId) throws ServerException {
        stackDao.remove(instanceId);
    }
}
