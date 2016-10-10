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
package com.codenvy.resource.api.ram;

import com.codenvy.resource.model.ResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.ConflictException;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Describes RAM resource type and defines operations for aggregating and deduction.
 *
 * @author Sergii Leschenko
 */
public class RamResourceType implements ResourceType {
    public static final String ID   = "RAM";
    public static final String UNIT = "mb";

    private static final Set<String> SUPPORTED_UNITS = ImmutableSet.of(UNIT);

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return "Number of RAM which can be used by active workspaces at the same time";
    }

    @Override
    public Set<String> getSupportedUnits() {
        return SUPPORTED_UNITS;
    }

    @Override
    public ResourceImpl aggregate(ResourceImpl resourceA, ResourceImpl resourceB) {
        checkResource(resourceA);
        checkResource(resourceB);

        return new ResourceImpl(ID, resourceA.getAmount() + resourceB.getAmount(), UNIT);
    }

    @Override
    public ResourceImpl deduct(ResourceImpl total, ResourceImpl deduction) throws ConflictException {
        checkResource(total);
        checkResource(deduction);

        final long resultAmount = total.getAmount() - deduction.getAmount();
        if (resultAmount < 0) {
            throw new ConflictException(String.format("Workspace needs %s RAM to start - your account has %s RAM available.",
                                                      deduction.getAmount() + deduction.getUnit(),
                                                      total.getAmount() + total.getUnit()));
        }
        return new ResourceImpl(ID, resultAmount, UNIT);
    }

    /**
     * Checks that given resources can be processed by this resource type
     *
     * @param resource
     *         resource to check
     * @throws IllegalArgumentException
     *         if given resources has unsupported type or unit
     */
    private void checkResource(ResourceImpl resource) {
        checkArgument(ID.equals(resource.getType()), "Resource should have '" + ID + "' type");
        checkArgument(SUPPORTED_UNITS.contains(resource.getUnit()), "Resource has unsupported unit '" + resource.getUnit() + "'");
    }
}
