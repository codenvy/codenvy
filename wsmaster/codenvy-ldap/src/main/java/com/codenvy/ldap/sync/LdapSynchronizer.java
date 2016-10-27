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
package com.codenvy.ldap.sync;

import com.codenvy.ldap.LdapUserIdNormalizer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jdbc.jpa.eclipselink.EntityListenerInjectionManagerInitializer;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * Periodically synchronizes ldap users with a provided database.
 * The data in the database becomes eventually consistent, as it will
 * be synchronized after some period of time.
 *
 * <p>If synchronization period is configured to be <= 0, then
 * synchronization scheduler won't be started and synchronization
 * can be performed either by {@link #syncAllAsynchronously()} or {@link #syncAll()}.
 *
 * <p>Please note that there is no way to execute parallel synchronizations
 * neither by scheduler nor by {@link #syncAllAsynchronously()} call,
 * unless {@link #syncAll()} is called directly.
 *
 * <p>Ldap entries selection strategy is picked by {@link LdapEntrySelectorProvider}.
 *
 * <p>It is thread-safe.
 *
 * @author Yevhenii Voevodin
 * @author Max Shaposhnik
 */
@Singleton
public class LdapSynchronizer {

    private static final Logger LOG                                   = LoggerFactory.getLogger(LdapSynchronizer.class);
    private static final int    EACH_ENTRIES_COUNT_CHECK_INTERRUPTION = 200;

    private static final String USER_ID_ATTRIBUTE_NAME    = "ldap.sync.user.attr.id";
    private static final String USER_NAME_ATTRIBUTE_NAME  = "ldap.sync.user.attr.name";
    private static final String USER_EMAIL_ATTRIBUTE_NAME = "ldap.sync.user.attr.email";

    private final long                             syncPeriodMs;
    private final long                             initDelayMs;
    private final boolean                          updateIfExists;
    private final boolean                          removeIfMissing;
    private final Function<LdapEntry, ProfileImpl> profileMapper;
    private final Function<LdapEntry, UserImpl>    userMapper;
    private final LdapEntrySelector                selector;
    private final ConnectionFactory                connFactory;
    private final UserDao                          userDao;
    private final ProfileDao                       profileDao;
    private final ScheduledExecutorService         scheduler;
    private final AtomicBoolean                    isSyncing;
    private final LdapUserIdNormalizer             idNormalizer;
    private final DBUserFinder                     userFinder;

    /**
     * Creates an instance of synchronizer.
     *
     * @param connFactory
     *         the factory used for getting connections, the strategy is
     *         one connection per one synchronization
     * @param selector
     *         selector used to query users for synchronization
     * @param userDao
     *         data access object for storing users
     * @param profileDao
     *         data access object for storing profiles
     * @param syncPeriodMs
     *         period of synchronization in milliseconds, if it is <=0 then
     *         synchronization won't be periodical and can be performed only
     *         by direct methods calls.
     * @param initDelayMs
     *         initial delay of synchronization, it is used only if {@code syncPeriodMs}
     *         is specified to be > 0
     * @param userIdAttr
     *         ldap attribute indicating user identifier, it must be unique, otherwise
     *         synchronization will fail on user which has the same identifier.
     *         e.g. 'uid'
     * @param userNameAttr
     *         ldap attribute indicating user name, it must be unique, otherwise
     *         synchronization will fail on user which has the same name.
     *         e.g. 'cn'
     * @param userEmailAttr
     *         ldap attribute indicating user email, it must be unique, otherwise
     *         synchronization will fail on user which has the same email
     *         e.g. 'mail'
     * @param profileAttributes
     *         an optional list of pairs indicating application to ldap attributes mapping.
     *         e.g. <i>lastName=sn,firstName=givenName,phone=telephoneNumber</i>
     * @param updateIfExists
     *         whether update those users who already present in persistence layer
     * @param removeIfMissing
     *         whether remove those users who are present in persistence layer while missing
     *         from ldap storage
     * @param userFinder
     *         gets database users and their attributes
     */
    @Inject
    public LdapSynchronizer(ConnectionFactory connFactory,
                            LdapEntrySelector selector,
                            UserDao userDao,
                            ProfileDao profileDao,
                            LdapUserIdNormalizer idNormalizer,
                            EntityListenerInjectionManagerInitializer jpaInitializer,
                            @Named("ldap.sync.period_ms") long syncPeriodMs,
                            @Named("ldap.sync.initial_delay_ms") long initDelayMs,
                            @Named(USER_ID_ATTRIBUTE_NAME) String userIdAttr,
                            @Named(USER_NAME_ATTRIBUTE_NAME) String userNameAttr,
                            @Named(USER_EMAIL_ATTRIBUTE_NAME) String userEmailAttr,
                            @Named("ldap.sync.profile.attrs") @Nullable Pair<String, String>[] profileAttributes,
                            @Named("ldap.sync.update_if_exists") boolean updateIfExists,
                            @Named("ldap.sync.remove_if_missing") boolean removeIfMissing,
                            DBUserFinder userFinder) {
        if (initDelayMs < 0) {
            throw new IllegalArgumentException("'ldap.sync.initial_delay_ms' must be >= 0, the actual value is " + initDelayMs);
        }
        this.connFactory = connFactory;
        this.userDao = userDao;
        this.profileDao = profileDao;
        this.syncPeriodMs = syncPeriodMs;
        this.initDelayMs = initDelayMs;
        this.selector = selector;
        this.idNormalizer = idNormalizer;
        this.userMapper = new UserMapper(userIdAttr, userNameAttr, userEmailAttr);
        this.profileMapper = new ProfileMapper(userIdAttr, profileAttributes);
        this.isSyncing = new AtomicBoolean(false);
        this.updateIfExists = updateIfExists;
        this.removeIfMissing = removeIfMissing;
        this.userFinder = userFinder;
        this.scheduler = Executors.newScheduledThreadPool(1,
                                                          new ThreadFactoryBuilder().setNameFormat("LdapSynchronizer-%d")
                                                                                    .setDaemon(false)
                                                                                    .build());
    }

    /**
     * Performs asynchronous synchronization only if is it is
     * not executing right now.
     *
     * @throws SyncException
     *         if the synchronization is currently executing
     */
    public void syncAllAsynchronously() {
        if (!isSyncing.compareAndSet(false, true)) {
            throw new SyncException("Couldn't start synchronization as it is executing right now");
        }
        scheduler.execute(this::syncSilentlyAndUnsetFlag);
    }

    /**
     * Does the synchronization.
     *
     * @return the result of synchronization
     * @throws LdapException
     *         when any error occurs during connection opening or closing
     * @throws SyncException
     *         when any error occurs during synchronization
     */
    public SyncResult syncAll() throws LdapException, SyncException {
        LOG.info("Preparing synchronization environment");
        final SyncResult syncResult = new SyncResult();
        final Set<String> linkingIds = userFinder.findLinkingIds();
        LOG.debug("Using selector {} for synchronization", selector);
        LOG.info("Starting synchronization of users/profiles");
        try (Connection connection = connFactory.getConnection()) {
            connection.open();
            long iteration = 0;
            for (LdapEntry entry : selector.select(connection)) {
                iteration++;

                syncFetched(entry, linkingIds, syncResult);

                // Each EACH_ENTRIES_COUNT_CHECK_INTERRUPTION synchronized entries check whether thread wasn't interrupted
                // if it was - stop the synchronization, all the users who were not synchronized
                // will be synchronized with the next synchronization
                if (iteration % EACH_ENTRIES_COUNT_CHECK_INTERRUPTION == 0) {
                    if (Thread.currentThread().isInterrupted()) {
                        LOG.warn("User/Profile synchronization was interrupted");
                        LOG.info("Synchronization result: {}", syncResult);
                        return syncResult;
                    }
                }
            }
        }

        if (removeIfMissing && !linkingIds.isEmpty()) {
            LOG.info("Removing users missing from ldap storage, users to remove '{}'", linkingIds.size());
            for (String linkingId : linkingIds) {
                try {
                    final User user = userFinder.findOne(linkingId);
                    userDao.remove(user.getId());
                    syncResult.removed++;
                    LOG.debug("Removed user '{}'", user.getId());
                } catch (NotFoundException | ServerException | ConflictException x) {
                    LOG.info(format("Couldn't remove user '%s' due to occurred error", linkingId), x);
                    syncResult.failed++;
                }
            }
        }

        LOG.info("Synchronization result: {}", syncResult);
        return syncResult;
    }

    /** Validates and tries to persist fetched ldap entry. */
    private void syncFetched(LdapEntry entry, Set<String> linkingIds, SyncResult syncResult) {
        idNormalizer.normalize(entry);

        final UserImpl ldapUser = userMapper.apply(entry);
        if (!isValid(ldapUser)) {
            syncResult.failed++;
            return;
        }

        final ProfileImpl ldapProfile = profileMapper.apply(entry);
        try {
            final String linkingId = userFinder.extractLinkingId(ldapUser);
            if (!linkingIds.remove(linkingId)) {
                createUserAndProfile(ldapUser, ldapProfile);
                syncResult.created++;
                LOG.debug("Created user & profile '{}'", ldapUser.getId());
                return;
            }

            if (!updateIfExists) {
                syncResult.skipped++;
                LOG.debug("User & profile '{}' are skipped", ldapUser.getId());
                return;
            }

            final User dbUser = userFinder.findOne(linkingId);
            final Profile dbProfile = profileDao.getById(dbUser.getId());
            if (updateUserAndProfile(dbUser, dbProfile, ldapUser, ldapProfile)) {
                syncResult.updated++;
                LOG.debug("Updated user & profile '{}'", ldapUser.getId());
            } else {
                syncResult.upToDate++;
                LOG.debug("User & profile '{}' are up-to-date", ldapUser.getId());
            }
        } catch (RuntimeException | ConflictException | NotFoundException | ServerException x) {
            LOG.info("Couldn't synchronize(update/create) user or his profile '{}' " +
                     "due to occurred error, original ldap entry '{}'. Error: {}",
                     ldapUser.getId(),
                     entry,
                     x.getMessage());
            syncResult.failed++;
        }
    }

    @Transactional
    protected void createUserAndProfile(UserImpl user, ProfileImpl profile) throws ConflictException, ServerException {
        userDao.create(user);
        profileDao.create(profile);
    }

    @Transactional
    protected boolean updateUserAndProfile(User dbUser,
                                           Profile dbProfile,
                                           UserImpl ldapUser,
                                           ProfileImpl ldapProfile) throws ServerException,
                                                                           NotFoundException,
                                                                           ConflictException {
        boolean updated = false;
        if (!dbUser.equals(ldapUser)) {
            userDao.update(ldapUser);
            updated = true;
        }
        if (!dbProfile.equals(ldapProfile)) {
            profileDao.update(ldapProfile);
            updated = true;
        }
        return updated;
    }

    private void syncSilentlyAndUnsetFlag() {
        try {
            syncAll();
        } catch (Exception x) {
            LOG.error("Couldn't finish users synchronization due to occurred error, " +
                      "the database may be partly synchronized", x);
        } finally {
            isSyncing.set(false);
        }
    }

    @PostConstruct
    public void startScheduler() {
        final SchedulerSyncRunnable task = new SchedulerSyncRunnable();
        if (syncPeriodMs > 0) {
            scheduler.scheduleAtFixedRate(task,
                                          initDelayMs,
                                          syncPeriodMs,
                                          TimeUnit.MILLISECONDS);
            LOG.info("Users/Profiles synchronizer registered, synchronization period {}ms, initial delay is {}ms",
                     syncPeriodMs,
                     initDelayMs);
        } else {
            scheduler.schedule(task, initDelayMs, TimeUnit.MILLISECONDS);
            LOG.info("Synchronizing Users/Profiles in {}ms", initDelayMs);
        }
    }

    @PreDestroy
    public void shutdownScheduler() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                scheduler.shutdownNow();
                if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                    LOG.warn("Couldn't terminate LdapSynchronizer scheduler");
                }
            }
        } catch (InterruptedException x) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private boolean isValid(UserImpl user) {
        if (user.getId() == null) {
            LOG.warn(format("Cannot find out user's id. Please, check configuration `%s` parameter correctness.",
                            USER_ID_ATTRIBUTE_NAME));
            return false;
        }
        if (user.getName() == null) {
            LOG.warn(format("Cannot find out user's name. Please, check configuration `%s` parameter correctness.",
                            USER_NAME_ATTRIBUTE_NAME));
            return false;
        }
        if (user.getEmail() == null) {
            LOG.warn(format("Cannot find out user's email. Please, check configuration `%s` parameter correctness.",
                            USER_NAME_ATTRIBUTE_NAME));
            return false;
        }
        return true;
    }

    /** Describes synchronization result. */
    public static class SyncResult {

        private long created;
        private long updated;
        private long removed;
        private long failed;
        private long upToDate;
        private long skipped;

        /** How many users where removed. */
        public long getRemoved() {
            return removed;
        }

        /** How many users where updated. */
        public long getUpdated() {
            return updated;
        }

        /** How many users where created. */
        public long getCreated() {
            return created;
        }

        /** How many users couldn't be created or updated. */
        public long getFailed() {
            return failed;
        }

        /** How many users were not updated because the database version of them is the same. */
        public long getUpToDate() {
            return upToDate;
        }

        /**
         * For how many users an attempt to create/update wasn't performed due to
         * synchronization configuration options.
         */
        public long getSkipped() {
            return skipped;
        }

        /**
         * How many synchronization attempts were performed
         * or how many ldap users were processed.
         */
        public long getProcessed() {
            return created + updated + upToDate + failed + skipped;
        }

        @Override
        public String toString() {
            return format("processed = '%d', " +
                          "created = '%d', " +
                          "updated = '%d', " +
                          "removed = '%d', " +
                          "failed = '%d', " +
                          "up-to-date = '%d', " +
                          "skipped = '%d'",
                          getProcessed(),
                          created,
                          updated,
                          removed,
                          failed,
                          upToDate,
                          skipped);
        }
    }

    private class SchedulerSyncRunnable implements Runnable {
        @Override
        public void run() {
            if (isSyncing.compareAndSet(false, true)) {
                syncSilentlyAndUnsetFlag();
            }
        }
    }
}
