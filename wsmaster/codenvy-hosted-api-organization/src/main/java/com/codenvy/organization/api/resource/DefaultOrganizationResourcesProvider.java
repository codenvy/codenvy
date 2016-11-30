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
package com.codenvy.organization.api.resource;

import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.shared.model.Organization;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.api.free.DefaultResourcesProvider;
import com.codenvy.resource.api.RamResourceType;
import com.codenvy.resource.api.RuntimeResourceType;
import com.codenvy.resource.api.WorkspaceResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.Size;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Provided free resources that are available for usage by organizational accounts by default.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class DefaultOrganizationResourcesProvider implements DefaultResourcesProvider {
    private final OrganizationManager organizationManager;
    private final long                ramPerOrganization;
    private final int                 workspacesPerOrganization;
    private final int                 runtimesPerOrganization;

    @Inject
    public DefaultOrganizationResourcesProvider(OrganizationManager organizationManager,
                                                @Named("limits.organization.workspaces.ram") String ramPerOrganization,
                                                @Named("limits.organization.workspaces.count") int workspacesPerOrganization,
                                                @Named("limits.organization.workspaces.run.count") int runtimesPerOrganization) {
        this.organizationManager = organizationManager;
        this.ramPerOrganization = "-1".equals(ramPerOrganization) ? -1 : Size.parseSizeToMegabytes(ramPerOrganization);
        this.workspacesPerOrganization = workspacesPerOrganization;
        this.runtimesPerOrganization = runtimesPerOrganization;
    }

    @Override
    public String getAccountType() {
        return OrganizationImpl.ORGANIZATIONAL_ACCOUNT;
    }

    @Override
    public List<ResourceImpl> getResources(String accountId) throws ServerException, NotFoundException {
        final Organization organization = organizationManager.getById(accountId);
        // only root organizations should have own resources
        if (organization.getParent() == null) {
            return asList(new ResourceImpl(RamResourceType.ID, ramPerOrganization, RamResourceType.UNIT),
                          new ResourceImpl(WorkspaceResourceType.ID, workspacesPerOrganization, WorkspaceResourceType.UNIT),
                          new ResourceImpl(RuntimeResourceType.ID, runtimesPerOrganization, RuntimeResourceType.UNIT));
        }

        return Collections.emptyList();
    }
}
