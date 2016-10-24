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
package com.codenvy.organization.spi.tck;

import com.codenvy.organization.api.event.OrganizationPersistedEvent;
import com.codenvy.organization.spi.OrganizationDao;
import com.codenvy.organization.spi.impl.OrganizationImpl;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link OrganizationDao} contract.
 *
 * @author Sergii Leschenko
 */
@Guice(moduleFactory = TckModuleFactory.class)
@Test(suiteName = OrganizationDaoTest.SUITE_NAME)
public class OrganizationDaoTest {

    public static final String SUITE_NAME = "OrganizationDaoTck";

    private OrganizationImpl[] organizations;

    @Inject
    private OrganizationDao organizationDao;

    @Inject
    private EventService eventService;

    @Inject
    private TckRepository<OrganizationImpl> tckRepository;

    @BeforeMethod
    private void setUp() throws TckRepositoryException {
        organizations = new OrganizationImpl[2];

        organizations[0] = new OrganizationImpl(NameGenerator.generate("organization", 10), "test1", null);
        organizations[1] = new OrganizationImpl(NameGenerator.generate("organization", 10), "test2", null);

        tckRepository.createAll(asList(organizations));
    }

    @AfterMethod
    private void cleanup() throws TckRepositoryException {
        tckRepository.removeAll();
    }

    @Test
    public void shouldCreateOrganization() throws Exception {
        final OrganizationImpl organization = new OrganizationImpl("organization123",
                                                                   "Test",
                                                                   null);

        organizationDao.create(organization);

        assertEquals(organizationDao.getById(organization.getId()), organization);
    }

    @Test
    public void shouldPublishOrganizationPersistedEventAfterStackIsPersisted() throws Exception {
        final boolean[] isNotified = new boolean[] {false};
        eventService.subscribe(event -> isNotified[0] = true, OrganizationPersistedEvent.class);

        organizationDao.create(new OrganizationImpl("organization123",
                                                    "Test",
                                                    null));

        assertTrue(isNotified[0], "Event subscriber notified");
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionOnCreatingOrganizationWithExistingId() throws Exception {
        final OrganizationImpl organization = new OrganizationImpl(organizations[0].getId(),
                                                                   "Test",
                                                                   null);

        organizationDao.create(organization);

        assertEquals(organizationDao.getById(organization.getId()), organization);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionOnCreatingOrganizationWithExistingName() throws Exception {
        final OrganizationImpl organization = new OrganizationImpl("organization123",
                                                                   organizations[0].getName(),
                                                                   null);

        organizationDao.create(organization);

        assertEquals(organizationDao.getById(organization.getId()), organization);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnCreatingNullableOrganization() throws Exception {
        organizationDao.create(null);
    }

    @Test
    public void shouldUpdateOrganization() throws Exception {
        final OrganizationImpl toUpdate = new OrganizationImpl(organizations[0].getId(),
                                                               "new-name",
                                                               null);

        organizationDao.update(toUpdate);

        final OrganizationImpl updated = organizationDao.getById(toUpdate.getId());
        assertEquals(toUpdate, updated);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionOnUpdatingNonExistingOrganization() throws Exception {
        final OrganizationImpl toUpdate = new OrganizationImpl("non-existing-id",
                                                               "new-name",
                                                               "new-parent");

        organizationDao.update(toUpdate);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionOnUpdatingOrganizationNameToExistingOne() throws Exception {
        final OrganizationImpl toUpdate = new OrganizationImpl(organizations[0].getId(),
                                                               organizations[1].getName(),
                                                               "new-parent");

        organizationDao.update(toUpdate);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnUpdatingNullableOrganization() throws Exception {
        organizationDao.update(null);
    }

    @Test(dependsOnMethods = "shouldThrowNotFoundExceptionOnGettingNonExistingOrganizationById")
    public void shouldRemoveOrganization() throws Exception {
        //given
        final OrganizationImpl organization = organizations[0];

        //when
        organizationDao.remove(organization.getId());

        //then
        assertNull(notFoundToNull(() -> organizationDao.getById(organization.getId())));
    }

    @Test
    public void shouldNotThrowAnyExceptionOnRemovingNonExistingOrganization() throws Exception {
        organizationDao.remove("non-existing-org");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenRemovingNull() throws Exception {
        organizationDao.remove(null);
    }

    @Test
    public void shouldGetOrganizationById() throws Exception {
        final OrganizationImpl organization = organizations[0];

        final OrganizationImpl found = organizationDao.getById(organization.getId());

        assertEquals(organization, found);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionOnGettingNonExistingOrganizationById() throws Exception {
        organizationDao.getById("non-existing-org");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGettingOrganizationByNullId() throws Exception {
        organizationDao.getById(null);
    }

    @Test
    public void shouldGetOrganizationByName() throws Exception {
        final OrganizationImpl organization = organizations[0];

        final OrganizationImpl found = organizationDao.getByName(organization.getName());

        assertEquals(organization, found);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionOnGettingNonExistingOrganizationByName() throws Exception {
        organizationDao.getByName("non-existing-org");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGettingOrganizationByNullName() throws Exception {
        organizationDao.getByName(null);
    }

    @Test
    public void shouldGetChildrenOrganizations() throws Exception {
        final OrganizationImpl parent = organizations[0];
        final OrganizationImpl child1 = new OrganizationImpl("child1", "childTest1", parent.getId());
        final OrganizationImpl child2 = new OrganizationImpl("child2", "childTest2", parent.getId());
        final OrganizationImpl child3 = new OrganizationImpl("child3", "childTest3", parent.getId());
        tckRepository.createAll(asList(child1, child2, child3));

        final Page<OrganizationImpl> children = organizationDao.getByParent(parent.getId(), 1, 1);

        assertEquals(children.getTotalItemsCount(), 3);
        assertEquals(children.getItemsCount(), 1);
        assertTrue(children.getItems().contains(child1)
                   ^ children.getItems().contains(child2)
                   ^ children.getItems().contains(child3));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGettingChildrenByNullableParentId() throws Exception {
        organizationDao.getByParent(null, 30, 0);
    }

    private static <T> T notFoundToNull(Callable<T> action) throws Exception {
        try {
            return action.call();
        } catch (NotFoundException x) {
            return null;
        }
    }
}
