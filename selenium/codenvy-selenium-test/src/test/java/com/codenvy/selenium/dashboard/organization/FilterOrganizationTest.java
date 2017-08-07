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
import com.codenvy.selenium.pageobject.dashboard.organization.AddOrganization;
import com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage;
import com.codenvy.selenium.pageobject.dashboard.organization.OrganizationPage;
import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test validates organization creation and actions on it in the list of organizations.
 *
 * @author Ann Shumilova
 */
public class FilterOrganizationTest {
    private static final Logger LOG = LoggerFactory.getLogger(FilterOrganizationTest.class);

    private List<OrganizationDto> organizations;
    private String                organizationName;

    @Inject
    private OrganizationListPage                organizationListPage;
    @Inject
    private OrganizationPage                    organizationPage;
    @Inject
    private NavigationBar                       navigationBar;
    @Inject
    private Dashboard                           dashboard;
    @Inject
    private AddOrganization                     addOrganization;
    @Inject
    private OnpremTestOrganizationServiceClient organizationServiceClient;
    @Inject
    private AdminTestUser                       adminTestUser;

    @BeforeClass
    public void setUp() throws Exception {
        dashboard.open(adminTestUser.getAuthToken());

        organizationName = NameGenerator.generate("organization", 5);
        organizations = organizationServiceClient.getOrganizationsAsAdmin();
    }

    @AfterClass
    public void tearDown() throws Exception {
        organizationServiceClient.deleteOrganizationByNameAsAdmin(organizationName);
    }

    @Test
    public void testOrganizationListFiler() {
        navigationBar.waitNavigationBar();
        int organizationsCount = organizations.size();

        navigationBar.clickOnMenu(NavigationBar.MenuItem.ORGANIZATIONS);
        organizationListPage.waitForOrganizationsToolbar();
        organizationListPage.clickAddOrganizationButton();

        addOrganization.waitAddOrganization();
        addOrganization.setOrganizationName(organizationName);
        addOrganization.checkAddOrganizationButtonEnabled();
        addOrganization.clickCreateOrganizationButton();
        organizationPage.waitOrganizationTitle(organizationName);

        assertEquals(navigationBar.getMenuCounterValue(NavigationBar.MenuItem.ORGANIZATIONS), String.valueOf(organizationsCount + 1));
        navigationBar.clickOnMenu(NavigationBar.MenuItem.ORGANIZATIONS);
        organizationListPage.waitForOrganizationsToolbar();
        organizationListPage.waitForOrganizationsList();
        assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount + 1);
        assertTrue(organizationListPage.getValues(OrganizationListPage.OrganizationListHeader.NAME).contains(organizationName));

        // Tests search:
        organizationListPage.typeInSearchInput(organizationName);
        assertEquals(organizationListPage.getOrganizationListItemCount(), 1);
        organizationListPage.typeInSearchInput(organizationName + "test");
        organizationListPage.waitForOrganizationsList();
        assertEquals(organizationListPage.getOrganizationListItemCount(), 0);
        organizationListPage.clearSearchInput();
        assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount + 1);
    }
}
