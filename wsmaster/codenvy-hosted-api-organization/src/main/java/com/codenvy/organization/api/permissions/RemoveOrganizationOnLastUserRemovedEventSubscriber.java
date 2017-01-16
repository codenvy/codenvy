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
package com.codenvy.organization.api.permissions;

import com.codenvy.api.permission.server.jpa.listener.RemovePermissionsOnLastUserRemovedEventSubscriber;
import com.codenvy.organization.spi.OrganizationDao;
import com.codenvy.organization.spi.jpa.JpaMemberDao;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Listens for {@link UserImpl} removal events, and checks if the removing user is the last who have "setPermissions"
 * role to particular organization, and if it is, then removes recipe itself.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RemoveOrganizationOnLastUserRemovedEventSubscriber
        extends RemovePermissionsOnLastUserRemovedEventSubscriber<JpaMemberDao> {

    @Inject
    private OrganizationDao organizationDao;

    @Override
    public void remove(String instanceId) throws ServerException {
        organizationDao.remove(instanceId);
    }
}
