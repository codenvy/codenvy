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
import com.codenvy.organization.shared.model.Organization;
import com.codenvy.resource.api.RamResourceType;
import com.codenvy.resource.api.RuntimeResourceType;
import com.codenvy.resource.api.WorkspaceResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link DefaultOrganizationResourcesProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class DefaultOrganizationResourcesProviderTest {
    @Mock
    private OrganizationManager organizationManager;
    @Mock
    private Organization        organization;

    private DefaultOrganizationResourcesProvider organizationResourcesProvider;

    @BeforeMethod
    public void setUp() throws Exception {
        organizationResourcesProvider = new DefaultOrganizationResourcesProvider(organizationManager,
                                                                                 "2gb",
                                                                                 10,
                                                                                 5);
        when(organizationManager.getById(anyString())).thenReturn(organization);
    }

    @Test
    public void shouldNotProvideDefaultResourcesForSuborganization() throws Exception {
        //given
        when(organization.getParent()).thenReturn("parentId");

        //when
        final List<ResourceImpl> defaultResources = organizationResourcesProvider.getResources("organization123");

        //then
        verify(organizationManager).getById("organization123");
        assertTrue(defaultResources.isEmpty());
    }

    @Test
    public void shouldProvideDefaultResourcesForRootOrganization() throws Exception {
        //given
        when(organization.getParent()).thenReturn(null);

        //when
        final List<ResourceImpl> defaultResources = organizationResourcesProvider.getResources("organization123");

        //then
        verify(organizationManager).getById("organization123");
        assertEquals(defaultResources.size(), 3);
        assertTrue(defaultResources.contains(new ResourceImpl(RamResourceType.ID,
                                                              2048,
                                                              RamResourceType.UNIT)));
        assertTrue(defaultResources.contains(new ResourceImpl(WorkspaceResourceType.ID,
                                                              10,
                                                              WorkspaceResourceType.UNIT)));
        assertTrue(defaultResources.contains(new ResourceImpl(RuntimeResourceType.ID,
                                                              5,
                                                              RuntimeResourceType.UNIT)));
    }
}
