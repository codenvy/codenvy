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

import com.codenvy.resource.model.Resource;
import com.codenvy.resource.model.ResourceType;
import com.codenvy.resource.spi.impl.AbstractResource;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link ResourceAggregator}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class ResourceAggregatorTest {
    private static final String A_RESOURCE_TYPE = "resourceA";
    private static final String B_RESOURCE_TYPE = "resourceB";
    private static final String C_RESOURCE_TYPE = "resourceC";
    @Mock
    ResourceType<AResource> aResourceType;

    @Mock
    ResourceType<BResource> bResourceType;

    @Mock
    ResourceType<BResource> cResourceType;

    private ResourceAggregator resourceAggregator;

    @BeforeMethod
    public void setUp() throws Exception {
        when(aResourceType.getId()).thenReturn(A_RESOURCE_TYPE);
        when(bResourceType.getId()).thenReturn(B_RESOURCE_TYPE);
        when(cResourceType.getId()).thenReturn(C_RESOURCE_TYPE);

        resourceAggregator = new ResourceAggregator(ImmutableSet.of(aResourceType, bResourceType, cResourceType));
    }

    @Test
    public void shouldTestResourcesAggregationByTypes() throws Exception {
        final AResource aResource = new AResource();
        final BResource bResource = new BResource();
        final BResource anotherBResource = new BResource();
        final BResource aggregatedBResources = new BResource();
        when(bResourceType.aggregate(any(), any())).thenReturn(aggregatedBResources);

        final Map<String, AbstractResource> aggregatedResources =
                resourceAggregator.aggregateByType(asList(aResource, bResource, anotherBResource));

        verify(bResourceType).aggregate(eq(bResource), eq(anotherBResource));
        verify(aResourceType, never()).aggregate(any(), any());

        assertEquals(aggregatedResources.size(), 2);

        assertTrue(aggregatedResources.containsKey(A_RESOURCE_TYPE));
        assertTrue(aggregatedResources.containsValue(aResource));

        assertTrue(aggregatedResources.containsKey(B_RESOURCE_TYPE));
        assertTrue(aggregatedResources.containsValue(aggregatedBResources));
    }

    @Test
    public void shouldTestResourcesDeduction() throws Exception {
        final AResource aResource = new AResource();
        final BResource bResource = new BResource();
        final CResource cResource = new CResource();
        final BResource anotherBResource = new BResource();
        final BResource aggregatedBResources = new BResource();
        when(bResourceType.deduct(any(), any())).thenReturn(aggregatedBResources);

        final List<? extends Resource> deductedResources = resourceAggregator.deduct(asList(aResource, bResource),
                                                                                    asList(anotherBResource, cResource));

        verify(bResourceType).deduct(eq(bResource), eq(anotherBResource));
        verify(aResourceType, never()).deduct(any(), any());

        assertEquals(deductedResources.size(), 2);
        assertTrue(deductedResources.contains(aResource));
        assertTrue(deductedResources.contains(aggregatedBResources));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "No enough resources")
    public void shouldThrowConflictExceptionWhenTotalResourcesDoNotHaveEnoughtAmoutToDeduct() throws Exception {
        final AResource aResource = new AResource();
        final AResource anotherAResource = new AResource();
        when(aResourceType.deduct(any(), any())).thenThrow(new ConflictException("No enough resources"));

        resourceAggregator.deduct(singletonList(aResource), singletonList(anotherAResource));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenTryingToAggregateNotSupportedResource() throws Exception {
        final AbstractResource dResource = mock(AbstractResource.class);
        when(dResource.getType()).thenReturn("resourceD");

        resourceAggregator.aggregateByType(singletonList(dResource));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenTryingToAggregateNotSupportedResources() throws Exception {
        final AbstractResource dResource = mock(AbstractResource.class);
        final AbstractResource anotherDResource = mock(AbstractResource.class);
        when(dResource.getType()).thenReturn("resourceD");
        when(anotherDResource.getType()).thenReturn("resourceD");

        resourceAggregator.aggregateByType(asList(dResource, anotherDResource));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenTotalResourcesListContainsNotSupportedResourceOnResourcesDeduction() throws Exception {
        final AbstractResource dResource = mock(AbstractResource.class);
        when(dResource.getType()).thenReturn("resourceD");

        resourceAggregator.deduct(emptyList(), singletonList(dResource));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenResourcesToDeductListContainsNotSupportedResourceOnResourcesDeduction() throws Exception {
        final AbstractResource dResource = mock(AbstractResource.class);
        when(dResource.getType()).thenReturn("resourceD");

        resourceAggregator.deduct(singletonList(dResource), emptyList());
    }

    private static class AResource extends AbstractResource {
        @Override
        public String getType() {
            return A_RESOURCE_TYPE;
        }

        @Override
        public String getUnit() {
            return null;
        }
    }

    private static class BResource extends AbstractResource {
        @Override
        public String getType() {
            return B_RESOURCE_TYPE;
        }

        @Override
        public String getUnit() {
            return null;
        }
    }

    private static class CResource extends AbstractResource {
        @Override
        public String getType() {
            return C_RESOURCE_TYPE;
        }

        @Override
        public String getUnit() {
            return null;
        }
    }
}
