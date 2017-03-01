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
import com.codenvy.organization.shared.model.Organization;
import com.codenvy.resource.api.ResourceAggregator;
import com.codenvy.resource.api.ResourcesReserveTracker;
import com.codenvy.resource.api.usage.ResourceUsageManager;
import com.codenvy.resource.model.Resource;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Pages;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.organization.spi.impl.OrganizationImpl.ORGANIZATIONAL_ACCOUNT;

/**
 * Makes organization's resources unavailable for usage when suborganization
 * use shared resources.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class OrganizationResourcesReserveTracker implements ResourcesReserveTracker {
    private final OrganizationManager            organizationManager;
    private final ResourceAggregator             resourceAggregator;
    private final Provider<ResourceUsageManager> usageManagerProvider;

    @Inject
    public OrganizationResourcesReserveTracker(OrganizationManager organizationManager,
                                               ResourceAggregator resourceAggregator,
                                               Provider<ResourceUsageManager> usageManagerProvider) {

        this.organizationManager = organizationManager;
        this.resourceAggregator = resourceAggregator;
        this.usageManagerProvider = usageManagerProvider;
    }

    @Override
    public List<? extends Resource> getReservedResources(String accountId) throws ServerException {
        ResourceUsageManager resourceUsageManager = usageManagerProvider.get();
        List<Resource> reservedResources = new ArrayList<>();
        for (Organization suborganization : Pages.iterate((maxItems, skipCount) -> organizationManager.getByParent(accountId,
                                                                                                                   maxItems,
                                                                                                                   skipCount))) {
            try {
                // make unavailable for parent shared resources that are used
                reservedResources.addAll(resourceUsageManager.getUsedResources(suborganization.getId()));
                reservedResources.addAll(resourceUsageManager.getReservedResources(suborganization.getId()));
            } catch (NotFoundException e) {
                throw new ServerException(e.getLocalizedMessage(), e);
            }
        }
        return new ArrayList<>(resourceAggregator.aggregateByType(reservedResources)
                                                 .values());
    }

    @Override
    public String getAccountType() {
        return ORGANIZATIONAL_ACCOUNT;
    }
}
