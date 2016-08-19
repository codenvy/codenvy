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
package com.codenvy.api.permission.server.jpa;

import com.codenvy.api.permission.server.SystemDomain;

import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author Max Shaposhnik
 *
 */
public class JpaSystemPermissionsDao extends AbstractPermissionsDao<SystemDomain.SystemPermissionsImpl> {
    @Inject
    public JpaSystemPermissionsDao(@Named(SystemDomain.SYSTEM_DOMAIN_ACTIONS) Set<String> allowedActions) throws IOException {
        super(new SystemDomain(allowedActions), SystemDomain.SystemPermissionsImpl.class);
    }

    @Override
    public SystemDomain.SystemPermissionsImpl get(String userId, String instanceId) throws ServerException {
        return null;
    }

    @Override
    public List<SystemDomain.SystemPermissionsImpl> getByInstance(String instanceId) throws ServerException {
        return null;
    }

    @Override
    public List<SystemDomain.SystemPermissionsImpl> getByUser(String userId) throws ServerException {
        return null;
    }
}
