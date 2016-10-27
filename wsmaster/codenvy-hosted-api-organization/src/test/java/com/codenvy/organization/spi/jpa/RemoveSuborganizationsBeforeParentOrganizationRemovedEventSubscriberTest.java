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
package com.codenvy.organization.spi.jpa;

import com.codenvy.organization.api.OrganizationJpaModule;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.organization.spi.jpa.JpaOrganizationDao.RemoveSuborganizationsBeforeParentOrganizationRemovedEventSubscriber;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.util.concurrent.Callable;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Tests for {@link RemoveSuborganizationsBeforeParentOrganizationRemovedEventSubscriber}
 *
 * @author Sergii Leschenko
 */
public class RemoveSuborganizationsBeforeParentOrganizationRemovedEventSubscriberTest {
    private EntityManager manager;

    private JpaOrganizationDao jpaOrganizationDao;

    private RemoveSuborganizationsBeforeParentOrganizationRemovedEventSubscriber suborganizationsRemover;

    private OrganizationImpl[] organizations;

    @BeforeClass
    public void setupEntities() throws Exception {
        organizations = new OrganizationImpl[] {new OrganizationImpl("org1", "parentOrg", null),
                                                new OrganizationImpl("org2", "childOrg1", "org1"),
                                                new OrganizationImpl("org3", "childOrg2", "org1")};

        Injector injector = Guice.createInjector(new OrganizationJpaModule(), new TestModule());

        manager = injector.getInstance(EntityManager.class);
        jpaOrganizationDao = injector.getInstance(JpaOrganizationDao.class);
        suborganizationsRemover =
                injector.getInstance(RemoveSuborganizationsBeforeParentOrganizationRemovedEventSubscriber.class);
        suborganizationsRemover.subscribe();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        manager.getTransaction().begin();
        for (OrganizationImpl organization : organizations) {
            manager.persist(organization);
            manager.flush();
        }
        manager.getTransaction().commit();
    }

    @AfterMethod
    public void cleanup() {
        manager.getTransaction().begin();
        manager.createQuery("SELECT org FROM Organization org", OrganizationImpl.class)
               .getResultList()
               .forEach(manager::remove);
        manager.getTransaction().commit();
    }

    @AfterClass
    public void shutdown() throws Exception {
        suborganizationsRemover.unsubscribe();
        manager.getEntityManagerFactory().close();
    }

    @Test
    public void shouldRemoveAllSuborganizationsWhenParentOrganizationIsRemoved() throws Exception {
        jpaOrganizationDao.remove(organizations[0].getId());

        assertNull(notFoundToNull(() -> jpaOrganizationDao.getById(organizations[0].getId())));
        assertNull(notFoundToNull(() -> jpaOrganizationDao.getById(organizations[1].getId())));
        assertNull(notFoundToNull(() -> jpaOrganizationDao.getById(organizations[2].getId())));
    }

    @Test
    public void shouldRemoveAllSuborganizationsWhenPageSizeEqualsToOne() throws Exception {
        suborganizationsRemover.removeSuborganizations(organizations[0].getId(), 1);

        assertNotNull(notFoundToNull(() -> jpaOrganizationDao.getById(organizations[0].getId())));
        assertNull(notFoundToNull(() -> jpaOrganizationDao.getById(organizations[1].getId())));
        assertNull(notFoundToNull(() -> jpaOrganizationDao.getById(organizations[2].getId())));
    }

    private static <T> T notFoundToNull(Callable<T> action) throws Exception {
        try {
            return action.call();
        } catch (NotFoundException x) {
            return null;
        }
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new JpaPersistModule("main"));
            bind(JpaInitializer.class).asEagerSingleton();
            bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();
        }
    }
}
