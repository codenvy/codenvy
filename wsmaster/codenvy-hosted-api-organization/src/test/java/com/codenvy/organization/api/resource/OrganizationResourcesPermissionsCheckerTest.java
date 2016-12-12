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

import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.organization.spi.impl.OrganizationImpl;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link OrganizationResourcesPermissionsChecker}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationResourcesPermissionsCheckerTest {
    @Mock
    private Subject subject;

    private OrganizationResourcesPermissionsChecker permissionsChecker;

    @BeforeMethod
    public void setUp() throws Exception {
        permissionsChecker = new OrganizationResourcesPermissionsChecker();
        EnvironmentContext.getCurrent().setSubject(subject);
    }

    @AfterMethod
    public void cleanUp() throws Exception {
        EnvironmentContext.getCurrent().setSubject(null);
    }

    @Test
    public void shouldNotThrowExceptionWhenUserHasPermissionToCreateWorkspaces() throws Exception {
        when(subject.hasPermission(OrganizationDomain.DOMAIN_ID, "organization123", OrganizationDomain.CREATE_WORKSPACES))
                .thenReturn(true);

        //when
        permissionsChecker.checkResourcesVisibility("organization123");

        //then
        verify(subject).hasPermission(OrganizationDomain.DOMAIN_ID, "organization123", OrganizationDomain.CREATE_WORKSPACES);
    }

    @Test
    public void shouldNotThrowExceptionWhenUserHasPermissionToManageWorkspaces() throws Exception {
        when(subject.hasPermission(OrganizationDomain.DOMAIN_ID, "organization123", OrganizationDomain.MANAGE_WORKSPACES))
                .thenReturn(true);

        //when
        permissionsChecker.checkResourcesVisibility("organization123");

        //then
        verify(subject).hasPermission(OrganizationDomain.DOMAIN_ID, "organization123", OrganizationDomain.MANAGE_WORKSPACES);
    }

    @Test(dataProvider = "requiredPermissions")
    public void shouldNotThrowExceptionWhenUserHasPermissionToManageResources(String presentAction) throws Exception {
        when(subject.hasPermission(OrganizationDomain.DOMAIN_ID, "organization123", presentAction))
                .thenReturn(true);

        //when
        permissionsChecker.checkResourcesVisibility("organization123");

        //then
        verify(subject).hasPermission(OrganizationDomain.DOMAIN_ID, "organization123", presentAction);
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "User is not authorized to see resources information of requested organization.")
    public void shouldThrowExceptionWhenUserHasNoAnyRequiredPermission() throws Exception {
        //when
        permissionsChecker.checkResourcesVisibility("organization123");
    }

    @Test
    public void shouldReturnOrganizationalAccountTypeOnGettingAccountType() throws Exception {
        //when
        final String accountType = permissionsChecker.getAccountType();

        //then
        assertEquals(accountType, OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
    }

    @DataProvider(name = "requiredPermissions")
    public Object[][] requiredPermissionsProvider() {
        return new Object[][] {
                {OrganizationDomain.MANAGE_RESOURCES},
                {OrganizationDomain.CREATE_WORKSPACES},
                {OrganizationDomain.MANAGE_WORKSPACES},
        };
    }

}
