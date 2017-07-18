/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
