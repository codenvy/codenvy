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
package com.codenvy.resource.api.free;

import com.codenvy.resource.model.Resource;
import com.codenvy.resource.model.ResourceType;

import org.eclipse.che.api.core.BadRequestException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Utils for validation of {@link Resource}
 *
 * @author Sergii Leschenko
 */
@Singleton
public class ResourceValidator {
    private final Map<String, Set<String>> resourcesTypesToUnits;

    @Inject
    public ResourceValidator(Set<ResourceType> supportedResources) {
        this.resourcesTypesToUnits = supportedResources.stream()
                                                       .collect(toMap(ResourceType::getId, ResourceType::getSupportedUnits));
    }

    /**
     * Validates given {@code resource}
     *
     * @param resource
     *         resource to validate
     * @throws BadRequestException
     *         when {@code resource} is null
     * @throws BadRequestException
     *         when {@code resource} has non supported type
     * @throws BadRequestException
     *         when {@code resource} has non supported unit
     */
    public void check(Resource resource) throws BadRequestException {
        if (resource == null) {
            throw new BadRequestException("Missed resource");
        }

        final Set<String> units = resourcesTypesToUnits.get(resource.getType());

        if (units == null) {
            throw new BadRequestException("Specified resources type '" + resource.getType() + "' is not supported");
        }

        if (!units.contains(resource.getUnit())) {
            throw new BadRequestException("Specified resources type '" + resource.getType() + "' support only following units: " +
                                          units.stream()
                                               .collect(Collectors.joining(", ")));
        }
    }
}
