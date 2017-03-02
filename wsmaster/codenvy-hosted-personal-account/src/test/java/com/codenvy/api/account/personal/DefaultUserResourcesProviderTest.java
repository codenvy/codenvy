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
package com.codenvy.api.account.personal;

import com.codenvy.resource.api.type.RamResourceType;
import com.codenvy.resource.api.type.RuntimeResourceType;
import com.codenvy.resource.api.type.TimeoutResourceType;
import com.codenvy.resource.api.type.WorkspaceResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link DefaultUserResourcesProvider}
 *
 * @author Sergii Leschenko
 */
public class DefaultUserResourcesProviderTest {
    private DefaultUserResourcesProvider resourcesProvider;

    @BeforeMethod
    public void setUp() throws Exception {
        resourcesProvider = new DefaultUserResourcesProvider(20 * 60 * 1000, "2gb", 10, 5);
    }

    @Test
    public void shouldReturnPersonalAccountType() throws Exception {
        //when
        final String accountType = resourcesProvider.getAccountType();

        //then
        assertEquals(accountType, OnpremisesUserManager.PERSONAL_ACCOUNT);
    }

    @Test
    public void shouldProvideDefaultRamResourceForUser() throws Exception {
        //when
        final List<ResourceImpl> defaultResources = resourcesProvider.getResources("user123");

        //then
        assertEquals(defaultResources.size(), 4);
        assertTrue(defaultResources.contains(new ResourceImpl(RamResourceType.ID,
                                                              2048,
                                                              RamResourceType.UNIT)));
        assertTrue(defaultResources.contains(new ResourceImpl(WorkspaceResourceType.ID,
                                                              10,
                                                              WorkspaceResourceType.UNIT)));
        assertTrue(defaultResources.contains(new ResourceImpl(RuntimeResourceType.ID,
                                                              5,
                                                              RuntimeResourceType.UNIT)));
        assertTrue(defaultResources.contains(new ResourceImpl(TimeoutResourceType.ID,
                                                              20,
                                                              TimeoutResourceType.UNIT)));
    }
}
