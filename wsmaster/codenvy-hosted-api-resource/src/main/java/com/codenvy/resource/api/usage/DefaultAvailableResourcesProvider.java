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
package com.codenvy.resource.api.usage;

import com.codenvy.resource.api.AvailableResourcesProvider;
import com.codenvy.resource.api.ResourceAggregator;
import com.codenvy.resource.api.exception.NoEnoughResourcesException;
import com.codenvy.resource.model.Resource;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of providing available resources for accounts.
 *
 * <p>By default account can use resources only by itself, so available
 * resources equals to total resources minus resources which
 * are already used by account.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class DefaultAvailableResourcesProvider implements AvailableResourcesProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAvailableResourcesProvider.class);

    private final Provider<ResourceUsageManager> resourceUsageManagerProvider;
    private final ResourceAggregator             resourceAggregator;

    @Inject
    public DefaultAvailableResourcesProvider(Provider<ResourceUsageManager> resourceUsageManagerProvider,
                                             ResourceAggregator resourceAggregator) {
        this.resourceUsageManagerProvider = resourceUsageManagerProvider;
        this.resourceAggregator = resourceAggregator;
    }

    @Override
    public List<? extends Resource> getAvailableResources(String accountId) throws NotFoundException, ServerException {
        ResourceUsageManager resourceUsageManager = resourceUsageManagerProvider.get();
        List<? extends Resource> totalResources = null;
        List<Resource> usedResources = null;
        try {
            totalResources = resourceUsageManager.getTotalResources(accountId);
            usedResources = new ArrayList<>(resourceUsageManager.getUsedResources(accountId));
            return resourceAggregator.deduct(totalResources, usedResources);
        } catch (NoEnoughResourcesException e) {
            LOG.warn("Account with id {} uses more resources {} than he has {}.", accountId, format(usedResources), format(totalResources));
            return resourceAggregator.excess(totalResources, usedResources);
        }
    }

    /**
     * Returns formatted string for list of resources.
     */
    private static String format(Collection<? extends Resource> resources) {
        return '[' +
               resources.stream()
                        .map(resource -> resource.getAmount() + resource.getUnit() + " of " + resource.getType())
                        .collect(Collectors.joining(", "))
               + ']';
    }
}
