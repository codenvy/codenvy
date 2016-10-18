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
import com.codenvy.resource.spi.impl.ResourceImpl;
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
    ResourceType aResourceType;

    @Mock
    ResourceType bResourceType;

    @Mock
    ResourceType cResourceType;

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
        final ResourceImpl aResource = new ResourceImpl(A_RESOURCE_TYPE, 123, "unit");
        final ResourceImpl bResource = new ResourceImpl(B_RESOURCE_TYPE, 123, "unit");
        final ResourceImpl anotherBResource = new ResourceImpl(B_RESOURCE_TYPE, 321, "unit");
        final ResourceImpl aggregatedBResources = new ResourceImpl(B_RESOURCE_TYPE, 444, "unit");
        when(bResourceType.aggregate(any(), any())).thenReturn(aggregatedBResources);

        final Map<String, ResourceImpl> aggregatedResources =
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
        final ResourceImpl aResource = new ResourceImpl(A_RESOURCE_TYPE, 123, "unit");
        final ResourceImpl bResource = new ResourceImpl(B_RESOURCE_TYPE, 123, "unit");
        final ResourceImpl anotherBResource = new ResourceImpl(B_RESOURCE_TYPE, 321, "unit");
        final ResourceImpl aggregatedBResources = new ResourceImpl(A_RESOURCE_TYPE, 444, "unit");
        when(bResourceType.deduct(any(), any())).thenReturn(aggregatedBResources);

        final List<? extends Resource> deductedResources = resourceAggregator.deduct(asList(aResource, bResource),
                                                                                     singletonList(anotherBResource));

        verify(bResourceType).deduct(eq(bResource), eq(anotherBResource));
        verify(aResourceType, never()).deduct(any(), any());

        assertEquals(deductedResources.size(), 2);
        assertTrue(deductedResources.contains(aResource));
        assertTrue(deductedResources.contains(aggregatedBResources));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "No enough resources")
    public void shouldThrowConflictExceptionWhenTotalResourcesDoNotHaveEnoughtAmoutToDeduct() throws Exception {
        final ResourceImpl aResource = new ResourceImpl(A_RESOURCE_TYPE, 123, "unit");
        final ResourceImpl anotherAResource = new ResourceImpl(A_RESOURCE_TYPE, 321, "unit");
        when(aResourceType.deduct(any(), any())).thenThrow(new ConflictException("No enough resources"));

        resourceAggregator.deduct(singletonList(aResource), singletonList(anotherAResource));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Your account doesn't have resourceB resource to use.")
    public void shouldThrowConflictExceptionWhenTotalResourcesDoNotContainsRequiredResourcesAtAll() throws Exception {
        final ResourceImpl aResource = new ResourceImpl(A_RESOURCE_TYPE, 123, "unit");
        final ResourceImpl bResource = new ResourceImpl(B_RESOURCE_TYPE, 321, "unit");

        resourceAggregator.deduct(singletonList(aResource), singletonList(bResource));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenTryingToAggregateNotSupportedResource() throws Exception {
        final ResourceImpl dResource = mock(ResourceImpl.class);
        when(dResource.getType()).thenReturn("resourceD");

        resourceAggregator.aggregateByType(singletonList(dResource));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenTryingToAggregateNotSupportedResources() throws Exception {
        final ResourceImpl dResource = mock(ResourceImpl.class);
        final ResourceImpl anotherDResource = mock(ResourceImpl.class);
        when(dResource.getType()).thenReturn("resourceD");
        when(anotherDResource.getType()).thenReturn("resourceD");

        resourceAggregator.aggregateByType(asList(dResource, anotherDResource));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenTotalResourcesListContainsNotSupportedResourceOnResourcesDeduction() throws Exception {
        final ResourceImpl dResource = mock(ResourceImpl.class);
        when(dResource.getType()).thenReturn("resourceD");

        resourceAggregator.deduct(emptyList(), singletonList(dResource));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenResourcesToDeductListContainsNotSupportedResourceOnResourcesDeduction() throws Exception {
        final ResourceImpl dResource = mock(ResourceImpl.class);
        when(dResource.getType()).thenReturn("resourceD");

        resourceAggregator.deduct(singletonList(dResource), emptyList());
    }
}
