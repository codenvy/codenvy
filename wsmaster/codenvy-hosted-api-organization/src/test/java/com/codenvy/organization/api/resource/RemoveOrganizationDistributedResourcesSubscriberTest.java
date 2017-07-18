/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
