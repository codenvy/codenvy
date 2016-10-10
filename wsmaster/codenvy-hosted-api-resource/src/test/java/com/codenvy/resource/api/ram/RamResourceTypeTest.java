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

import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.api.core.ConflictException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
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
        final ResourceImpl aggregate = resourceType.aggregate(new ResourceImpl(RamResourceType.ID,
                                                                               1000,
                                                                               RamResourceType.UNIT),
                                                              new ResourceImpl(RamResourceType.ID,
                                                                               500,
                                                                               RamResourceType.UNIT));

        assertEquals(aggregate.getType(), RamResourceType.ID);
        assertEquals(aggregate.getAmount(), 1500);
        assertEquals(aggregate.getUnit(), RamResourceType.UNIT);
    }

    @Test
    public void shouldFindDifferenceRamAmountOnResourcesDeduction() throws Exception {
        final ResourceImpl deducted = resourceType.deduct(new ResourceImpl(RamResourceType.ID,
                                                                           1000,
                                                                           RamResourceType.UNIT),
                                                          new ResourceImpl(RamResourceType.ID,
                                                                           500,
                                                                           RamResourceType.UNIT));

        assertEquals(deducted.getType(), RamResourceType.ID);
        assertEquals(deducted.getAmount(), 500);
        assertEquals(deducted.getUnit(), RamResourceType.UNIT);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Workspace needs 1000mb RAM to start - your account has 500mb RAM available.")
    public void shouldThrowConflictExceptionWhenDeductionAmountMoreThanTotalAmountOnResourcesDeduction() throws Exception {
        resourceType.deduct(new ResourceImpl(RamResourceType.ID,
                                             500,
                                             RamResourceType.UNIT),
                            new ResourceImpl(RamResourceType.ID,
                                             1000,
                                             RamResourceType.UNIT));
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          dataProvider = "resources")
    public void shouldThrowIllegalArgumentExceptionWhenOneOfResourcesHasUnsupportedTypeOrUnitOnResourcesAggregation(ResourceImpl resourceA,
                                                                                                                    ResourceImpl resourceB) {
        resourceType.aggregate(resourceA, resourceB);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          dataProvider = "resources")
    public void shouldThrowIllegalArgumentExceptionWhenOneOfResourcesHasUnsupportedTypeOrUnitOnResourcesDeduction(ResourceImpl resourceA,
                                                                                                                  ResourceImpl resourceB) {
        resourceType.aggregate(resourceA, resourceB);
    }

    @DataProvider(name = "resources")
    public Object[][] getWorkspaceStatus() {
        return new Object[][] {
                {new ResourceImpl("unsupported", 1000, RamResourceType.UNIT),
                 new ResourceImpl(RamResourceType.ID, 1000, RamResourceType.UNIT)},
                {new ResourceImpl(RamResourceType.ID, 1000, RamResourceType.UNIT),
                 new ResourceImpl("unsupported", 1000, RamResourceType.UNIT)},
                {new ResourceImpl(RamResourceType.ID, 1000, "unsupported"),
                 new ResourceImpl(RamResourceType.ID, 1000, RamResourceType.UNIT)},
                {new ResourceImpl(RamResourceType.ID, 1000, RamResourceType.UNIT),
                 new ResourceImpl(RamResourceType.ID, 1000, "unsupported")}
        };
    }
}
