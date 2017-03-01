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
package com.codenvy.organization.api.permissions;

import com.codenvy.api.permission.server.account.AccountOperation;
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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link OrganizationalAccountPermissionsChecker}
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationalAccountPermissionsCheckerTest {
    private static final String ORG_ID = "org123";

    @Mock
    private Subject subject;

    private OrganizationalAccountPermissionsChecker permissionsChecker;

    @BeforeMethod
    public void setUp() throws Exception {
        when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(true);

        EnvironmentContext.getCurrent().setSubject(subject);

        permissionsChecker = new OrganizationalAccountPermissionsChecker();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        EnvironmentContext.reset();
    }

    @Test
    public void shouldReturnOrganizationalReturnType() throws Exception {
        //then
        assertEquals(permissionsChecker.getAccountType(), OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
    }

    @Test
    public void shouldCheckCreateWorkspacesPermissionOnOrganizationDomainLevel() throws Exception {
        permissionsChecker.checkPermissions(ORG_ID, AccountOperation.CREATE_WORKSPACE);

        verify(subject).hasPermission(OrganizationDomain.DOMAIN_ID, ORG_ID, OrganizationDomain.CREATE_WORKSPACES);
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "User is not authorized to create workspaces in specified namespace.")
    public void shouldThrowForbiddenWhenUserDoesNotHavePermissionToCreateWorkspaces() throws Exception {
        when(subject.hasPermission(OrganizationDomain.DOMAIN_ID, ORG_ID, OrganizationDomain.CREATE_WORKSPACES)).thenReturn(false);

        permissionsChecker.checkPermissions(ORG_ID, AccountOperation.CREATE_WORKSPACE);
    }

    @Test
    public void shouldCheckManageWorkspacesPermissionOnOrganizationDomainLevel() throws Exception {
        permissionsChecker.checkPermissions(ORG_ID, AccountOperation.MANAGE_WORKSPACES);

        verify(subject).hasPermission(OrganizationDomain.DOMAIN_ID, ORG_ID, OrganizationDomain.MANAGE_WORKSPACES);
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "User is not authorized to use specified namespace.")
    public void shouldThrowForbiddenWhenUserDoesNotHavePermissionToManagerWorkspaces() throws Exception {
        when(subject.hasPermission(OrganizationDomain.DOMAIN_ID, ORG_ID, OrganizationDomain.MANAGE_WORKSPACES)).thenReturn(false);

        permissionsChecker.checkPermissions(ORG_ID, AccountOperation.MANAGE_WORKSPACES);
    }

    @Test(dataProvider = "requiredAction")
    public void shouldNotThrowExceptionWhenUserHasAtLeastOnRequiredPermissionOnGettingResourcesInformation(String action) throws Exception {
        when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);
        when(subject.hasPermission(OrganizationDomain.DOMAIN_ID, ORG_ID, action)).thenReturn(true);

        permissionsChecker.checkPermissions(ORG_ID, AccountOperation.SEE_RESOURCE_INFORMATION);

        verify(subject).hasPermission(OrganizationDomain.DOMAIN_ID, ORG_ID, action);
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "User is not authorized to see resources information of requested organization.")
    public void shouldThrowForbiddenWhenUserDoesNotHavePermissionToSeeResourcesInformation() throws Exception {
        when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(false);

        permissionsChecker.checkPermissions(ORG_ID, AccountOperation.SEE_RESOURCE_INFORMATION);
    }

    @DataProvider
    private Object[][] requiredAction() {
        return new Object[][] {
                {OrganizationDomain.CREATE_WORKSPACES},
                {OrganizationDomain.MANAGE_WORKSPACES},
                {OrganizationDomain.MANAGE_RESOURCES}
        };
    }
}
