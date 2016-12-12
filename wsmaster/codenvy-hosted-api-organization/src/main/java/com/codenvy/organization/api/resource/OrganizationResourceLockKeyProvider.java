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
import com.codenvy.resource.api.ResourceLockKeyProvider;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides resources lock key for accounts with organizational type.
 *
 * <p>A lock key for any organization is an identifier of the root organization.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class OrganizationResourceLockKeyProvider implements ResourceLockKeyProvider {
    private final OrganizationManager organizationManager;

    @Inject
    public OrganizationResourceLockKeyProvider(OrganizationManager organizationManager) {
        this.organizationManager = organizationManager;
    }

    @Override
    public String getLockKey(String accountId) throws ServerException {
        String currentOrganizationId = accountId;
        try {
            Organization organization = organizationManager.getById(currentOrganizationId);
            while (organization.getParent() != null) {
                currentOrganizationId = organization.getParent();
                organization = organizationManager.getById(currentOrganizationId);
            }
            return organization.getId();
        } catch (NotFoundException e) {
            // should not happen
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public String getAccountType() {
        return OrganizationImpl.ORGANIZATIONAL_ACCOUNT;
    }
}
