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

import com.codenvy.organization.api.event.BeforeOrganizationRemovedEvent;
import com.codenvy.organization.spi.OrganizationDistributedResourcesDao;
import com.codenvy.organization.spi.impl.OrganizationImpl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.codenvy.organization.api.resource.OrganizationResourcesDistributor.RemoveOrganizationDistributedResourcesSubscriber}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RemoveOrganizationDistributedResourcesSubscriberTest {
    @Mock
    private OrganizationImpl                 organization;
    @Mock
    private OrganizationDistributedResourcesDao organizationDistributedResourcesDao;
    @InjectMocks
    private OrganizationResourcesDistributor organizationResourcesDistributor;

    private OrganizationResourcesDistributor.RemoveOrganizationDistributedResourcesSubscriber suborganizationsRemover;

    @BeforeMethod
    public void setUp() throws Exception {
        suborganizationsRemover = organizationResourcesDistributor.new RemoveOrganizationDistributedResourcesSubscriber();
    }

    @Test
    public void shouldResetResourcesDistributionBeforeSuborganizationRemoving() throws Exception {
        //given
        when(organization.getId()).thenReturn("suborg123");
        when(organization.getParent()).thenReturn("org123");

        //when
        suborganizationsRemover.onEvent(new BeforeOrganizationRemovedEvent(organization));

        //then
        verify(organizationDistributedResourcesDao).remove("suborg123");
    }

    @Test
    public void shouldNotResetResourcesDistributionBeforeRootOrganizationRemoving() throws Exception {
        //given
        when(organization.getId()).thenReturn("org123");
        when(organization.getParent()).thenReturn(null);

        //when
        suborganizationsRemover.onEvent(new BeforeOrganizationRemovedEvent(organization));

        //then
        verify(organizationDistributedResourcesDao, never()).remove("org123");
    }
}
