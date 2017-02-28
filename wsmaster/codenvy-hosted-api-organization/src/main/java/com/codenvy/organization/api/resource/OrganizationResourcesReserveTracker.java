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

import com.codenvy.resource.api.ResourceAggregator;
import com.codenvy.resource.api.ResourcesReserveTracker;
import com.codenvy.resource.model.Resource;

import org.eclipse.che.api.core.Pages;
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

        List<? extends Resource> toReserve = Pages.stream((maxItems, skipCount) ->
                                                                  managerProvider.get().getByParent(accountId, maxItems, skipCount))
                                                  .flatMap(distributedResources -> distributedResources.getResources()
                                                                                                       .stream())
                                                  .collect(Collectors.toList());

        return new ArrayList<>(resourceAggregator.aggregateByType(toReserve)
                                                 .values());
    }

    @Override
    public String getAccountType() {
        return ORGANIZATIONAL_ACCOUNT;
    }
}
