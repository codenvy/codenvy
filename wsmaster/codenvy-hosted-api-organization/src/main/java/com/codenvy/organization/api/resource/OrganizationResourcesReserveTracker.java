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

import com.codenvy.organization.shared.model.OrganizationDistributedResources;
import com.codenvy.resource.api.ResourceAggregator;
import com.codenvy.resource.api.ResourcesReserveTracker;
import com.codenvy.resource.model.Resource;

import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.codenvy.organization.spi.impl.OrganizationImpl.ORGANIZATIONAL_ACCOUNT;

/**
 * Makes organization's resources unavailable for usage when organization shares them for its suborganizations.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class OrganizationResourcesReserveTracker implements ResourcesReserveTracker {
    static final int ORGANIZATION_RESOURCES_PER_PAGE = 100;

    private final Provider<OrganizationResourcesDistributor> managerProvider;
    private final ResourceAggregator                         resourceAggregator;

    @Inject
    public OrganizationResourcesReserveTracker(Provider<OrganizationResourcesDistributor> managerProvider,
                                               ResourceAggregator resourceAggregator) {
        this.managerProvider = managerProvider;
        this.resourceAggregator = resourceAggregator;
    }

    @Override
    public List<? extends Resource> getReservedResources(String accountId) throws ServerException {
        Page<? extends OrganizationDistributedResources> resourcesPage = managerProvider.get()
                                                                                        .getByParent(accountId,
                                                                                                     ORGANIZATION_RESOURCES_PER_PAGE,
                                                                                                     0);
        List<Resource> resources = new ArrayList<>();
        do {
            resourcesPage.getItems()
                         .stream()
                         .flatMap(distributedResources -> distributedResources.getResources()
                                                                              .stream())
                         .collect(Collectors.toCollection(() -> resources));
        } while ((resourcesPage = getNextPage(resourcesPage, accountId)) != null);

        return new ArrayList<>(resourceAggregator.aggregateByType(resources)
                                                 .values());
    }

    @Override
    public String getAccountType() {
        return ORGANIZATIONAL_ACCOUNT;
    }

    private Page<? extends OrganizationDistributedResources> getNextPage(Page<? extends OrganizationDistributedResources> resourcesPage,
                                                                         String organizationId) throws ServerException {
        if (!resourcesPage.hasNextPage()) {
            return null;
        }

        final Page.PageRef nextPageRef = resourcesPage.getNextPageRef();
        return managerProvider.get().getByParent(organizationId,
                                                 nextPageRef.getPageSize(),
                                                 nextPageRef.getItemsBefore());
    }
}
