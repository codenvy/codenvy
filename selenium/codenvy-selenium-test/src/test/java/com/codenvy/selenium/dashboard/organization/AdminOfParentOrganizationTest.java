/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.selenium.dashboard.organization;

import com.codenvy.organization.shared.dto.OrganizationDto;
import com.codenvy.selenium.core.client.OnpremTestOrganizationServiceClient;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage;
import com.codenvy.selenium.pageobject.dashboard.organization.OrganizationPage;
import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import com.codenvy.selenium.pageobject.dashboard.CodenvyAdminDashboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;
import static com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.ACTIONS;
import static com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.AVAILABLE_RAM;
import static com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.MEMBERS;
import static com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.NAME;
import static com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.SUB_ORGANIZATIONS;
import static com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.TOTAL_RAM;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test validates organization views for admin of the organization.
 *
 * @author Ann Shumilova
 */
public class AdminOfParentOrganizationTest {

    private static final Logger LOG = LoggerFactory.getLogger(AdminOfParentOrganizationTest.class);

    private OrganizationDto parentOrganization;
    private OrganizationDto childOrganization;

    @Inject
    private OrganizationListPage                organizationListPage;
    @Inject
    private OrganizationPage                    organizationPage;
    @Inject
    private NavigationBar                       navigationBar;
    @Inject
    private CodenvyAdminDashboard               dashboard;
    @Inject
    private TestIdeUrlProvider                  testIdeUrlProvider;
    @Inject
    private TestDashboardUrlProvider            testDashboardUrlProvider;
    @Inject
    private DefaultTestUser                     testUser;
    @Inject
    private OnpremTestOrganizationServiceClient organizationServiceClient;
    @Inject
    private AdminTestUser                       adminTestUser;

    @BeforeClass
    public void setUp() throws Exception {
        dashboard.open(adminTestUser.getAuthToken());
        dashboard.waitDashboardToolbarTitle();

        parentOrganization =
                organizationServiceClient.createOrganizationAsAdmin(NameGenerator.generate("organization", 5));
        childOrganization =
                organizationServiceClient
                        .createOrganizationAsAdmin(NameGenerator.generate("organization", 5), parentOrganization.getId());

        organizationServiceClient.addOrganizationAdmin(parentOrganization.getId(), testUser.getId());
        organizationServiceClient.addOrganizationMemberAsAdmin(childOrganization.getId(), testUser.getId());

        dashboard.open(testUser.getAuthToken());
    }

    @AfterClass
    public void tearDown() throws Exception {
        organizationServiceClient.deleteOrganizationByIdAsAdmin(childOrganization.getId());
        organizationServiceClient.deleteOrganizationByIdAsAdmin(parentOrganization.getId());
    }

    @Test(priority = 1)
    public void testOrganizationListComponents() {
        navigationBar.waitNavigationBar();
        int organizationsCount = 2;
        navigationBar.clickOnMenu(ORGANIZATIONS);
        organizationListPage.waitForOrganizationsToolbar();
        organizationListPage.waitForOrganizationsList();
        WaitUtils.sleepQuietly(3);

        assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), String.valueOf(organizationsCount));
        assertEquals(organizationListPage.getOrganizationsToolbarTitle(), "Organizations");
        assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), String.valueOf(organizationsCount));
        assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount);
        assertFalse(organizationListPage.isAddOrganizationButtonVisible());
        assertTrue(organizationListPage.isSearchInputVisible());
        //Check all headers are present:
        ArrayList<String> headers = organizationListPage.getOrganizationListHeaders();
        assertTrue(headers.contains(NAME.getTitle()));
        assertTrue(headers.contains(MEMBERS.getTitle()));
        assertTrue(headers.contains(TOTAL_RAM.getTitle()));
        assertTrue(headers.contains(AVAILABLE_RAM.getTitle()));
        assertTrue(headers.contains(SUB_ORGANIZATIONS.getTitle()));
        assertTrue(headers.contains(ACTIONS.getTitle()));
        assertTrue(
                organizationListPage.getValues(NAME).contains(parentOrganization.getQualifiedName()));
        assertTrue(organizationListPage.getValues(NAME).contains(childOrganization.getQualifiedName()));
    }

    @Test(priority = 2)
    public void testParentOrganization() {
        organizationListPage.clickOnOrganization(parentOrganization.getName());

        organizationPage.waitOrganizationName(parentOrganization.getName());
        assertFalse(organizationPage.isOrganizationNameReadonly());
        assertTrue(organizationPage.isWorkspaceCapReadonly());
        assertTrue(organizationPage.isRunningCapReadonly());
        assertTrue(organizationPage.isRAMCapReadonly());
        assertTrue(organizationPage.isWorkspaceCapReadonly());
        assertTrue(organizationPage.isDeleteButtonVisible());

        organizationPage.clickMembersTab();
        organizationPage.waitMembersList();
        assertTrue(organizationPage.isAddMemberButtonVisible());

        organizationPage.clickSubOrganizationsTab();
        organizationListPage.waitForOrganizationsList();
        assertFalse(organizationListPage.isAddOrganizationButtonVisible());
        assertTrue(organizationListPage.isAddSubOrganizationButtonVisible());
        assertTrue(organizationListPage.getValues(NAME).contains(childOrganization.getQualifiedName()));

        organizationPage.clickBackButton();
        organizationListPage.waitForOrganizationsList();
        assertEquals(organizationListPage.getOrganizationListItemCount(), 2);
    }

    @Test(priority = 3)
    public void testChildOrganization() {
        navigationBar.clickOnMenu(ORGANIZATIONS);
        organizationListPage.waitForOrganizationsToolbar();
        organizationListPage.waitForOrganizationsList();
        WaitUtils.sleepQuietly(3);
        organizationListPage.clickOnOrganization(childOrganization.getQualifiedName());
        organizationPage.waitOrganizationName(childOrganization.getQualifiedName());
        assertTrue(organizationPage.isOrganizationNameReadonly());
        assertFalse(organizationPage.isWorkspaceCapReadonly());
        assertFalse(organizationPage.isRunningCapReadonly());
        assertFalse(organizationPage.isRAMCapReadonly());
        assertFalse(organizationPage.isWorkspaceCapReadonly());
        assertFalse(organizationPage.isDeleteButtonVisible());

        organizationPage.clickMembersTab();
        organizationPage.waitMembersList();
        assertFalse(organizationPage.isAddMemberButtonVisible());

        organizationPage.clickSubOrganizationsTab();
        organizationListPage.waitForSubOrganizationsEmptyList();
        assertFalse(organizationListPage.isAddOrganizationButtonVisible());
        assertFalse(organizationListPage.isAddSubOrganizationButtonVisible());

        organizationPage.clickBackButton();
        organizationPage.waitOrganizationName(parentOrganization.getName());
    }

}
