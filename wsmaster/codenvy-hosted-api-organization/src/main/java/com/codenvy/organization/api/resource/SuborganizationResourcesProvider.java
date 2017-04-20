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
package com.codenvy.organization.api.resource;

import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.api.license.ResourcesProvider;
import com.codenvy.resource.api.usage.ResourceUsageManager;
import com.codenvy.resource.model.ProvidedResources;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.ProvidedResourcesImpl;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

/**
 * Provides resources that are shared for suborganization by its parent organization.
 *
 * <p>By default suborganizations are able to use parent's resources.
 * Parent organization can limit usage of resources by suborganization by
 * setting resources caps.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class SuborganizationResourcesProvider implements ResourcesProvider {
    public static final String PARENT_RESOURCES_PROVIDER = "parentOrganization";

    private final AccountManager                             accountManager;
    private final OrganizationManager                        organizationManager;
    private final Provider<OrganizationResourcesDistributor> distributorProvider;
    private final Provider<ResourceUsageManager>             usageManagerProvider;

    @Inject
    public SuborganizationResourcesProvider(AccountManager accountManager,
                                            OrganizationManager organizationManager,
                                            Provider<OrganizationResourcesDistributor> distributorProvider,
                                            Provider<ResourceUsageManager> usageManagerProvider) {
        this.accountManager = accountManager;
        this.organizationManager = organizationManager;
        this.distributorProvider = distributorProvider;
        this.usageManagerProvider = usageManagerProvider;
    }

    @Override
    public List<ProvidedResources> getResources(String accountId) throws NotFoundException,
                                                                         ServerException {
        final Account account = accountManager.getById(accountId);
        String parent;

        if (!OrganizationImpl.ORGANIZATIONAL_ACCOUNT.equals(account.getType())
            || (parent = organizationManager.getById(accountId).getParent()) == null) {
            return emptyList();
        }

        // given account is suborganization's account and can have resources provided by parent
        List<? extends Resource> parentTotalResources = usageManagerProvider.get().getTotalResources(parent);

        if (!parentTotalResources.isEmpty()) {
            try {
                List<? extends Resource> resourcesCaps = distributorProvider.get().getResourcesCaps(accountId);

                return singletonList(new ProvidedResourcesImpl(PARENT_RESOURCES_PROVIDER,
                                                               null,
                                                               accountId,
                                                               -1L,
                                                               -1L,
                                                               cap(parentTotalResources, resourcesCaps)));
            } catch (ConflictException e) {
                throw new ServerException(e.getLocalizedMessage());
            }
        }

        return emptyList();
    }

    private List<ResourceImpl> cap(Collection<? extends Resource> source, List<? extends Resource> caps) {
        final Map<String, Resource> resourcesCaps = caps.stream()
                                                        .collect(toMap(Resource::getType, Function.identity()));
        return source.stream()
                     .map(resource -> {
                         Resource resourceCap = resourcesCaps.get(resource.getType());
                         if (resourceCap != null &&
                             resourceCap.getAmount() < resource.getAmount()) {
                             return resourceCap;
                         }
                         return resource;
                     })
                     .map(ResourceImpl::new)
                     .collect(Collectors.toList());
    }
}
