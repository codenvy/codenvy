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
package com.codenvy.organization.api.resource;

import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.api.ResourceAggregator;
import com.codenvy.resource.api.type.RamResourceType;
import com.codenvy.resource.api.type.WorkspaceResourceType;
import com.codenvy.resource.api.usage.ResourceUsageManager;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.ResourceImpl;
import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.Page;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link OrganizationResourcesReserveTracker}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationResourcesReserveTrackerTest {
    private static String PARENT_ID = "organization123";
    private static String SUBORG_ID = "organization321";
    @Captor
    private ArgumentCaptor<List<? extends Resource>> resourcesToAggregateCaptor;
    @Mock
    private Provider<ResourceUsageManager>           managerProvider;
    @Mock
    private ResourceUsageManager                     resourceUsageManager;
    @Mock
    private ResourceAggregator                       resourceAggregator;
    @Mock
    private OrganizationManager                      organizationManager;

    @InjectMocks
    private OrganizationResourcesReserveTracker resourcesReserveTracker;

    @BeforeMethod
    public void setUp() throws Exception {
        when(managerProvider.get()).thenReturn(resourceUsageManager);
    }

    @Test
    public void shouldReturnSumOfSuborganizationsUsedAndReservedResourcesWhenGettingReservedResources() throws Exception {
        //given
        doReturn(new Page<>(singletonList(new OrganizationImpl(SUBORG_ID, "suborgname", PARENT_ID)), 0, 10, 1))
                .when(organizationManager).getByParent(anyString(), anyInt(), anyLong());

        final ResourceImpl usedWorkspaceResource = new ResourceImpl(WorkspaceResourceType.ID, 1, WorkspaceResourceType.UNIT);
        final ResourceImpl usedRamResource = new ResourceImpl(RamResourceType.ID, 1200, RamResourceType.UNIT);
        final ResourceImpl reservedRamResource = new ResourceImpl(RamResourceType.ID, 800, RamResourceType.UNIT);
        final ResourceImpl aggregatedRAM = new ResourceImpl(RamResourceType.ID, 2000, RamResourceType.UNIT);

        doReturn(ImmutableMap.of(RamResourceType.ID, aggregatedRAM,
                                 WorkspaceResourceType.ID, usedWorkspaceResource))
                .when(resourceAggregator).aggregateByType(any());
        doReturn(Arrays.asList(usedWorkspaceResource, usedRamResource))
                .when(resourceUsageManager).getUsedResources(anyString());
        doReturn(Arrays.asList(reservedRamResource))
                .when(resourceUsageManager).getReservedResources(anyString());

        //when
        final List<? extends Resource> reservedResources = resourcesReserveTracker.getReservedResources(PARENT_ID);

        //then
        verify(resourceAggregator).aggregateByType(resourcesToAggregateCaptor.capture());
        final List<? extends Resource> resourcesToAggregate = resourcesToAggregateCaptor.getValue();
        assertTrue(resourcesToAggregate.contains(usedRamResource));
        assertTrue(resourcesToAggregate.contains(reservedRamResource));
        assertTrue(resourcesToAggregate.contains(usedWorkspaceResource));

        verify(resourceUsageManager).getUsedResources(SUBORG_ID);
        verify(resourceUsageManager).getReservedResources(SUBORG_ID);
        assertEquals(reservedResources.size(), 2);
        assertTrue(reservedResources.contains(usedWorkspaceResource));
        assertTrue(reservedResources.contains(aggregatedRAM));
        verify(organizationManager).getByParent(eq(PARENT_ID), anyInt(), anyLong());
    }

    @Test
    public void shouldReturnOrganizationalAccountType() throws Exception {
        final String accountType = resourcesReserveTracker.getAccountType();

        assertEquals(accountType, OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
    }
}
