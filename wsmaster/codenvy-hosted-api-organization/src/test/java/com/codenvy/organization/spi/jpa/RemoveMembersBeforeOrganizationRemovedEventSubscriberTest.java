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
package com.codenvy.organization.spi.jpa;

import com.codenvy.organization.api.OrganizationJpaModule;
import com.codenvy.organization.spi.impl.MemberImpl;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
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
import java.util.stream.Stream;

import static com.codenvy.organization.spi.jpa.JpaMemberDao.RemoveMembersBeforeOrganizationRemovedEventSubscriber;
import static java.util.Arrays.asList;
import static org.eclipse.che.commons.test.db.H2TestHelper.inMemoryDefault;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link RemoveMembersBeforeOrganizationRemovedEventSubscriber}
 *
 * @author Sergii Leschenko
 */
public class RemoveMembersBeforeOrganizationRemovedEventSubscriberTest {
    private EntityManager      manager;
    private JpaOrganizationDao organizationDao;
    private JpaMemberDao       memberDao;

    private RemoveMembersBeforeOrganizationRemovedEventSubscriber subscriber;

    private OrganizationImpl organization;
    private UserImpl[]       users;
    private MemberImpl[]     members;

    @BeforeClass
    public void setupEntities() throws Exception {
        organization = new OrganizationImpl("org123", "test-org", null);

        users = new UserImpl[3];
        for (int i = 0; i < 3; i++) {
            users[i] = new UserImpl("user" + i, "user" + i + "@test.com", "username" + i);
        }

        members = new MemberImpl[3];
        for (int i = 0; i < 3; i++) {
            members[i] = new MemberImpl(users[i].getId(), organization.getId(), asList("read", "update"));
        }

        Injector injector = Guice.createInjector(new OrganizationJpaModule(), new TestModule());

        manager = injector.getInstance(EntityManager.class);
        organizationDao = injector.getInstance(JpaOrganizationDao.class);
        memberDao = injector.getInstance(JpaMemberDao.class);

        subscriber = injector.getInstance(RemoveMembersBeforeOrganizationRemovedEventSubscriber.class);
        subscriber.subscribe();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        manager.getTransaction().begin();
        manager.persist(organization);
        Stream.of(users).forEach(manager::persist);
        Stream.of(members).forEach(manager::persist);
        manager.getTransaction().commit();
    }

    @AfterMethod
    public void cleanup() {
        manager.getTransaction().begin();
        manager.createQuery("SELECT org FROM Organization org", OrganizationImpl.class)
               .getResultList()
               .forEach(manager::remove);
        manager.createQuery("SELECT usr FROM Usr usr", UserImpl.class)
               .getResultList()
               .forEach(manager::remove);
        manager.createQuery("SELECT m FROM Member m", MemberImpl.class)
               .getResultList()
               .forEach(manager::remove);
        manager.getTransaction().commit();
    }

    @AfterClass
    public void shutdown() throws Exception {
        subscriber.unsubscribe();
        manager.getEntityManagerFactory().close();
        H2TestHelper.shutdownDefault();
    }

    @Test
    public void shouldRemoveAllMembersWhenOrganizationIsRemoved() throws Exception {
        organizationDao.remove(organization.getId());

        assertEquals(memberDao.getMembers(organization.getId(), 1, 0).getTotalItemsCount(), 0);
    }

    @Test
    public void shouldRemoveAllMembersWhenPageSizeEqualsToOne() throws Exception {
        subscriber.removeMembers(organization.getId(), 1);

        assertEquals(memberDao.getMembers(organization.getId(), 1, 0).getTotalItemsCount(), 0);
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
