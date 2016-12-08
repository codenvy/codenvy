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
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.api.license.ResourcesProvider;
import com.codenvy.resource.model.ProvidedResources;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.ProvidedResourcesImpl;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Provides resources that are shared for suborganization by its parent organization
 *
 * @author Sergii Leschenko
 */
@Singleton
public class SuborganizationResourcesProvider implements ResourcesProvider {
    public static final String PARENT_RESOURCES_PROVIDER = "parentOrganization";

    private final AccountManager                             accountManager;
    private final OrganizationManager                        organizationManager;
    private final Provider<OrganizationResourcesDistributor> distributorProvider;

    @Inject
    public SuborganizationResourcesProvider(AccountManager accountManager,
                                            OrganizationManager organizationManager,
                                            Provider<OrganizationResourcesDistributor> distributorProvider) {
        this.accountManager = accountManager;
        this.organizationManager = organizationManager;
        this.distributorProvider = distributorProvider;
    }

    @Override
    public List<ProvidedResources> getResources(String accountId) throws ServerException,
                                                                         NotFoundException {
        final Account account = accountManager.getById(accountId);

        if (OrganizationImpl.ORGANIZATIONAL_ACCOUNT.equals(account.getType())
            && organizationManager.getById(accountId).getParent() != null) {
            // given account is suborganization's account and can have resources provided by parent
            try {
                final List<? extends Resource> sharedResources = distributorProvider.get().get(accountId).getResources();
                return singletonList(new ProvidedResourcesImpl(PARENT_RESOURCES_PROVIDER,
                                                               null,
                                                               accountId,
                                                               -1L,
                                                               -1L,
                                                               sharedResources));
            } catch (NotFoundException ignored) {
                // there is no any distributed resources for this suborganization
            }
        }

        return Collections.emptyList();
    }
}
