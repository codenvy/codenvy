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
package com.codenvy.resource.api.free;

import com.codenvy.resource.api.RamResourceType;
import com.codenvy.resource.api.RuntimeResourceType;
import com.codenvy.resource.api.WorkspaceResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.lang.Size;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Provided free resources that are available for usage by personal accounts by default.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class DefaultUserResourcesProvider implements DefaultResourcesProvider {
    private final long ramPerUser;
    private final int  workspacesPerUser;
    private final int  runtimesPerUser;

    @Inject
    public DefaultUserResourcesProvider(@Named("limits.user.workspaces.ram") String ramPerUser,
                                        @Named("limits.user.workspaces.count") int workspacesPerUser,
                                        @Named("limits.user.workspaces.run.count") int runtimesPerUser) {
        this.ramPerUser = "-1".equals(ramPerUser) ? -1 : Size.parseSizeToMegabytes(ramPerUser);
        this.workspacesPerUser = workspacesPerUser;
        this.runtimesPerUser = runtimesPerUser;
    }

    @Override
    public String getAccountType() {
        return UserImpl.PERSONAL_ACCOUNT;
    }

    @Override
    public List<ResourceImpl> getResources(String accountId) throws ServerException, NotFoundException {
        return asList(new ResourceImpl(RamResourceType.ID, ramPerUser, RamResourceType.UNIT),
                      new ResourceImpl(WorkspaceResourceType.ID, workspacesPerUser, WorkspaceResourceType.UNIT),
                      new ResourceImpl(RuntimeResourceType.ID, runtimesPerUser, RuntimeResourceType.UNIT));
    }
}
