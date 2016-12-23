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
import com.codenvy.organization.spi.impl.OrganizationDistributedResourcesImpl;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.organization.spi.jpa.JpaOrganizationDistributedResourcesDao.RemoveOrganizationDistributedResourcesSubscriber;
import com.codenvy.resource.spi.impl.ResourceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.util.concurrent.Callable;

import static java.util.Collections.singletonList;
import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;
import static org.testng.Assert.assertNull;

/**
 * Tests for {@link RemoveOrganizationDistributedResourcesSubscriber}
 *
 * @author Sergii Leschenko
 */
public class RemoveOrganizationDistributedResourcesSubscriberTest {
    private EntityManager manager;

    private JpaOrganizationDao                     jpaOrganizationDao;
    private JpaOrganizationDistributedResourcesDao distributedResourcesDao;

    private RemoveOrganizationDistributedResourcesSubscriber suborganizationsRemover;

    private OrganizationImpl                     organization;
    private OrganizationDistributedResourcesImpl distributedResources;


    @BeforeClass
    public void setupEntities() throws Exception {
        organization = new OrganizationImpl("org1", "parentOrg", null);
        distributedResources = new OrganizationDistributedResourcesImpl(organization.getId(),
                                                                        singletonList(new ResourceImpl("test",
                                                                                                       1020,
                                                                                                       "unit")));

        Injector injector = com.google.inject.Guice.createInjector(new OrganizationJpaModule(), new TestModule());

        manager = injector.getInstance(EntityManager.class);
        jpaOrganizationDao = injector.getInstance(JpaOrganizationDao.class);
        distributedResourcesDao = injector.getInstance(JpaOrganizationDistributedResourcesDao.class);
        suborganizationsRemover =
                injector.getInstance(RemoveOrganizationDistributedResourcesSubscriber.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        manager.getTransaction().begin();
        manager.persist(organization);
        manager.persist(distributedResources);
        manager.getTransaction().commit();
        suborganizationsRemover.subscribe();
    }

    @AfterMethod
    public void cleanup() {
        suborganizationsRemover.unsubscribe();

        manager.getTransaction().begin();

        final OrganizationImpl managedOrganization = manager.find(OrganizationImpl.class, this.organization.getId());
        if (managedOrganization != null) {
            manager.remove(managedOrganization);
        }

        OrganizationDistributedResourcesImpl managedDistributedResources = manager.find(OrganizationDistributedResourcesImpl.class,
                                                                                        this.organization.getId());
        if (managedDistributedResources != null) {
            manager.remove(managedDistributedResources);
        }

        manager.getTransaction().commit();
    }

    @AfterClass
    public void shutdown() throws Exception {
        manager.getEntityManagerFactory().close();
    }

    @Test
    public void shouldRemoveDistributedOrganizationResourcesWhenOrganizationIsRemoved() throws Exception {
        jpaOrganizationDao.remove(organization.getId());

        assertNull(notFoundToNull(() -> distributedResourcesDao.get(organization.getId())));
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
