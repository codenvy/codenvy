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
package com.codenvy.resource.api.provider;

import com.codenvy.organization.api.OrganizationManager;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.resource.api.ram.RamResource;
import com.codenvy.resource.spi.impl.ProvidedResourcesImpl;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link FreeResourcesProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class FreeResourcesProviderTest {
    private static final Integer USER_RAM         = 2000;
    private static final Integer ORGANIZATION_RAM = 3000;
    @Mock
    private AccountManager      accountManager;
    @Mock
    private OrganizationManager organizationManager;
    @Mock
    private AccountImpl         account;

    private FreeResourcesProvider provider;

    @BeforeMethod
    public void setUp() throws Exception {
        provider = new FreeResourcesProvider(accountManager,
                                             organizationManager,
                                             USER_RAM + "mb",
                                             ORGANIZATION_RAM + "mb");

        when(accountManager.getById(any())).thenReturn(account);
    }

    @Test
    public void shouldProvideConfiguredRamForPersonalAccount() throws Exception {
        when(account.getType()).thenReturn(UserImpl.PERSONAL_ACCOUNT);

        final List<ProvidedResourcesImpl> resources = provider.getResources("user123");

        verify(accountManager).getById(eq("user123"));
        assertEquals(resources.size(), 1);
        final ProvidedResourcesImpl providedResource = resources.get(0);
        assertEquals(providedResource.getProviderId(), FreeResourcesProvider.FREE_RESOURCES_PROVIDER);
        assertEquals(providedResource.getStartTime(), new Long(-1));
        assertEquals(providedResource.getEndTime(), new Long(-1));
        assertEquals(providedResource.getOwner(), "user123");
        assertNull(providedResource.getId());
        assertEquals(providedResource.getResources().size(), 1);
        assertEquals(providedResource.getResources().get(0), new RamResource(USER_RAM));
    }

    @Test
    public void shouldProvideConfiguredRamForRootOrganizationalAccount() throws Exception {
        when(organizationManager.getById(anyString())).thenReturn(new OrganizationImpl("organization123",
                                                                                       "testOrg",
                                                                                       null));
        when(account.getType()).thenReturn(OrganizationImpl.ORGANIZATIONAL_ACCOUNT);

        final List<ProvidedResourcesImpl> resources = provider.getResources("organization123");

        verify(accountManager).getById(eq("organization123"));
        assertEquals(resources.size(), 1);
        final ProvidedResourcesImpl providedResource = resources.get(0);
        assertEquals(providedResource.getProviderId(), FreeResourcesProvider.FREE_RESOURCES_PROVIDER);
        assertEquals(providedResource.getStartTime(), new Long(-1));
        assertEquals(providedResource.getEndTime(), new Long(-1));
        assertEquals(providedResource.getOwner(), "organization123");
        assertNull(providedResource.getId());
        assertEquals(providedResource.getResources().size(), 1);
        assertEquals(providedResource.getResources().get(0), new RamResource(ORGANIZATION_RAM));
    }

    @Test
    public void shouldNotProvideConfiguredRamForChildOrganizationalAccount() throws Exception {
        when(organizationManager.getById(anyString())).thenReturn(new OrganizationImpl("organization123",
                                                                                       "testOrg",
                                                                                       "organization321"));
        when(account.getType()).thenReturn(OrganizationImpl.ORGANIZATIONAL_ACCOUNT);

        final List<ProvidedResourcesImpl> resources = provider.getResources("organization123");

        verify(accountManager).getById(eq("organization123"));
        assertTrue(resources.isEmpty());
    }

    @Test
    public void shouldNotProvideFreeResourcesForUnlistedAccount() throws Exception {
        when(account.getType()).thenReturn("test");

        final List<ProvidedResourcesImpl> resources = provider.getResources("account123");

        verify(accountManager).getById(eq("account123"));
        assertTrue(resources.isEmpty());
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Account was not found")
    public void shouldThrowNotFoundExceptionWhenAccountWithGivenIdWasNotFound() throws Exception {
        when(accountManager.getById(any())).thenThrow(new NotFoundException("Account was not found"));

        provider.getResources("account123");
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "connection problem")
    public void shouldRethrowServerExceptionWhenExceptionOccurredOnAccountFetching() throws Exception {
        when(accountManager.getById(any())).thenThrow(new ServerException("connection problem"));

        provider.getResources("account123");
    }
}
