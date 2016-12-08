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

import com.codenvy.resource.api.ram.RamResourceType;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests for {@link DefaultUserResourcesProvider}
 *
 * @author Sergii Leschenko
 */
public class DefaultUserResourcesProviderTest {
    private DefaultUserResourcesProvider resourcesProvider;

    @BeforeMethod
    public void setUp() throws Exception {
        resourcesProvider = new DefaultUserResourcesProvider("2gb");
    }

    @Test
    public void shouldReturnPersonalAccountType() throws Exception {
        //when
        final String accountType = resourcesProvider.getAccountType();

        //then
        Assert.assertEquals(accountType, UserImpl.PERSONAL_ACCOUNT);
    }

    @Test
    public void shouldProvideDefaultRamResourceForUser() throws Exception {
        //when
        final List<ResourceImpl> defaultResources = resourcesProvider.getResources("user123");

        //then
        Assert.assertEquals(defaultResources.size(), 1);
        Assert.assertEquals(defaultResources.get(0), new ResourceImpl(RamResourceType.ID,
                                                                      2048,
                                                                      RamResourceType.UNIT));
    }
}
