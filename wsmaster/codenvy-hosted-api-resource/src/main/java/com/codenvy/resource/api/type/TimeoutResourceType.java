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
package com.codenvy.resource.api.type;

import com.codenvy.resource.api.exception.NoEnoughResourcesException;
import com.codenvy.resource.model.Resource;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Describes resource type that control the length of time
 * that a user is idle with their workspace when the system
 * will suspend the workspace by snapshotting it and then stopping it.
 *
 * @author Sergii Leschenko
 */
public class TimeoutResourceType implements ResourceType {
    public static final String ID   = "timeout";
    public static final String UNIT = "minute";

    private static final Set<String> SUPPORTED_UNITS = ImmutableSet.of(UNIT);

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return "Timeout";
    }

    @Override
    public Set<String> getSupportedUnits() {
        return SUPPORTED_UNITS;
    }

    @Override
    public String getDefaultUnit() {
        return UNIT;
    }

    @Override
    public Resource aggregate(Resource resourceA, Resource resourceB) {
        return resourceA.getAmount() > resourceB.getAmount() ? resourceA : resourceB;
    }

    @Override
    public Resource deduct(Resource total, Resource deduction) throws NoEnoughResourcesException {
        return total;
    }
}
