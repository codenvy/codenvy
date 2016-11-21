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
import org.eclipse.che.commons.test.db.H2TestHelper;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
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
        suborganizationsRemover = injector.getInstance(RemoveSuborganizationsBeforeParentOrganizationRemovedEventSubscriber.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        manager.getTransaction().begin();
        for (OrganizationImpl organization : organizations) {
            manager.persist(organization);
            manager.flush();
        }
        manager.getTransaction().commit();
        suborganizationsRemover.subscribe();
    }

    @AfterMethod
    public void cleanup() {
        suborganizationsRemover.unsubscribe();

        manager.getTransaction().begin();
        final Map<String, OrganizationImpl> managedOrganizations = manager.createQuery("SELECT org FROM Organization org",
                                                                                       OrganizationImpl.class)
                                                                          .getResultList().stream()
                                                                          .collect(toMap(OrganizationImpl::getId,
                                                                                         Function.identity()));
        for (int i = organizations.length - 1; i > -1; i--) {
            final OrganizationImpl managedOrganization = managedOrganizations.get(organizations[i].getId());
            if (managedOrganization != null) {
                manager.remove(managedOrganization);
            }
        }
        manager.getTransaction().commit();
    }

    @AfterClass
    public void shutdown() throws Exception {
        manager.getEntityManagerFactory().close();
        H2TestHelper.shutdownDefault();
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
            bind(SchemaInitializer.class).toInstance(new FlywaySchemaInitializer(inMemoryDefault(), "che-schema", "codenvy-schema"));
            bind(DBInitializer.class).asEagerSingleton();
        }
    }
}
