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
import com.codenvy.organization.spi.impl.OrganizationImpl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link OrganizationResourceLockKeyProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationResourceLockKeyProviderTest {
    @Mock
    private OrganizationManager organizationManager;

    @InjectMocks
    private OrganizationResourceLockKeyProvider lockProvider;

    @Test
    public void shouldReturnRootOrganizationId() throws Exception {
        //given
        createOrganization("root", null);
        createOrganization("suborg", "root");
        createOrganization("subsuborg", "suborg");

        //when
        final String lockId = lockProvider.getLockKey("subsuborg");

        //then
        assertEquals(lockId, "root");
    }

    @Test
    public void shouldReturnOrganizationalReturnType() throws Exception {
        //then
        assertEquals(lockProvider.getAccountType(), OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
    }

    private void createOrganization(String id, String parentId) throws Exception {
        when(organizationManager.getById(id)).thenReturn(new OrganizationImpl(id, id + "Name", parentId));
    }
}
