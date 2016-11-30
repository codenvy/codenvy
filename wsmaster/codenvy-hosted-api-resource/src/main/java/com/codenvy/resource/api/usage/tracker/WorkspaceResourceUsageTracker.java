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
package com.codenvy.resource.api.usage.tracker;

import com.codenvy.resource.api.ResourceUsageTracker;
import com.codenvy.resource.api.WorkspaceResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Tracks usage of {@link WorkspaceResourceType} resource.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class WorkspaceResourceUsageTracker implements ResourceUsageTracker {
    private final Provider<WorkspaceManager> workspaceManagerProvider;
    private final AccountManager             accountManager;

    @Inject
    public WorkspaceResourceUsageTracker(Provider<WorkspaceManager> workspaceManagerProvider, AccountManager accountManager) {
        this.workspaceManagerProvider = workspaceManagerProvider;
        this.accountManager = accountManager;
    }

    @Override
    public Optional<ResourceImpl> getUsedResource(String accountId) throws NotFoundException, ServerException {
        final Account account = accountManager.getById(accountId);
        final List<WorkspaceImpl> accountWorkspaces = workspaceManagerProvider.get().getByNamespace(account.getName());
        if (!accountWorkspaces.isEmpty()) {
            return Optional.of(new ResourceImpl(WorkspaceResourceType.ID, accountWorkspaces.size(), WorkspaceResourceType.UNIT));
        } else {
            return Optional.empty();
        }
    }
}
