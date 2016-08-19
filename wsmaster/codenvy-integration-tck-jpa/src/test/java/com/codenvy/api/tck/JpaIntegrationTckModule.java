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
package com.codenvy.api.tck;

import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.codenvy.api.workspace.server.spi.WorkerDao;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.persist.Transactional;
import com.google.inject.persist.jpa.JpaPersistModule;

import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.account.spi.jpa.JpaAccountDao;
import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.jdbc.jpa.guice.JpaInitializer;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.factory.server.jpa.JpaFactoryDao;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.factory.server.spi.FactoryDao;
import org.eclipse.che.api.machine.server.jpa.JpaRecipeDao;
import org.eclipse.che.api.machine.server.jpa.JpaSnapshotDao;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.ssh.server.jpa.JpaSshDao;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;
import org.eclipse.che.api.user.server.jpa.JpaPreferenceDao;
import org.eclipse.che.api.user.server.jpa.JpaProfileDao;
import org.eclipse.che.api.user.server.jpa.JpaUserDao;
import org.eclipse.che.api.user.server.jpa.PreferenceEntity;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.api.workspace.server.jpa.JpaStackDao;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.security.PasswordEncryptor;
import org.eclipse.che.security.SHA512PasswordEncryptor;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.TRANSACTION_TYPE;

/**
 * Module for testing JPA DAO.
 *
 * @author Mihail Kuznyetsov
 */
public class JpaIntegrationTckModule extends TckModule {

    @Override
    protected void configure() {
        Map properties = new HashMap();
        properties.put(TRANSACTION_TYPE,
                       PersistenceUnitTransactionType.RESOURCE_LOCAL.name());

        properties.put(JDBC_DRIVER, System.getProperty("jdbc.driver"));
        properties.put(JDBC_URL, System.getProperty("jdbc.url"));
        properties.put(JDBC_USER, System.getProperty("jdbc.user"));
        properties.put(JDBC_PASSWORD, System.getProperty("jdbc.password"));

        JpaPersistModule main = new JpaPersistModule("main");
        main.properties(properties);
        install(main);

        bind(JpaInitializer.class).asEagerSingleton();
        bind(EntityListenerInjectionManagerInitializer.class).asEagerSingleton();

        //repositories
        //api-account
        bind(new TypeLiteral<TckRepository<AccountImpl>>() {}).toInstance(new JpaTckRepository<>(AccountImpl.class));
        //api-factory
        bind(new TypeLiteral<TckRepository<FactoryImpl>>() {}).to(FactoryJpaTckRepository.class);
        //api-user
        bind(new TypeLiteral<TckRepository<UserImpl>>() {}).to(UserJpaTckRepository.class);
        bind(new TypeLiteral<TckRepository<ProfileImpl>>() {}).toInstance(new JpaTckRepository<>(ProfileImpl.class));
        bind(new TypeLiteral<TckRepository<Pair<String, Map<String, String>>>>() {}).to(PreferenceJpaTckRepository.class);
        //api-workspace
        bind(new TypeLiteral<TckRepository<WorkspaceImpl>>() {}).toInstance(new JpaTckRepository<>(WorkspaceImpl.class));
        bind(new TypeLiteral<TckRepository<StackImpl>>() {}).toInstance(new JpaTckRepository<>(StackImpl.class));
        bind(new TypeLiteral<TckRepository<WorkerImpl>>() {}).toInstance(new JpaTckRepository<>(WorkerImpl.class));

        //api-machine
        bind(new TypeLiteral<TckRepository<RecipeImpl>>() {}).toInstance(new JpaTckRepository<>(RecipeImpl.class));
        bind(new TypeLiteral<TckRepository<SnapshotImpl>>() {}).to(SnapshotJpaTckRepository.class);
        //api ssh
        bind(new TypeLiteral<TckRepository<SshPairImpl>>() {}).toInstance(new JpaTckRepository<>(SshPairImpl.class));

        //dao
        //api-account
        bind(AccountDao.class).to(JpaAccountDao.class);
        //api-factory
        bind(FactoryDao.class).to(JpaFactoryDao.class);
        //api-user
        bind(UserDao.class).to(JpaUserDao.class);
        bind(ProfileDao.class).to(JpaProfileDao.class);
        bind(PreferenceDao.class).to(JpaPreferenceDao.class);
        //api-workspace
        bind(WorkspaceDao.class).to(JpaWorkspaceDao.class);
        bind(StackDao.class).to(JpaStackDao.class);
        bind(WorkerDao.class).to(JpaWorkerDao.class);
        //api-machine
        bind(RecipeDao.class).to(JpaRecipeDao.class);
        bind(SnapshotDao.class).to(JpaSnapshotDao.class);
        //api-ssh
        bind(SshDao.class).to(JpaSshDao.class);

        // SHA-512 ecnryptor is faster than PBKDF2 so it is better for testing
        bind(PasswordEncryptor.class).to(SHA512PasswordEncryptor.class).in(Singleton.class);
        bind(org.eclipse.che.api.core.postgresql.jdbc.jpa.eclipselink.PostgreSqlExceptionHandler.class);
    }


