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
package com.codenvy.organization.api;

import com.codenvy.organization.api.event.OrganizationCreatedEvent;
import com.codenvy.organization.shared.model.Organization;
import com.codenvy.organization.spi.MemberDao;
import com.codenvy.organization.spi.OrganizationDao;
import com.codenvy.organization.spi.impl.OrganizationImpl;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for {@link OrganizationManager}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationManagerTest {
    @Captor
    ArgumentCaptor<OrganizationImpl>         organizationCaptor;
    @Captor
    ArgumentCaptor<OrganizationCreatedEvent> eventArgumentCaptor;

    @Mock
    OrganizationDao organizationDao;
    @Mock
    MemberDao       memberDao;
    @Mock
    EventService    eventService;

    OrganizationManager manager;

    @BeforeMethod
    public void setUp() throws Exception {
        manager = new OrganizationManager(organizationDao,
                                          memberDao,
                                          eventService,
                                          new String[] {"reserved"});
    }

    @Test
    public void shouldCreateOrganization() throws Exception {
        final Organization toCreate = createOrganization();

        manager.create(toCreate);

        verify(organizationDao).create(organizationCaptor.capture());
        final OrganizationImpl createdOrganization = organizationCaptor.getValue();
        assertEquals(createdOrganization.getName(), toCreate.getName());
        assertEquals(createdOrganization.getParent(), toCreate.getParent());

        verify(eventService).publish(eventArgumentCaptor.capture());
        final OrganizationCreatedEvent event = eventArgumentCaptor.getValue();
        assertEquals(event.getOrganization().getName(), toCreate.getName());
        assertEquals(event.getOrganization().getParent(), toCreate.getParent());
    }

    @Test
    public void shouldGenerateIdentifierWhenCreatingOrganization() throws Exception {
        final Organization organization = createOrganization();

        manager.create(organization);

        verify(organizationDao).create(organizationCaptor.capture());
        final String id = organizationCaptor.getValue().getId();
        assertNotNull(id);
        assertNotEquals(id, "identifier");
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionOnCreationIfOrganizationNameIsReserved() throws Exception {
        final OrganizationImpl organization = new OrganizationImpl("identifier", "reserved", "parentId");

        manager.create(organization);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenCreatingNullableOrganization() throws Exception {
        manager.create(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenUpdatingOrganizationWithNullEntity() throws Exception {
        manager.update("organizationId", null);
    }

    @Test
    public void shouldUpdateOrganizationAndIgnoreIdAndParentFields() throws Exception {
        final OrganizationImpl existing = createOrganization();
        when(organizationDao.getById("organizationId")).thenReturn(existing);
        final OrganizationImpl update = new OrganizationImpl(existing);
        update.setId("newId");
        update.setParent("newParent");

        final OrganizationImpl updated = manager.update("organizationId", update);

        verify(organizationDao).update(eq(updated));
        assertEquals(updated.getName(), update.getName());
        assertEquals(updated.getParent(), existing.getParent());
        assertEquals(updated.getId(), existing.getId());
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionOnUpdatingIfOrganizationNameIsReserved() throws Exception {
        manager.update("id", new OrganizationImpl("id", "reserved", "parentId"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenUpdatingOrganizationByNullId() throws Exception {
        manager.update(null, new OrganizationImpl());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenRemovingOrganizationByNullId() throws Exception {
        manager.remove(null);
    }

    @Test
    public void shouldRemoveOrganization() throws Exception {
        manager.remove("org123");

        verify(organizationDao).remove(eq("org123"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingOrganizationByNullName() throws Exception {
        manager.getById(null);
    }

    @Test
    public void shouldGetOrganizationByName() throws Exception {
        final OrganizationImpl toFetch = createOrganization();
        when(organizationDao.getByName(eq("org123"))).thenReturn(toFetch);

        final OrganizationImpl fetched = manager.getByName("org123");

        assertEquals(fetched, toFetch);
        verify(organizationDao).getByName("org123");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingOrganizationByNullId() throws Exception {
        manager.getById(null);
    }

    @Test
    public void shouldGetOrganizationById() throws Exception {
        final OrganizationImpl toFetch = createOrganization();
        when(organizationDao.getById(eq("org123"))).thenReturn(toFetch);

        final OrganizationImpl fetched = manager.getById("org123");

        assertEquals(fetched, toFetch);
        verify(organizationDao).getById("org123");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingOrganizationsByNullParent() throws Exception {
        manager.getByParent(null);
    }

    @Test
    public void shouldGetOrganizationsByParent() throws Exception {
        final OrganizationImpl toFetch = createOrganization();
        when(organizationDao.getByParent(eq("org123"))).thenReturn(Collections.singletonList(toFetch));

        final List<OrganizationImpl> organizations = manager.getByParent("org123");

        assertEquals(organizations.size(), 1);
        assertEquals(organizations.get(0), toFetch);
        verify(organizationDao).getByParent("org123");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingOrganizationsByNullUserId() throws Exception {
        manager.getByMember(null);
    }

    @Test
    public void shouldGetOrganizationsByMember() throws Exception {
        final OrganizationImpl toFetch = createOrganization();
        when(memberDao.getOrganizations(eq("org123"))).thenReturn(Collections.singletonList(toFetch));

        final List<OrganizationImpl> organizations = manager.getByMember("org123");

        assertEquals(organizations.size(), 1);
        assertEquals(organizations.get(0), toFetch);
        verify(memberDao).getOrganizations("org123");
    }

    private OrganizationImpl createOrganization() {
        return new OrganizationImpl("identifier", "orgName", "parentId");
    }
}
