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
package com.codenvy.resource.api.usage;

import com.codenvy.resource.api.ResourceAggregator;
import com.codenvy.resource.api.exception.NoEnoughResourcesException;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Test for {@link DefaultAvailableResourcesProvider}
 */
@Listeners(MockitoTestNGListener.class)
public class DefaultAvailableResourcesProviderTest {
    @Mock
    private Provider<ResourceUsageManager> resourceUsageManagerProvider;
    @Mock
    private ResourceUsageManager           resourceUsageManager;
    @Mock
    private ResourceAggregator             resourceAggregator;

    @InjectMocks
    private DefaultAvailableResourcesProvider defaultAvailableResourcesProvider;

    @BeforeMethod
    public void setUp() throws Exception {
        when(resourceUsageManagerProvider.get()).thenReturn(resourceUsageManager);
    }

    @Test
    public void shouldReturnAvailableResourcesWhenNotAllTotalResourcesAreUsed() throws Exception {
        //given
        List<ResourceImpl> totalResources = singletonList(new ResourceImpl("test", 5000, "unit"));
        doReturn(totalResources).when(resourceUsageManager).getTotalResources(anyString());
        List<ResourceImpl> usedResources = singletonList(new ResourceImpl("test", 2000, "unit"));
        doReturn(usedResources).when(resourceUsageManager).getUsedResources(anyString());
        ResourceImpl availableResource = new ResourceImpl("test", 3000, "unit");
        doReturn(singletonList(availableResource)).when(resourceAggregator).deduct(anyList(), anyList());

        //when
        List<? extends Resource> availableResources = defaultAvailableResourcesProvider.getAvailableResources("account123");

        //then
        assertEquals(availableResources.size(), 1);
        assertEquals(availableResources.get(0), availableResource);
        verify(resourceUsageManager).getTotalResources("account123");
        verify(resourceUsageManager).getUsedResources("account123");
        verify(resourceAggregator).deduct(totalResources, usedResources);
        verify(resourceAggregator, never()).excess(anyList(), anyList());
    }

    @Test
    public void shouldReturnExcessiveResourcesWhenNotOneResourceIsUsedButNotPresentInTotal() throws Exception {
        //given
        List<ResourceImpl> totalResources = singletonList(new ResourceImpl("test", 5000, "unit"));
        doReturn(totalResources).when(resourceUsageManager).getTotalResources(anyString());
        List<ResourceImpl> usedResources = Arrays.asList(new ResourceImpl("test", 2000, "unit"),
                                                         new ResourceImpl("test2", 5, "unit"));
        doReturn(usedResources).when(resourceUsageManager).getUsedResources(anyString());
        doThrow(new NoEnoughResourcesException(emptyList(), emptyList(), emptyList()))
                .when(resourceAggregator).deduct(anyList(), anyList());
        ResourceImpl excessiveResource = new ResourceImpl("test", 3000, "unit");
        doReturn(singletonList(excessiveResource)).when(resourceAggregator).excess(anyList(), anyList());

        //when
        List<? extends Resource> availableResources = defaultAvailableResourcesProvider.getAvailableResources("account123");

        //then
        assertEquals(availableResources.size(), 1);
        assertEquals(availableResources.get(0), excessiveResource);
        verify(resourceUsageManager).getTotalResources("account123");
        verify(resourceUsageManager).getUsedResources("account123");
        verify(resourceAggregator).deduct(totalResources, usedResources);
        verify(resourceAggregator).excess(totalResources, usedResources);
    }
}
