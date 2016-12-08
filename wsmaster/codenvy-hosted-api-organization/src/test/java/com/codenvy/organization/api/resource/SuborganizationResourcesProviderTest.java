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
import com.codenvy.organization.spi.impl.OrganizationDistributedResourcesImpl;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.model.ProvidedResources;
import com.codenvy.resource.spi.impl.ProvidedResourcesImpl;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Provider;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link SuborganizationResourcesProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class SuborganizationResourcesProviderTest {
    @Mock
    private Account      account;
    @Mock
    private Organization organization;

    @Mock
    private AccountManager                             accountManager;
    @Mock
    private OrganizationManager                        organizationManager;
    @Mock
    private OrganizationResourcesDistributor           resourcesDistributor;
    @Mock
    private Provider<OrganizationResourcesDistributor> distributorProvider;

    @InjectMocks
    private SuborganizationResourcesProvider suborganizationResourcesProvider;

    @BeforeMethod
    public void setUp() throws Exception {
        when(accountManager.getById(any())).thenReturn(account);
        when(organizationManager.getById(any())).thenReturn(organization);

        when(distributorProvider.get()).thenReturn(resourcesDistributor);
    }

    @Test
    public void shouldNotProvideResourcesForNonOrganizationalAccounts() throws Exception {
        //given
        when(account.getType()).thenReturn("test");

        //when
        final List<ProvidedResources> providedResources = suborganizationResourcesProvider.getResources("account123");

        //then
        assertTrue(providedResources.isEmpty());
        verify(accountManager).getById("account123");
    }

    @Test
    public void shouldNotProvideResourcesForRootOrganizationalAccount() throws Exception {
        //given
        when(account.getType()).thenReturn(OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
        when(organization.getParent()).thenReturn(null);

        //when
        final List<ProvidedResources> providedResources = suborganizationResourcesProvider.getResources("organization123");

        //then
        assertTrue(providedResources.isEmpty());
        verify(accountManager).getById("organization123");
        verify(organizationManager).getById("organization123");
    }

    @Test
    public void shouldProvideResourcesForOrganizationalAccount() throws Exception {
        //given
        when(account.getType()).thenReturn(OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
        when(organization.getParent()).thenReturn("parentOrg");
        final ResourceImpl resourceToProvide = new ResourceImpl("test",
                                                                1234,
                                                                "unit");
        final OrganizationDistributedResourcesImpl distributedResources = new OrganizationDistributedResourcesImpl("organization123",
                                                                                                                   singletonList(
                                                                                                                           resourceToProvide));
        when(resourcesDistributor.get(any())).thenReturn(distributedResources);

        //when
        final List<ProvidedResources> providedResources = suborganizationResourcesProvider.getResources("organization123");

        //then
        assertEquals(providedResources.size(), 1);
        assertEquals(providedResources.get(0), new ProvidedResourcesImpl(SuborganizationResourcesProvider.PARENT_RESOURCES_PROVIDER,
                                                                         null,
                                                                         "organization123",
                                                                         -1L,
                                                                         -1L,
                                                                         singletonList(resourceToProvide)));
        verify(accountManager).getById("organization123");
        verify(organizationManager).getById("organization123");
        verify(resourcesDistributor).get("organization123");
    }

    @Test
    public void shouldNotProvideResourcesForOrganizationalAccountIfItDoesNotHaveDistributedResources() throws Exception {
        //given
        when(account.getType()).thenReturn(OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
        when(organization.getParent()).thenReturn("parentOrg");
        when(resourcesDistributor.get(any())).thenThrow(new NotFoundException(""));

        //when
        final List<ProvidedResources> providedResources = suborganizationResourcesProvider.getResources("organization123");

        //then
        assertTrue(providedResources.isEmpty());
        verify(accountManager).getById("organization123");
        verify(organizationManager).getById("organization123");
        verify(resourcesDistributor).get("organization123");
    }
}
