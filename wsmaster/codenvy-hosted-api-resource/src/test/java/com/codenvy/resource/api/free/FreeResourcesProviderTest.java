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

import com.codenvy.resource.model.ProvidedResources;
import com.codenvy.resource.spi.impl.FreeResourcesLimitImpl;
import com.codenvy.resource.spi.impl.ProvidedResourcesImpl;
import com.codenvy.resource.spi.impl.ResourceImpl;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
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
    private static final String TEST_ACCOUNT_TYPE  = "test";
    private static final String TEST_RESOURCE_TYPE = "testResource";
    private static final String TEST_RESOURCE_UNIT = "testResourceUnit";

    @Mock
    private AccountImpl               account;
    @Mock
    private FreeResourcesLimitManager freeResourcesLimitManager;
    @Mock
    private AccountManager            accountManager;
    @Mock
    private DefaultResourcesProvider  defaultResourcesProvider;

    private FreeResourcesProvider provider;

    @BeforeMethod
    public void setUp() throws Exception {
        when(account.getType()).thenReturn(TEST_ACCOUNT_TYPE);

        when(defaultResourcesProvider.getAccountType()).thenReturn(TEST_ACCOUNT_TYPE);
        when(defaultResourcesProvider.getResources(any())).thenReturn(singletonList(new ResourceImpl(TEST_RESOURCE_TYPE,
                                                                                                     1020,
                                                                                                     TEST_RESOURCE_UNIT)));

        provider = new FreeResourcesProvider(freeResourcesLimitManager,
                                             accountManager,
                                             ImmutableSet.of(defaultResourcesProvider));
    }

    @Test
    public void shouldProvideDefaultResourcesIfThereAreProviderForThisAccountType() throws Exception {
        //given
        when(accountManager.getById(any())).thenReturn(account);
        when(freeResourcesLimitManager.get(any())).thenThrow(new NotFoundException("not found"));

        //when
        List<ProvidedResources> result = provider.getResources("user123");

        //then
        assertEquals(result.size(), 1);
        ProvidedResources providedResources = result.get(0);
        assertEquals(providedResources, new ProvidedResourcesImpl(FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
                                                                  null,
                                                                  "user123",
                                                                  -1L,
                                                                  -1L,
                                                                  singletonList(new ResourceImpl(TEST_RESOURCE_TYPE,
                                                                                                 1020,
                                                                                                 TEST_RESOURCE_UNIT))));
        verify(freeResourcesLimitManager).get("user123");
    }

    @Test
    public void shouldRewriteDefaultResourcesWithFreeResourcesLimitIfItExists() throws Exception {
        //given
        when(accountManager.getById(any())).thenReturn(account);
        when(freeResourcesLimitManager.get(any())).thenReturn(new FreeResourcesLimitImpl("user123",
                                                                                         singletonList(
                                                                                                 new ResourceImpl(TEST_RESOURCE_TYPE,
                                                                                                                  12345,
                                                                                                                  TEST_RESOURCE_UNIT))));

        //when
        List<ProvidedResources> result = provider.getResources("user123");

        //then
        assertEquals(result.size(), 1);
        ProvidedResources providedResources = result.get(0);
        assertEquals(providedResources, new ProvidedResourcesImpl(FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
                                                                  "user123",
                                                                  "user123",
                                                                  -1L,
                                                                  -1L,
                                                                  singletonList(new ResourceImpl(TEST_RESOURCE_TYPE,
                                                                                                 12345,
                                                                                                 TEST_RESOURCE_UNIT))));
        verify(freeResourcesLimitManager).get("user123");
    }

    @Test
    public void shouldNotProvideDefaultResourcesForAccountThatDoesNotHaveDefaultResourcesProvider() throws Exception {
        //given
        when(account.getType()).thenReturn("anotherTestType");
        when(accountManager.getById(any())).thenReturn(account);
        when(freeResourcesLimitManager.get(any())).thenThrow(new NotFoundException("not found"));
        doThrow(new NotFoundException("not found")).when(freeResourcesLimitManager).get(any());

        //when
        List<ProvidedResources> result = provider.getResources("account123");

        //then
        assertEquals(result.size(), 1);
        ProvidedResources providedResources = result.get(0);
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
        when(account.getType()).thenReturn("anotherTestType");
        when(accountManager.getById(any())).thenReturn(account);
        when(freeResourcesLimitManager.get(any())).thenReturn(new FreeResourcesLimitImpl("account123",
                                                                                         singletonList(new ResourceImpl(TEST_RESOURCE_TYPE,
                                                                                                                        12345,
                                                                                                                        TEST_RESOURCE_UNIT))));

        //when
        List<ProvidedResources> result = provider.getResources("account123");

        //then
        assertEquals(result.size(), 1);
        ProvidedResources providedResources = result.get(0);
        assertEquals(providedResources, new ProvidedResourcesImpl(FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
                                                                  "account123",
                                                                  "account123",
                                                                  -1L,
                                                                  -1L,
                                                                  singletonList(new ResourceImpl(TEST_RESOURCE_TYPE,
                                                                                                 12345,
                                                                                                 TEST_RESOURCE_UNIT))));
        verify(freeResourcesLimitManager).get("account123");
    }
}
