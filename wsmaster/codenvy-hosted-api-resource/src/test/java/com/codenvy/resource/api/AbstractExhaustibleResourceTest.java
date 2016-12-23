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
import com.codenvy.resource.spi.impl.ResourceImpl;
import com.google.common.collect.ImmutableSet;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Set;

import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link AbstractExhaustibleResource}
 *
 * @author Sergii Leschenko
 */
public class AbstractExhaustibleResourceTest {
    private AbstractExhaustibleResource resourceType;

    @BeforeMethod
    public void setUp() throws Exception {
        resourceType = new TestResourceType();
    }

    @Test
    public void shouldFindSumResourcesAmountsOnResourcesAggregation() throws Exception {
        final Resource aggregate = resourceType.aggregate(new ResourceImpl(TestResourceType.ID,
                                                                           1000,
                                                                           TestResourceType.UNIT),
                                                          new ResourceImpl(TestResourceType.ID,
                                                                           500,
                                                                           TestResourceType.UNIT));

        assertEquals(aggregate.getType(), TestResourceType.ID);
        assertEquals(aggregate.getAmount(), 1500);
        assertEquals(aggregate.getUnit(), TestResourceType.UNIT);
    }

    @Test
    public void shouldFindDifferenceResourcesAmountsOnResourcesDeduction() throws Exception {
        final Resource deducted = resourceType.deduct(new ResourceImpl(TestResourceType.ID,
                                                                       1000,
                                                                       TestResourceType.UNIT),
                                                      new ResourceImpl(TestResourceType.ID,
                                                                       500,
                                                                       TestResourceType.UNIT));

        assertEquals(deducted.getType(), TestResourceType.ID);
        assertEquals(deducted.getAmount(), 500);
        assertEquals(deducted.getUnit(), TestResourceType.UNIT);
    }

    @Test
    public void shouldThrowConflictExceptionWhenDeductionAmountMoreThanTotalAmountOnResourcesDeduction() throws Exception {
        try {
            resourceType.deduct(new ResourceImpl(TestResourceType.ID,
                                                 300,
                                                 TestResourceType.UNIT),
                                new ResourceImpl(TestResourceType.ID,
                                                 1000,
                                                 TestResourceType.UNIT));
        } catch (NoEnoughResourcesException e) {
            assertEquals(e.getMissingResources(), Collections.singletonList(new ResourceImpl(TestResourceType.ID,
                                                                                             700,
                                                                                             TestResourceType.UNIT)));
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          dataProvider = "getResources")
    public void shouldThrowIllegalArgumentExceptionWhenOneOfResourcesHasUnsupportedTypeOrUnitOnResourcesAggregation(ResourceImpl resourceA,
                                                                                                                    ResourceImpl resourceB) {
        resourceType.aggregate(resourceA, resourceB);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          dataProvider = "getResources")
    public void shouldThrowIllegalArgumentExceptionWhenOneOfResourcesHasUnsupportedTypeOrUnitOnResourcesDeduction(ResourceImpl resourceA,
                                                                                                                  ResourceImpl resourceB) {
        resourceType.aggregate(resourceA, resourceB);
    }

    @DataProvider(name = "resources")
    public Object[][] getResources() {
        return new Object[][] {
                {new ResourceImpl("unsupported", 1000, TestResourceType.UNIT),
                 new ResourceImpl(TestResourceType.ID, 1000, TestResourceType.UNIT)},
                {new ResourceImpl(TestResourceType.ID, 1000, TestResourceType.UNIT),
                 new ResourceImpl("unsupported", 1000, TestResourceType.UNIT)},
                {new ResourceImpl(TestResourceType.ID, 1000, "unsupported"),
                 new ResourceImpl(TestResourceType.ID, 1000, TestResourceType.UNIT)},
                {new ResourceImpl(TestResourceType.ID, 1000, TestResourceType.UNIT),
                 new ResourceImpl(TestResourceType.ID, 1000, "unsupported")}
        };
    }

    private static class TestResourceType extends AbstractExhaustibleResource {
        private static final String ID   = "testResource";
        private static final String UNIT = "testUnit";

        @Override
        public String getId() {
            return ID;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public Set<String> getSupportedUnits() {
            return ImmutableSet.of(UNIT);
        }

        @Override
        public String getDefaultUnit() {
            return UNIT;
        }
    }
}
