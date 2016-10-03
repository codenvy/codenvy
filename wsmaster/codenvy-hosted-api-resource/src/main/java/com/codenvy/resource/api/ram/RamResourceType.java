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

import org.eclipse.che.api.core.ConflictException;

/**
 * Describes {@link RamResource} and defines operations for aggregating and deduction
 *
 * @author Sergii Leschenko
 */
public class RamResourceType implements ResourceType<RamResource> {
    public static final String ID   = "RAM";
    public static final String UNIT = "mb";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return "Number of RAM which can be used by active workspaces at the same time";
    }

    @Override
    public RamResource aggregate(RamResource resourceA, RamResource resourceB) {
        return new RamResource(resourceA.getAmount() + resourceB.getAmount());
    }

    @Override
    public RamResource deduct(RamResource total, RamResource deduction) throws ConflictException {
        final long resultAmount = total.getAmount() - deduction.getAmount();
        if (resultAmount < 0) {
            throw new ConflictException(String.format("Workspace needs %s RAM to start - your account has %s RAM available.",
                                                      total.getAmount() + total.getUnit(),
                                                      deduction.getAmount() + deduction.getUnit()));
        }
        return new RamResource(resultAmount);
    }
}
