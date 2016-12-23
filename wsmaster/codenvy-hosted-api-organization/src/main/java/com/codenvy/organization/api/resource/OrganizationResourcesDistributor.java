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
import com.codenvy.organization.shared.model.OrganizationDistributedResources;
import com.codenvy.organization.spi.OrganizationDistributedResourcesDao;
import com.codenvy.organization.spi.impl.OrganizationDistributedResourcesImpl;
import com.codenvy.resource.api.ResourceAggregator;
import com.codenvy.resource.api.exception.NoEnoughResourcesException;
import com.codenvy.resource.api.usage.ResourceUsageManager;
import com.codenvy.resource.api.usage.ResourcesLocks;
import com.codenvy.resource.model.Resource;
import com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.concurrent.CloseableLock;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * Facade for organization resources distribution operations.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class OrganizationResourcesDistributor {
    private final OrganizationDistributedResourcesDao organizationDistributedResourcesDao;
    private final ResourcesLocks                      resourcesLocks;
    private final ResourceUsageManager                usageManager;
    private final ResourceAggregator                  resourceAggregator;
    private final OrganizationManager                 organizationManager;

    @Inject
    public OrganizationResourcesDistributor(OrganizationDistributedResourcesDao organizationDistributedResourcesDao,
                                            ResourcesLocks resourcesLocks,
                                            ResourceUsageManager usageManager,
                                            ResourceAggregator resourceAggregator,
                                            OrganizationManager organizationManager) {
        this.organizationDistributedResourcesDao = organizationDistributedResourcesDao;
        this.resourcesLocks = resourcesLocks;
        this.usageManager = usageManager;
        this.resourceAggregator = resourceAggregator;
        this.organizationManager = organizationManager;
    }

    /**
     * Distributes resources for suborganization.
     *
     * <p>The distributed resources become unavailable for usage by parent organization.
     *
     * <p>To initial distributing resources or increasing number of distributed ones parent
     * organization should have enough available resources.
     * And vice versa to decrease number of distributed resources suborganization should
     * have enough available resources (which are not in use and not distributed).
     *
     * @param suborganizationId
     *         suborganization identifier
     * @param resources
     *         resources to distribute
     * @throws NullPointerException
     *         when either {@code suborganizationId} or {@code resources} is null
     * @throws NotFoundException
     *         when organization with {@code suborganizationId} was not found
     * @throws ConflictException
     *         when specified {@code suborganizationId} is root organization's identifier
     * @throws ConflictException
     *         when parent organization doesn't have enough resources to increase distributed resource amount
     * @throws ConflictException
     *         when suborganization doesn't have enough available resources (which are not in use and not distributed).
     * @throws ServerException
     *         when any other error occurs
     */
    public void distribute(String suborganizationId, List<? extends Resource> resources) throws NotFoundException,
                                                                                                ConflictException,
                                                                                                ServerException {
        requireNonNull(suborganizationId, "Required non-null suborganization id");
        requireNonNull(resources, "Required non-null resources to distribute");
        checkArgument(!resources.isEmpty(), "Required at least one resource to distribute");

        // locking resources by suborganization should lock resources whole organization tree
        // so we can check resource availability for suborganization and parent organization
        // TODO Rework it to using resourcesLocks.acquiresLock(suborganizationId, parentOrganizationId) when it will be implemented
        try (CloseableLock lock = resourcesLocks.acquiresLock(suborganizationId)) {
            checkResourcesAvailability(suborganizationId,
                                       getDistributionOrganization(suborganizationId),
                                       getDistributedResources(suborganizationId),
                                       resources);

            organizationDistributedResourcesDao.store(new OrganizationDistributedResourcesImpl(suborganizationId, resources));
        }
    }

    /**
     * Returns distributed resources for given suborganization.
     *
     * @param suborganizationId
     *         suborganization identifier
     * @return distributed resources for given suborganization
     * @throws NotFoundException
     *         when organization with specified id doesn't have distributed resources
     * @throws ServerException
     *         when any other error occurs
     */
    public OrganizationDistributedResources get(String suborganizationId) throws NotFoundException, ServerException {
        requireNonNull(suborganizationId, "Required non-null suborganization id");
        return organizationDistributedResourcesDao.get(suborganizationId);
    }

    /**
     * Returns distributed resources for suborganizations by specified parent organization
     *
     * @param organizationId
     *         organization id
     * @return distributed resources for suborganizations by specified parent organization
     * @throws NullPointerException
     *         when either {@code organizationId} is null
     * @throws ServerException
     *         when any other error occurs
     */
    public Page<? extends OrganizationDistributedResources> getByParent(String organizationId,
                                                                        int maxItems,
                                                                        long skipCount) throws ServerException {
        requireNonNull(organizationId, "Required non-null organization id");

        return organizationDistributedResourcesDao.getByParent(organizationId, maxItems, skipCount);
    }

    /**
     * Reset resources distribution.
     *
     * <p>When parent organization reset resources distribution for its suborganization resources become available for usage by itself.
     *
     * Suborganization should not use resource and it should not be distributed for its
     * suborganizations in case resetting distributed resource.
     *
     * @param organizationId
     *         organization id
     * @throws NullPointerException
     *         when either {@code organizationId} is null
     * @throws NotFoundException
     *         when organization with specified id was not found
     * @throws ConflictException
     *         when specified {@code suborganizationId} is root organization's identifier
     * @throws ConflictException
     *         when suborganization doesn't have enough available resources (which are not in use and not distributed).
     * @throws ServerException
     *         when any other error occurs
     */
    public void reset(String organizationId) throws NotFoundException,
                                                    ConflictException,
                                                    ServerException {
        requireNonNull(organizationId, "Required non-null organization id");

        try (CloseableLock lock = resourcesLocks.acquiresLock(organizationId)) {
            checkResourcesAvailability(organizationId,
                                       getDistributionOrganization(organizationId),
                                       getDistributedResources(organizationId),
                                       emptyList());
            organizationDistributedResourcesDao.remove(organizationId);
        }
    }

    private String getDistributionOrganization(String organizationId) throws NotFoundException, ServerException, ConflictException {
        String parentOrganization = organizationManager.getById(organizationId).getParent();
        if (parentOrganization == null) {
            throw new ConflictException("It is not allowed to distribute resources for root organization.");
        }
        return parentOrganization;
    }

    /**
     * Checks that parent organization and suborganization have enough available resources to perform resources distribution.
     *
     * <p>Parent organization should have enough available resource to distribute it initially or increase number of distributed one.
     * Suborganization should not use resource and it should not be distributed for its
     * suborganizations in case decreasing number or resetting of distributed one.
     *
     * @param suborganizationId
     *         identifier of suborganization
     * @param parentOrganizationId
     *         identifier of parent organization
     * @param newResources
     *         resources to distribute
     * @param existingResources
     *         resources which are already distributed
     * @throws ConflictException
     *         when parent organization doesn't have enough resources to increase distributed resource amount
     * @throws ConflictException
     *         when resources can't be distributed because suborganization is using existing resources
     *         or when they are distributed to next organizations level
     * @throws ServerException
     *         when any other error occurs
     */
    @VisibleForTesting
    void checkResourcesAvailability(String suborganizationId,
                                    String parentOrganizationId,
                                    List<? extends Resource> existingResources,
                                    List<? extends Resource> newResources) throws NotFoundException,
                                                                                  ConflictException,
                                                                                  ServerException {

        List<Resource> suborganizationResourcesToReset = new ArrayList<>();
        List<Resource> parentOrganizationResourcesToDistribute = new ArrayList<>();

        final Map<String, Resource> distributedResourceToType = existingResources.stream()
                                                                                 .collect(Collectors.toMap(Resource::getType,
                                                                                                           Function.identity()));

        for (Resource newResource : newResources) {
            final Resource existing = distributedResourceToType.remove(newResource.getType());
            if (existing != null) {
                try {
                    final Resource toReset = resourceAggregator.deduct(existing, newResource);
                    // distributed resource amount is greater than new one
                    // we should check availability of difference resource on suborganization level
                    suborganizationResourcesToReset.add(toReset);
                } catch (NoEnoughResourcesException e) {
                    // distributed resource amount is less than new one
                    // we should check availability of difference resource on parent organization level
                    parentOrganizationResourcesToDistribute.add(e.getMissingResources().get(0));
                }
            } else {
                // distributed resources doesn't contain this resource
                // so we should check availability of it on parent organization level
                parentOrganizationResourcesToDistribute.add(newResource);
            }
        }

        // add all resources from distributed resources which were not present in resources to distribute
        suborganizationResourcesToReset.addAll(distributedResourceToType.values());

        if (!parentOrganizationResourcesToDistribute.isEmpty()) {
            try {
                usageManager.checkResourcesAvailability(parentOrganizationId, parentOrganizationResourcesToDistribute);
            } catch (NoEnoughResourcesException e) {
                throw new ConflictException("Parent organization doesn't have enough resources. Try to stop resources usage or " +
                                            "distribute resources in other way.");
            }
        }

        if (!suborganizationResourcesToReset.isEmpty()) {
            try {
                usageManager.checkResourcesAvailability(suborganizationId, suborganizationResourcesToReset);
            } catch (NoEnoughResourcesException e) {
                throw new ConflictException("Resources are currently in use. You can't reallocate and decrease them, while they are " +
                                            "used. Free resources, by stopping workspaces, before changing the resources distribution.");
            }
        }
    }

    /**
     * Returns distributed resources or empty list
     *
     * @param organizationId
     *         organization id to fetch resources
     * @return distributed resources or empty list
     * @throws ServerException
     *         when any other error occurs
     */
    private List<? extends Resource> getDistributedResources(String organizationId) throws ServerException {
        try {
            return organizationDistributedResourcesDao.get(organizationId).getResources();
        } catch (NotFoundException ignored) {
            return emptyList();
        }
    }
}
