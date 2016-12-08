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
import com.codenvy.resource.api.ram.RamResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.Size;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Provided free resources that are available for usage by organizational accounts by default.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class DefaultOrganizationResourcesProvider implements DefaultResourcesProvider {
    private final OrganizationManager organizationManager;
    private final long                ramPerOrganization;

    @Inject
    public DefaultOrganizationResourcesProvider(OrganizationManager organizationManager,
                                                @Named("limits.organization.workspaces.ram") String ramPerOrganization) {
        this.organizationManager = organizationManager;
        this.ramPerOrganization = "-1".equals(ramPerOrganization) ? -1 : Size.parseSizeToMegabytes(ramPerOrganization);
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
            return singletonList(new ResourceImpl(RamResourceType.ID, ramPerOrganization, RamResourceType.UNIT));
        }

        return Collections.emptyList();
    }
}
