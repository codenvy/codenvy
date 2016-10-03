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

import org.eclipse.che.api.core.ConflictException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link RamResourceType}
 *
 * @author Sergii Leschenko
 */
public class RamResourceTypeTest {
    private RamResourceType resourceType;

    @BeforeMethod
    public void setUp() throws Exception {
        resourceType = new RamResourceType();
    }

    @Test
    public void shouldFindSumRamAmountOnResourcesAggregation() throws Exception {
        final RamResource aggregate = resourceType.aggregate(new RamResource(1000), new RamResource(500));

        assertEquals(aggregate.getAmount(), 1500);
    }

    @Test
    public void shouldFindDifferenceRamAmountOnResourcesDeduction() throws Exception {
        final RamResource aggregate = resourceType.deduct(new RamResource(1000), new RamResource(500));

        assertEquals(aggregate.getAmount(), 500);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenDeductionAmountMoreThanTotalAmountOnResourcesDeduction() throws Exception {
        resourceType.deduct(new RamResource(500), new RamResource(1000));
    }
}
