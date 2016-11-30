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

import com.codenvy.resource.model.FreeResourcesLimit;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.shared.dto.FreeResourcesLimitDto;
import com.codenvy.resource.shared.dto.ResourceDto;

import org.eclipse.che.api.core.BadRequestException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

/**
 * Utils for validation of {@link FreeResourcesLimit}
 *
 * @author Sergii Leschenko
 */
@Singleton
public class FreeResourcesLimitValidator {
    private final ResourceValidator resourceValidator;

    @Inject
    public FreeResourcesLimitValidator(ResourceValidator resourceValidator) {
        this.resourceValidator = resourceValidator;
    }

    /**
     * Validates given {@code freeResourcesLimit}
     *
     * @param freeResourcesLimit
     *         resources limit to validate
     * @throws BadRequestException
     *         when {@code freeResourcesLimit} is null
     * @throws BadRequestException
     *         when any of {@code freeResourcesLimit.getResources} is not valid
     * @see ResourceValidator#validate(ResourceDto)
     */
    public void check(FreeResourcesLimitDto freeResourcesLimit) throws BadRequestException {
        if (freeResourcesLimit == null) {
            throw new BadRequestException("Missed free resources limit description.");
        }
        if (freeResourcesLimit.getAccountId() == null) {
            throw new BadRequestException("Missed account id.");
        }

        Set<String> resourcesToSet = new HashSet<>();
        for (ResourceDto resource : freeResourcesLimit.getResources()) {
            if (!resourcesToSet.add(resource.getType())) {
                throw new BadRequestException(format("Free resources limit should contain only one resources with type '%s'.",
                                                     resource.getType()));
            }
            resourceValidator.validate(resource);
        }
    }
}
