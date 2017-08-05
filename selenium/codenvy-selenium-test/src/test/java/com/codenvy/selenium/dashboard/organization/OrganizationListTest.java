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
import com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage;
import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test validates the main UI elements on the organization list page.
 *
 * @author Ann Shumilova
 */
public class OrganizationListTest {
    private List<OrganizationDto> organizations;

    @Inject
    private OrganizationListPage                organizationListPage;
    @Inject
    private NavigationBar                       navigationBar;
    @Inject
    private Dashboard                           dashboard;
    @Inject
    private OnpremTestOrganizationServiceClient organizationServiceClient;
    @Inject
    private AdminTestUser                       adminTestUser;

    private OrganizationDto organization;

    @BeforeClass
    public void setUp() throws Exception {
        organization =
                organizationServiceClient.createOrganizationAsAdmin(NameGenerator.generate("organization", 5));
        organizations = organizationServiceClient.getOrganizationsAsAdmin();
        dashboard.open(adminTestUser.getAuthToken());
    }

    @AfterClass
    public void tearDown() throws Exception {
        organizationServiceClient.deleteOrganizationByIdAsAdmin(organization.getId());
    }

    @Test
    public void testOrganizationListComponents() {
        navigationBar.waitNavigationBar();
        int organizationsCount = organizations.size();
        navigationBar.clickOnMenu(NavigationBar.MenuItem.ORGANIZATIONS);
        organizationListPage.waitForOrganizationsToolbar();
        organizationListPage.waitForOrganizationsList();

        assertEquals(navigationBar.getMenuCounterValue(NavigationBar.MenuItem.ORGANIZATIONS), String.valueOf(organizationsCount));
        assertEquals(organizationListPage.getOrganizationsToolbarTitle(), "Organizations");
        assertEquals(navigationBar.getMenuCounterValue(NavigationBar.MenuItem.ORGANIZATIONS), String.valueOf(organizationsCount));
        assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount);
        assertTrue(organizationListPage.isAddOrganizationButtonVisible());
        assertTrue(organizationListPage.isSearchInputVisible());
        //Check all headers are present:
        ArrayList<String> headers = organizationListPage.getOrganizationListHeaders();
        assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.NAME.getTitle()));
        assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.MEMBERS.getTitle()));
        assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.TOTAL_RAM.getTitle()));
        assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.AVAILABLE_RAM.getTitle()));
        assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.SUB_ORGANIZATIONS.getTitle()));
        assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.ACTIONS.getTitle()));
    }
}
