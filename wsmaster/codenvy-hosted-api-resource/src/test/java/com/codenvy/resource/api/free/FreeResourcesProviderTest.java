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
package com.codenvy.resource.api.free;

import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.api.ram.RamResourceType;
import com.codenvy.resource.spi.impl.FreeResourcesLimitImpl;
import com.codenvy.resource.spi.impl.ProvidedResourcesImpl;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link FreeResourcesProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class FreeResourcesProviderTest {
    private static final int RAM_PER_USER         = 2000;
    private static final int RAM_PER_ORGANIZATION = 1000;

    @Mock
    private AccountImpl      account;
    @Mock
    private OrganizationImpl organization;

    @Mock
    private FreeResourcesLimitManager freeResourcesLimitManager;
    @Mock
    private AccountManager            accountManager;
    @Mock
    private OrganizationManager       organizationManager;

    private FreeResourcesProvider provider;

    @BeforeMethod
    public void setUp() throws Exception {
        provider = new FreeResourcesProvider(freeResourcesLimitManager,
                                             accountManager,
                                             organizationManager,
                                             RAM_PER_USER + "mb",
                                             RAM_PER_ORGANIZATION + "mb");
    }

    @Test
    public void shouldProvideDefaultResourcesForPersonalAccount() throws Exception {
        //given
        when(account.getType()).thenReturn(UserImpl.PERSONAL_ACCOUNT);
        when(accountManager.getById(any())).thenReturn(account);
        when(freeResourcesLimitManager.get(any())).thenThrow(new NotFoundException("not found"));

        //when
        List<ProvidedResourcesImpl> result = provider.getResources("user123");

        //then
        assertEquals(result.size(), 1);
        ProvidedResourcesImpl providedResources = result.get(0);
        assertEquals(providedResources, new ProvidedResourcesImpl(FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
                                                                  null,
                                                                  "user123",
                                                                  -1L,
                                                                  -1L,
                                                                  singletonList(new ResourceImpl(RamResourceType.ID, RAM_PER_USER,
                                                                                                 RamResourceType.UNIT))));
        verify(freeResourcesLimitManager).get("user123");
    }

    @Test
    public void shouldRewriteDefaultResourcesWithFreeResourcesLimitIfItExistsForPersonalAccount() throws Exception {
        //given
        when(account.getType()).thenReturn(UserImpl.PERSONAL_ACCOUNT);
        when(accountManager.getById(any())).thenReturn(account);
        when(freeResourcesLimitManager.get(any())).thenReturn(new FreeResourcesLimitImpl("user123",
                                                                                         singletonList(
                                                                                                 new ResourceImpl(RamResourceType.ID, 12345,
                                                                                                                  RamResourceType.UNIT))));

        //when
        List<ProvidedResourcesImpl> result = provider.getResources("user123");

        //then
        assertEquals(result.size(), 1);
        ProvidedResourcesImpl providedResources = result.get(0);
        assertEquals(providedResources, new ProvidedResourcesImpl(FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
                                                                  "user123",
                                                                  "user123",
                                                                  -1L,
                                                                  -1L,
                                                                  singletonList(new ResourceImpl(RamResourceType.ID, 12345,
                                                                                                 RamResourceType.UNIT))));
        verify(freeResourcesLimitManager).get("user123");
    }

    @Test
    public void shouldProvideDefaultResourcesForOrganizationalAccountOfRootOrganization() throws Exception {
        //given
        when(account.getType()).thenReturn(OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
        when(accountManager.getById(any())).thenReturn(account);
        when(freeResourcesLimitManager.get(any())).thenThrow(new NotFoundException("not found"));

        when(organization.getParent()).thenReturn(null);
        when(organizationManager.getById(any())).thenReturn(organization);

        //when
        List<ProvidedResourcesImpl> result = provider.getResources("organization123");

        //then
        assertEquals(result.size(), 1);
        ProvidedResourcesImpl providedResources = result.get(0);
        assertEquals(providedResources, new ProvidedResourcesImpl(FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
                                                                  null,
                                                                  "organization123",
                                                                  -1L,
                                                                  -1L,
                                                                  singletonList(new ResourceImpl(RamResourceType.ID, RAM_PER_ORGANIZATION,
                                                                                                 RamResourceType.UNIT))));
        verify(freeResourcesLimitManager).get("organization123");
        verify(organizationManager).getById("organization123");
    }

    @Test
    public void shouldRewriteDefaultResourcesWithFreeResourcesLimitIfItExistsForOrganizationalAccount() throws Exception {
        //given
        when(account.getType()).thenReturn(OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
        when(accountManager.getById(any())).thenReturn(account);
        when(freeResourcesLimitManager.get(any())).thenReturn(new FreeResourcesLimitImpl("organization123",
                                                                                         singletonList(
                                                                                                 new ResourceImpl(RamResourceType.ID, 12345,
                                                                                                                  RamResourceType.UNIT))));

        when(organization.getParent()).thenReturn(null);
        when(organizationManager.getById(any())).thenReturn(organization);

        //when
        List<ProvidedResourcesImpl> result = provider.getResources("organization123");

        //then
        assertEquals(result.size(), 1);
        ProvidedResourcesImpl providedResources = result.get(0);
        assertEquals(providedResources, new ProvidedResourcesImpl(FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
                                                                  "organization123",
                                                                  "organization123",
                                                                  -1L,
                                                                  -1L,
                                                                  singletonList(new ResourceImpl(RamResourceType.ID, 12345,
                                                                                                 RamResourceType.UNIT))));
        verify(freeResourcesLimitManager).get("organization123");
        verify(organizationManager).getById("organization123");
    }

    @Test
    public void shouldNotProvideDefaultResourcesForOrganizationalAccountOfSuborganization() throws Exception {
        //given
        when(account.getType()).thenReturn(OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
        when(accountManager.getById(any())).thenReturn(account);
        when(freeResourcesLimitManager.get(any())).thenThrow(new NotFoundException("not found"));

        when(organization.getParent()).thenReturn("organization234");
        when(organizationManager.getById(any())).thenReturn(organization);

        //when
        List<ProvidedResourcesImpl> result = provider.getResources("organization123");

        //then
        assertEquals(result.size(), 1);
        ProvidedResourcesImpl providedResources = result.get(0);
        assertEquals(providedResources, new ProvidedResourcesImpl(FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
                                                                  null,
                                                                  "organization123",
                                                                  -1L,
                                                                  -1L,
                                                                  emptyList()));
        verify(freeResourcesLimitManager).get("organization123");
        verify(organizationManager).getById("organization123");
    }

    @Test
    public void shouldNotProvideDefaultResourcesForAccountWithUnlistedType() throws Exception {
        //given
        when(account.getType()).thenReturn("test");
        when(accountManager.getById(any())).thenReturn(account);
//        when(freeResourcesManager.get(any())).thenThrow(new NotFoundException("not found"));
        doThrow(new NotFoundException("not found")).when(freeResourcesLimitManager).get(any());

        //when
        List<ProvidedResourcesImpl> result = provider.getResources("account123");

        //then
        assertEquals(result.size(), 1);
        ProvidedResourcesImpl providedResources = result.get(0);
        assertEquals(providedResources, new ProvidedResourcesImpl(FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
                                                                  null,
                                                                  "account123",
                                                                  -1L,
                                                                  -1L,
                                                                  emptyList()));
        verify(freeResourcesLimitManager).get("account123");
    }

    @Test
    public void shouldProvideResourcesFromFreeResourcesLimitIfItExists() throws Exception {
        //given
        when(account.getType()).thenReturn("test");
        when(accountManager.getById(any())).thenReturn(account);
        when(freeResourcesLimitManager.get(any())).thenReturn(new FreeResourcesLimitImpl("account123",
                                                                                         singletonList(
                                                                                                 new ResourceImpl(RamResourceType.ID, 12345,
                                                                                                                  RamResourceType.UNIT))));

        //when
        List<ProvidedResourcesImpl> result = provider.getResources("account123");

        //then
        assertEquals(result.size(), 1);
        ProvidedResourcesImpl providedResources = result.get(0);
        assertEquals(providedResources, new ProvidedResourcesImpl(FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
                                                                  "account123",
                                                                  "account123",
                                                                  -1L,
                                                                  -1L,
                                                                  singletonList(new ResourceImpl(RamResourceType.ID, 12345,
                                                                                                 RamResourceType.UNIT))));
        verify(freeResourcesLimitManager).get("account123");
    }
}
