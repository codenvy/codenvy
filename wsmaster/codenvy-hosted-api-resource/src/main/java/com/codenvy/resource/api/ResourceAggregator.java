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
package com.codenvy.resource.api;

import com.codenvy.resource.model.Resource;
import com.codenvy.resource.model.ResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helps aggregate resources by theirs type.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class ResourceAggregator {
    private final Map<String, ResourceType> resourcesTypes;

    @Inject
    public ResourceAggregator(Set<ResourceType> resourcesTypes) {
        this.resourcesTypes = resourcesTypes.stream()
                                            .collect(Collectors.toMap(ResourceType::getId, Function.identity()));
    }

    /**
     * Aggregates resources of the same type.
     *
     * @param resources
     *         resources list which can contain more that one instance for some type
     * @return map where key is resources type and value is aggregated resource
     * @throws NotFoundException
     *         when resources list contains resource with not supported type
     */
    public Map<String, ResourceImpl> aggregateByType(List<ResourceImpl> resources) throws NotFoundException {
        checkSupporting(resources);

        Map<String, ResourceImpl> type2Resource = new HashMap<>();
        for (ResourceImpl resource : resources) {
            final ResourceImpl resource1 = type2Resource.get(resource.getType());
            if (resource1 != null) {
                type2Resource.put(resource.getType(), aggregate(resource1, resource));
            } else {
                type2Resource.put(resource.getType(), resource);
            }
        }
        return type2Resource;
    }

    /**
     * Returns list which is result of deduction {@code resourceToDeduct} from {@code totalResources}.
     *
     * @param totalResources
     *         the total resources
     * @param resourcesToDeduct
     *         the resources which should be deducted from {@code totalResources}
     * @throws ConflictException
     *         when {@code totalResources} list doesn't contain enough resources
     * @throws NotFoundException
     *         when {@code totalResources} or {@code deduction} contain resource with not supported type
     */
    public List<ResourceImpl> deduct(List<ResourceImpl> totalResources,
                                     List<ResourceImpl> resourcesToDeduct) throws NotFoundException,
                                                                                  ConflictException {
        checkSupporting(totalResources);
        checkSupporting(resourcesToDeduct);

        final Map<String, ResourceImpl> result = totalResources.stream()
                                                               .collect(Collectors.toMap(Resource::getType,
                                                                                         Function.identity()));
        for (ResourceImpl toDeduct : resourcesToDeduct) {
            final ResourceImpl resource1 = result.get(toDeduct.getType());
            if (resource1 != null) {
                result.put(toDeduct.getType(), deduct(resource1, toDeduct));
            } else {
                throw new ConflictException(String.format("Your account doesn't have %s resource to use.", toDeduct.getType()));
            }
        }
        return new ArrayList<>(result.values());
    }

    /**
     * Check supporting of all given resources.
     *
     * @param resources
     *         resources to check types
     * @throws NotFoundException
     *         when {@code resources} list contains resource with not supported type
     */
    private void checkSupporting(List<ResourceImpl> resources) throws NotFoundException {
        final Set<String> resourcesTypes = resources.stream()
                                                    .map(Resource::getType)
                                                    .collect(Collectors.toSet());
        for (String resourcesType : resourcesTypes) {
            if (!this.resourcesTypes.containsKey(resourcesType)) {
                throw new NotFoundException(String.format("'%s' resource type is not supported", resourcesType));
            }
        }
    }

    /**
     * Aggregates two resources which have the same type.
     *
     * @param resourceA
     *         resources A
     * @param resourceB
     *         resource B
     * @return one resources with type {@code T} that is result of aggregating {@code resourceA} and {@code resourceB}
     * @throws NotFoundException
     *         when {@code T} is not supported type
     */
    private ResourceImpl aggregate(ResourceImpl resourceA, ResourceImpl resourceB) throws NotFoundException {
        final String typeId = resourceA.getType();
        final ResourceType resourceType = getResourceType(typeId);
        return resourceType.aggregate(resourceA, resourceB);
    }

    /**
     * Deducts two resources which have the same type.
     *
     * @param totalResource
     *         total resource
     * @param deduction
     *         resources which should be deducted from {@code totalResource}
     * @return one resources with type {@code T} that is result of subtraction {@code totalResource} and {@code deduction}
     * @throws ConflictException
     *         when {@code totalResource}'s amount is less than {@code deduction}'s amount
     * @throws NotFoundException
     *         when {@code T} is not supported type
     */
    private ResourceImpl deduct(ResourceImpl totalResource, ResourceImpl deduction) throws NotFoundException, ConflictException {
        final String typeId = totalResource.getType();
        final ResourceType resourceType = getResourceType(typeId);
        return resourceType.deduct(totalResource, deduction);
    }

    /**
     * Returns resources type by given id.
     *
     * @param typeId
     *         id of resources type
     * @return resources type by given id
     * @throws NotFoundException
     *         when type by given id is not supported type
     */
    private ResourceType getResourceType(String typeId) throws NotFoundException {
        final ResourceType resourceType = resourcesTypes.get(typeId);
        if (resourceType == null) {
            throw new NotFoundException(String.format("'%s' resource type is not supported", typeId));
        }
        return resourceType;
    }
}
