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
package com.codenvy.organization.api.resource;

import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.shared.model.OrganizationDistributedResources;
import com.codenvy.organization.spi.OrganizationDistributedResourcesDao;
import com.codenvy.organization.spi.impl.OrganizationDistributedResourcesImpl;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.api.ResourceAggregator;
import com.codenvy.resource.api.exception.NoEnoughResourcesException;
import com.codenvy.resource.api.usage.ResourceUsageManager;
import com.codenvy.resource.api.usage.ResourcesLocks;
import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.commons.lang.concurrent.CloseableLock;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link OrganizationResourcesDistributor}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationResourcesDistributorTest {
    private static final String PARENT_ORG_ID = "parentOrg123";
    private static final String ORG_ID        = "organization123";

    @Mock
    private CloseableLock                       lock;
    @Mock
    private OrganizationDistributedResourcesDao distributedResourcesDao;
    @Mock
    private ResourcesLocks                      resourcesLocks;
    @Mock
    private ResourceUsageManager                usageManager;
    @Mock
    private ResourceAggregator                  resourceAggregator;
    @Mock
    private OrganizationManager                 organizationManager;

    @Spy
    @InjectMocks
    private OrganizationResourcesDistributor manager;

    @BeforeMethod
    public void setUp() throws Exception {
        doNothing().when(manager).checkResourcesAvailability(anyString(), anyString(), any(), any());
        when(resourcesLocks.acquiresLock(anyString())).thenReturn(lock);

        when(organizationManager.getById(ORG_ID)).thenReturn(new OrganizationImpl(ORG_ID, ORG_ID + "name", PARENT_ORG_ID));
        when(organizationManager.getById(PARENT_ORG_ID)).thenReturn(new OrganizationImpl(PARENT_ORG_ID, PARENT_ORG_ID + "name", null));
    }

    @Test
    public void shouldDistributeResources() throws Exception {
        doThrow(new NotFoundException("no distributed resources"))
                .when(distributedResourcesDao).get(anyString());
        List<ResourceImpl> toDistribute = singletonList(createTestResource(1000));

        //when
        manager.distribute(ORG_ID, toDistribute);

        //then
        verify(distributedResourcesDao).get(ORG_ID);
        verify(manager).checkResourcesAvailability(ORG_ID,
                                                   PARENT_ORG_ID,
                                                   emptyList(),
                                                   toDistribute);
        verify(distributedResourcesDao).store(new OrganizationDistributedResourcesImpl(ORG_ID,
                                                                                       toDistribute));
        verify(resourcesLocks).acquiresLock(ORG_ID);
        verify(lock).close();
    }

    @Test
    public void shouldDistributeResourcesWhenThereIsOldOne() throws Exception {
        //given
        final OrganizationDistributedResourcesImpl distributedResources = createDistributedResources(500);
        when(distributedResourcesDao.get(anyString())).thenReturn(distributedResources);
        List<ResourceImpl> toDistribute = singletonList(createTestResource(1000));

        //when
        manager.distribute(ORG_ID, toDistribute);

        //then
        verify(distributedResourcesDao).get(ORG_ID);
        verify(manager).checkResourcesAvailability(ORG_ID,
                                                   PARENT_ORG_ID,
                                                   distributedResources.getResources(),
                                                   toDistribute);
        verify(distributedResourcesDao).store(new OrganizationDistributedResourcesImpl(ORG_ID,
                                                                                       toDistribute));
        verify(resourcesLocks).acquiresLock(ORG_ID);
        verify(lock).close();
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "It is not allowed to distribute resources for root organization.")
    public void shouldThrowConflictExceptionOnDistributingResourcesForRootOrganization() throws Exception {
        //given
        final OrganizationDistributedResourcesImpl distributedResources = createDistributedResources(500);
        when(distributedResourcesDao.get(anyString())).thenReturn(distributedResources);
        List<ResourceImpl> toDistribute = singletonList(createTestResource(1000));

        //when
        manager.distribute(PARENT_ORG_ID, toDistribute);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnDistributionResourcesWithNullOrganizationId() throws Exception {
        //when
        manager.distribute(null, Collections.emptyList());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnDistributionNullResourcesList() throws Exception {
        //when
        manager.distribute(ORG_ID, null);
    }

    @Test
    public void shouldGetDistributedResources() throws Exception {
        //given
        final OrganizationDistributedResourcesImpl distributedResources = createDistributedResources(1000);
        when(distributedResourcesDao.get(anyString())).thenReturn(distributedResources);

        //when
        final OrganizationDistributedResources fetchedDistributedResources = manager.get(ORG_ID);

        //then
        assertEquals(fetchedDistributedResources, distributedResources);
        verify(distributedResourcesDao).get(ORG_ID);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGettingDistributedResourcesByNullOrganizationId() throws Exception {
        //when
        manager.get(null);
    }

    @Test
    public void shouldGetDistributedResourcesByParentOrganizationId() throws Exception {
        //given
        final Page<OrganizationDistributedResourcesImpl> existedPage = new Page<>(singletonList(createDistributedResources(1000)), 2, 1, 4);
        when(distributedResourcesDao.getByParent(anyString(), anyInt(), anyLong())).thenReturn(existedPage);

        //when
        final Page<? extends OrganizationDistributedResources> fetchedPage = manager.getByParent(PARENT_ORG_ID, 1, 2);

        //then
        assertEquals(fetchedPage, existedPage);
        verify(distributedResourcesDao).getByParent(PARENT_ORG_ID, 1, 2L);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGettingDistributedResourcesByNullParentOrganizationId() throws Exception {
        //when
        manager.getByParent(null, 1, 1);
    }

    @Test
    public void shouldResetDistributedResources() throws Exception {
        //given
        final OrganizationDistributedResourcesImpl distributedResources = createDistributedResources(500);
        when(distributedResourcesDao.get(anyString())).thenReturn(distributedResources);

        //when
        manager.reset(ORG_ID);

        //then
        verify(distributedResourcesDao).get(ORG_ID);
        verify(manager).checkResourcesAvailability(ORG_ID,
                                                   PARENT_ORG_ID,
                                                   distributedResources.getResources(),
                                                   emptyList());
        verify(distributedResourcesDao).remove(ORG_ID);
        verify(resourcesLocks).acquiresLock(ORG_ID);
        verify(lock).close();
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "It is not allowed to distribute resources for root organization.")
    public void shouldThrowConflictExceptionOnResettingDistributedResourcesForRootOrganization() throws Exception {
        //when
        manager.reset(PARENT_ORG_ID);
    }

    @Test
    public void shouldNotThrowNotFoundExceptionOnDistributedResourcesResettingWhenThereAreAlreadyReset() throws Exception {
        //given
        when(distributedResourcesDao.get(anyString())).thenThrow(new NotFoundException("no distributed resources"));

        //when
        manager.reset(ORG_ID);

        //then
        verify(distributedResourcesDao).get(ORG_ID);
        verify(manager).checkResourcesAvailability(ORG_ID,
                                                   PARENT_ORG_ID,
                                                   emptyList(),
                                                   emptyList());
        verify(distributedResourcesDao).remove(ORG_ID);
        verify(resourcesLocks).acquiresLock(ORG_ID);
        verify(lock).close();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnResettingDistributedResourcesByNullOrganizationId() throws Exception {
        //when
        manager.reset(null);
    }

    @Test
    public void shouldCheckTestResourceDifferenceAvailabilityOnParenOrganizationLevelOnAmountIncreasing() throws Exception {
        //given
        doCallRealMethod().when(manager).checkResourcesAvailability(anyString(), anyString(), any(), any());
        ResourceImpl distributed = createTestResource(700);
        ResourceImpl toDistribute = createTestResource(1000);
        doThrow(new NoEnoughResourcesException(distributed, toDistribute, createTestResource(300)))
                .when(resourceAggregator).deduct((Resource)any(), any());

        //when
        manager.checkResourcesAvailability(ORG_ID,
                                           PARENT_ORG_ID,
                                           singletonList(distributed),
                                           singletonList(toDistribute));

        //then
        verify(usageManager).checkResourcesAvailability(PARENT_ORG_ID, singletonList(createTestResource(300)));
    }

    @Test
    public void shouldCheckTestResourceDifferenceAvailabilityOnSuborganizationLevelOnAmountDecreasing() throws Exception {
        //given
        doCallRealMethod().when(manager).checkResourcesAvailability(anyString(), anyString(), any(), any());
        ResourceImpl distributed = createTestResource(1000);
        ResourceImpl toDistribute = createTestResource(700);
        doReturn(createTestResource(300))
                .when(resourceAggregator).deduct((Resource)any(), any());

        //when
        manager.checkResourcesAvailability(ORG_ID,
                                           PARENT_ORG_ID,
                                           singletonList(distributed),
                                           singletonList(toDistribute));

        //then
        verify(usageManager).checkResourcesAvailability(ORG_ID, singletonList(createTestResource(300)));
    }

    @Test
    public void shouldCheckTestResourceAvailabilityOnParentOrganizationLevelOnInitialDistribution() throws Exception {
        //given
        doCallRealMethod().when(manager).checkResourcesAvailability(anyString(), anyString(), any(), any());
        ResourceImpl toDistribute = createTestResource(700);

        //when
        manager.checkResourcesAvailability(ORG_ID,
                                           PARENT_ORG_ID,
                                           emptyList(),
                                           singletonList(toDistribute));

        //then
        verify(usageManager).checkResourcesAvailability(PARENT_ORG_ID, singletonList(toDistribute));
    }

    @Test
    public void shouldCheckTestResourceAvailabilityOnParentOrganizationLevelOnTestResourceDistributionResetting() throws Exception {
        //given
        doCallRealMethod().when(manager).checkResourcesAvailability(anyString(), anyString(), any(), any());
        ResourceImpl distributed = createTestResource(700);

        //when
        manager.checkResourcesAvailability(ORG_ID,
                                           PARENT_ORG_ID,
                                           singletonList(distributed),
                                           emptyList());

        //then
        verify(usageManager).checkResourcesAvailability(ORG_ID, singletonList(distributed));
    }

    private ResourceImpl createTestResource(long amount) {
        return new ResourceImpl("test",
                                amount,
                                "init");
    }

    private OrganizationDistributedResourcesImpl createDistributedResources(long resourceAmount) {
        return new OrganizationDistributedResourcesImpl(ORG_ID,
                                                        singletonList(createTestResource(resourceAmount)));
    }
}
