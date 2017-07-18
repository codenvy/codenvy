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
package com.codenvy.api.account.personal;

import com.codenvy.resource.api.free.DefaultResourcesProvider;
import com.codenvy.resource.api.type.RamResourceType;
import com.codenvy.resource.api.type.RuntimeResourceType;
import com.codenvy.resource.api.type.TimeoutResourceType;
import com.codenvy.resource.api.type.WorkspaceResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.Size;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

/**
 * Provided free resources that are available for usage by personal accounts by default.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class DefaultUserResourcesProvider implements DefaultResourcesProvider {
    private final long timeout;
    private final long ramPerUser;
    private final int  workspacesPerUser;
    private final int  runtimesPerUser;

    @Inject
    public DefaultUserResourcesProvider(@Named("limits.workspace.idle.timeout") long timeout,
                                        @Named("limits.user.workspaces.ram") String ramPerUser,
                                        @Named("limits.user.workspaces.count") int workspacesPerUser,
                                        @Named("limits.user.workspaces.run.count") int runtimesPerUser) {
        this.timeout = TimeUnit.MILLISECONDS.toMinutes(timeout);
        this.ramPerUser = "-1".equals(ramPerUser) ? -1 : Size.parseSizeToMegabytes(ramPerUser);
        this.workspacesPerUser = workspacesPerUser;
        this.runtimesPerUser = runtimesPerUser;
    }

    @Override
    public String getAccountType() {
        return OnpremisesUserManager.PERSONAL_ACCOUNT;
    }

    @Override
    public List<ResourceImpl> getResources(String accountId) throws ServerException, NotFoundException {
        return asList(new ResourceImpl(TimeoutResourceType.ID, timeout, TimeoutResourceType.UNIT),
                      new ResourceImpl(RamResourceType.ID, ramPerUser, RamResourceType.UNIT),
                      new ResourceImpl(WorkspaceResourceType.ID, workspacesPerUser, WorkspaceResourceType.UNIT),
                      new ResourceImpl(RuntimeResourceType.ID, runtimesPerUser, RuntimeResourceType.UNIT));
    }
}
