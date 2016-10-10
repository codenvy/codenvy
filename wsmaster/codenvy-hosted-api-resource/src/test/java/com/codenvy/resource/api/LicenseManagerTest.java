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
package com.codenvy.resource.api;

import com.codenvy.resource.spi.impl.LicenseImpl;
import com.codenvy.resource.spi.impl.ProvidedResourcesImpl;
import com.codenvy.resource.spi.impl.ResourceImpl;
import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.NotFoundException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link LicenseManager}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class LicenseManagerTest {
    @Mock
    private ResourcesProvider  resourcesProvider;
    @Mock
    private ResourceAggregator resourceAggregator;

    private LicenseManager licenseManager;

    @BeforeMethod
    public void setUp() {
        licenseManager = new LicenseManager(singleton(resourcesProvider), resourceAggregator);
    }

    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Account with specified id was not found")
    public void shouldThrowNotFoundExceptionWhenAccountWithGivenIdWasNotFound() throws Exception {
        when(resourcesProvider.getResources(eq("account123")))
                .thenThrow(new NotFoundException("Account with specified id was not found"));

        licenseManager.getByAccount("account123");
    }

    @Test
    public void shouldReturnLicenseForGivenAccount() throws Exception {
        final ResourceImpl testResource = new ResourceImpl("RAM", 1000, "mb");
        final ResourceImpl reducedResource = new ResourceImpl("timeout", 2000, "m");
        final ProvidedResourcesImpl providedResource = new ProvidedResourcesImpl("test",
                                                                                 null,
                                                                                 "account123",
                                                                                 123L,
                                                                                 321L,
                                                                                 singletonList(testResource));

        when(resourcesProvider.getResources(eq("account123"))).thenReturn(singletonList(providedResource));
        when(resourceAggregator.aggregateByType(any())).thenReturn(ImmutableMap.of(reducedResource.getType(), reducedResource));

        final LicenseImpl license = licenseManager.getByAccount("account123");

        verify(resourcesProvider).getResources(eq("account123"));
        verify(resourceAggregator).aggregateByType(eq(singletonList(testResource)));

        assertEquals(license.getAccountId(), "account123");
        assertEquals(license.getResourcesDetails().size(), 1);
        assertEquals(license.getResourcesDetails().get(0), providedResource);

        assertEquals(license.getTotalResources().size(), 1);
        assertEquals(license.getTotalResources().get(0), reducedResource);
    }
}
