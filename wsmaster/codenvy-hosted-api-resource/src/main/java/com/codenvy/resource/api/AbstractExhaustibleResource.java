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

import com.codenvy.resource.api.exception.NoEnoughResourcesException;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.model.ResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Abstract resource that contains logic for aggregating and deduction for exhaustible resources.
 *
 * @author Sergii Leschenko
 */
public abstract class AbstractExhaustibleResource implements ResourceType {
    @Override
    public Resource aggregate(Resource resourceA, Resource resourceB) {
        checkResource(resourceA);
        checkResource(resourceB);

        return new ResourceImpl(getId(), resourceA.getAmount() + resourceB.getAmount(), getDefaultUnit());
    }

    @Override
    public Resource deduct(Resource total, Resource deduction) throws NoEnoughResourcesException {
        checkResource(total);
        checkResource(deduction);

        final long resultAmount = total.getAmount() - deduction.getAmount();
        if (resultAmount < 0) {
            throw new NoEnoughResourcesException(total, deduction, new ResourceImpl(getId(), -resultAmount, getDefaultUnit()));
        }
        return new ResourceImpl(getId(), resultAmount, getDefaultUnit());
    }

    /**
     * Checks that given resources can be processed by this resource type
     *
     * @param resource
     *         resource to check
     * @throws IllegalArgumentException
     *         if given resources has unsupported type or unit
     */
    private void checkResource(Resource resource) {
        checkArgument(getId().equals(resource.getType()), "Resource should have '" + getId() + "' type");
        checkArgument(getSupportedUnits().contains(resource.getUnit()),
                      "Resource has unsupported unit '" + resource.getUnit() + "'");
    }
}
