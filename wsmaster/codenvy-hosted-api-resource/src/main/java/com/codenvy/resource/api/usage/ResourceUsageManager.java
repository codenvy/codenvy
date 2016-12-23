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
package com.codenvy.resource.api.usage;

import com.codenvy.resource.api.ResourceAggregator;
import com.codenvy.resource.api.ResourceUsageTracker;
import com.codenvy.resource.api.ResourcesReserveTracker;
import com.codenvy.resource.api.exception.NoEnoughResourcesException;
import com.codenvy.resource.api.license.AccountLicenseManager;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Facade for resources using related operations.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class ResourceUsageManager {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceUsageManager.class);

    private final ResourceAggregator                   resourceAggregator;
    private final Set<ResourceUsageTracker>            usageTrackers;
    private final AccountManager                       accountManager;
    private final Map<String, ResourcesReserveTracker> accountTypeToReserveTracker;
    private final AccountLicenseManager                accountLicenseManager;

    @Inject
    public ResourceUsageManager(ResourceAggregator resourceAggregator,
                                Set<ResourceUsageTracker> usageTrackers,
                                Set<ResourcesReserveTracker> resourcesReserveTrackers,
                                AccountManager accountManager,
                                AccountLicenseManager accountLicenseManager) {
        this.resourceAggregator = resourceAggregator;
        this.usageTrackers = usageTrackers;
        this.accountManager = accountManager;
        this.accountLicenseManager = accountLicenseManager;
        this.accountTypeToReserveTracker = resourcesReserveTrackers.stream()
                                                                   .collect(toMap(ResourcesReserveTracker::getAccountType,
                                                                                  Function.identity()));
    }

    /**
     * Returns list of resources which are available for usage by given account.
     *
     * @param accountId
     *         id of account
     * @return list of resources which are available for usage by given account
     * @throws NotFoundException
     *         when account with specified id was not found
     * @throws ServerException
     *         when some exception occurred while resources fetching
     */
    public List<? extends Resource> getTotalResources(String accountId) throws NotFoundException, ServerException {
        final Account account = accountManager.getById(accountId);
        final ResourcesReserveTracker resourcesReserveTracker = accountTypeToReserveTracker.get(account.getType());
        List<? extends Resource> licenseResources = accountLicenseManager.getByAccount(accountId)
                                                                         .getTotalResources();

        if (resourcesReserveTracker == null) {
            return licenseResources;
        }

        List<? extends Resource> reservedResources = resourcesReserveTracker.getReservedResources(accountId);

        try {
            return resourceAggregator.deduct(licenseResources,
                                             reservedResources);
        } catch (NoEnoughResourcesException e) {
            LOG.error("Number of reserved resources is greater than resources provided by license.", e);
            return deductWithSkippingMissed(licenseResources, reservedResources, e.getMissingResources());
        }
    }

    /**
     * Returns list of resources which are available for usage by given account.
     *
     * @param accountId
     *         id of account
     * @return list of resources which are available for usage by given account
     * @throws NotFoundException
     *         when account with specified id was not found
     * @throws ServerException
     *         when some exception occurred while resources fetching
     */
    public List<? extends Resource> getAvailableResources(String accountId) throws NotFoundException, ServerException {
        final List<? extends Resource> totalResources = getTotalResources(accountId);
        final List<? extends Resource> usedResources = getUsedResources(accountId);
        try {
            return resourceAggregator.deduct(totalResources,
                                             usedResources);
        } catch (NoEnoughResourcesException e) {
            LOG.error("Number of used resources more than total resources", e);
            return deductWithSkippingMissed(totalResources, usedResources, e.getMissingResources());
        }
    }

    /**
     * Returns list of resources which are used by given account.
     *
     * @param accountId
     *         id of account
     * @return list of resources which are used by given account
     * @throws NotFoundException
     *         when account with specified id was not found
     * @throws ServerException
     *         when some exception occurred while resources fetching
     */
    public List<? extends Resource> getUsedResources(String accountId) throws NotFoundException, ServerException {
        List<ResourceImpl> usedResources = new ArrayList<>();
        for (ResourceUsageTracker usageTracker : usageTrackers) {
            Optional<ResourceImpl> usedResource = usageTracker.getUsedResource(accountId);
            if (usedResource.isPresent()) {
                usedResources.add(usedResource.get());
            }
        }
        return usedResources;
    }

    /**
     * Checks that specified account has available resources to use
     *
     * @param accountId
     *         account id
     * @param resources
     *         resources to check availability
     * @throws NotFoundException
     *         when account with specified id was not found
     * @throws NoEnoughResourcesException
     *         when account doesn't have specified available resources
     * @throws ServerException
     *         when any other error occurs
     */
    public void checkResourcesAvailability(String accountId, List<? extends Resource> resources) throws NotFoundException,
                                                                                                        NoEnoughResourcesException,
                                                                                                        ServerException {
        List<? extends Resource> availableResources = getAvailableResources(accountId);
        //check resources availability
        resourceAggregator.deduct(availableResources, resources);
    }

    private List<? extends Resource> deductWithSkippingMissed(List<? extends Resource> totalResources,
                                                              List<? extends Resource> resourcesToDeduct,
                                                              List<? extends Resource> missedResources) throws NotFoundException,
                                                                                                               ServerException {
        final Set<String> missedResourcesTypes = missedResources.stream()
                                                                .map(Resource::getType)
                                                                .collect(Collectors.toSet());
        totalResources = totalResources.stream()
                                       .filter(resource -> !missedResourcesTypes.contains(resource.getType()))
                                       .collect(Collectors.toList());
        resourcesToDeduct = resourcesToDeduct.stream()
                                             .filter(resource -> !missedResourcesTypes.contains(resource.getType()))
                                             .collect(Collectors.toList());

        try {
            return resourceAggregator.deduct(totalResources,
                                             resourcesToDeduct);
        } catch (NoEnoughResourcesException e) {
            // should not happen
            throw new ServerException(e.getLocalizedMessage(), e);
        }
    }
}