    @Transactional
    public static class PreferenceJpaTckRepository implements TckRepository<Pair<String, Map<String, String>>> {

        @Inject
        private Provider<EntityManager> managerProvider;

        @Override
        public void createAll(Collection<? extends Pair<String, Map<String, String>>> entities) throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            for (Pair<String, Map<String, String>> pair : entities) {
                manager.persist(new PreferenceEntity(pair.first, pair.second));
            }
        }

        @Override
        public void removeAll() throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            manager.createQuery("SELECT prefs FROM Preference prefs", PreferenceEntity.class)
                   .getResultList()
                   .forEach(manager::remove);
        }
    }


    @Transactional
    public static class UserJpaTckRepository implements TckRepository<UserImpl> {

        @Inject
        private Provider<EntityManager> managerProvider;

        @Inject
        private PasswordEncryptor encryptor;

        @Override
        public void createAll(Collection<? extends UserImpl> entities) throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            entities.stream()
                    .map(user -> new UserImpl(user.getId(),
                                              user.getEmail(),
                                              user.getName(),
                                              user.getPassword() != null ? encryptor.encrypt(user.getPassword()) : null,
                                              user.getAliases()))
                    .forEach(manager::persist);
        }

        @Override
        public void removeAll() throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            manager.createQuery("SELECT users FROM Usr users", UserImpl.class)
                   .getResultList()
                   .forEach(manager::remove);
        }
    }

    @Transactional
    public static class SnapshotJpaTckRepository implements TckRepository<SnapshotImpl> {

        @Inject
        private Provider<EntityManager> managerProvider;

        @Override
        public void createAll(Collection<? extends SnapshotImpl> snapshots) throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            WorkspaceConfig config = new WorkspaceConfigImpl("name",
                                    "description",
                                    "defaultEnv",
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    Collections.emptyList());
            final AccountImpl[] accounts = new AccountImpl[] {new AccountImpl("id1", "test0", "test"),
                                                              new AccountImpl("id1", "test1", "test"),
                                                              new AccountImpl("id1", "test2", "test")};
            manager.persist(accounts[0]);
            manager.persist(accounts[1]);
            manager.persist(accounts[2]);
            manager.persist(new WorkspaceImpl("workspace-0", accounts[0], config));
            manager.persist(new WorkspaceImpl("workspace-1", accounts[1], config));
            manager.persist(new WorkspaceImpl("workspace-id", accounts[2], config));
            for (SnapshotImpl snapshot : snapshots) {
                manager.persist(snapshot);
            }
        }

        @Override
        public void removeAll() throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            manager.createQuery("SELECT snapshots FROM Snapshot snapshots", SnapshotImpl.class)
                   .getResultList()
                   .forEach(manager::remove);
            manager.createQuery("SELECT workspaces FROM Workspace workspaces", WorkspaceImpl.class)
                   .getResultList()
                   .forEach(manager::remove);
        }
    }


    @Transactional
    public static class FactoryJpaTckRepository implements TckRepository<FactoryImpl> {

        @Inject
        private Provider<EntityManager> managerProvider;

        @Override
        public void createAll(Collection<? extends FactoryImpl> factories) throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            for (FactoryImpl factory : factories) {
                final String id = factory.getCreator().getUserId();
                manager.persist(new UserImpl(id, "email_" + id, "name_" + id));
                manager.persist(factory);
            }
        }

        @Override
        public void removeAll() throws TckRepositoryException {
            final EntityManager manager = managerProvider.get();
            manager.createQuery("SELECT factory FROM Factory factory", FactoryImpl.class)
                   .getResultList()
                   .forEach(manager::remove);
            manager.createQuery("SELECT users FROM Usr users", UserImpl.class)
                   .getResultList()
                   .forEach(manager::remove);
        }
    }
}
