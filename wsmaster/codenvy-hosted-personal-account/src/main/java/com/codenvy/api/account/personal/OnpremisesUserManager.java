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
package com.codenvy.api.account.personal;

import com.google.inject.persist.Transactional;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.spi.PreferenceDao;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.api.user.server.spi.UserDao;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Manager that ensures that every user has one and only one personal account.
 * Doesn't contain any logic related to user changing.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class OnpremisesUserManager extends UserManager {
    public static final String PERSONAL_ACCOUNT = "personal";

    private final AccountManager accountManager;

    @Inject
    public OnpremisesUserManager(UserDao userDao,
                                 ProfileDao profileDao,
                                 PreferenceDao preferencesDao,
                                 @Named("che.auth.reserved_user_names") String[] reservedNames,
                                 AccountManager accountManager,
                                 EventService eventService) {
        super(userDao, profileDao, preferencesDao, eventService, reservedNames);
        this.accountManager = accountManager;
    }

    @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
    @Override
    public User create(User newUser, boolean isTemporary) throws ConflictException, ServerException {
        User createdUser = super.create(newUser, isTemporary);

        accountManager.create(new AccountImpl(createdUser.getId(), createdUser.getName(), PERSONAL_ACCOUNT));

        return createdUser;
    }

    @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
    @Override
    public void update(User user) throws NotFoundException, ServerException, ConflictException {
        User originalUser = getById(user.getId());

        if (!originalUser.getName().equals(user.getName())) {
            accountManager.update(new AccountImpl(user.getId(), user.getName(), PERSONAL_ACCOUNT));
        }

        super.update(user);
    }

    @Transactional(rollbackOn = {RuntimeException.class, ApiException.class})
    @Override
    public void remove(String id) throws ServerException, ConflictException {
        accountManager.remove(id);
        super.remove(id);
    }
}
