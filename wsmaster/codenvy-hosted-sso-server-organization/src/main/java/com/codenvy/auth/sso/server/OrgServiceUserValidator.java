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
package com.codenvy.auth.sso.server;

import com.codenvy.auth.sso.server.organization.UserCreationValidator;
import com.google.common.collect.Sets;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.UserValidator;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Sergii Kabashniuk
 */
public class OrgServiceUserValidator implements UserCreationValidator {

    private final UserManager   userDao;
    private final UserValidator userValidator;
    private final boolean       userSelfCreationAllowed;
    private final Set<String>   reservedNames;

    @Inject
    public OrgServiceUserValidator(UserManager userDao,
                                   UserValidator userValidator,
                                   @Named("che.auth.user_self_creation") boolean userSelfCreationAllowed,
                                   @Named("che.auth.reserved_user_names") String[] reservedNames) {
        this.userDao = userDao;
        this.userValidator = userValidator;
        this.userSelfCreationAllowed = userSelfCreationAllowed;
        this.reservedNames = Sets.newHashSet(reservedNames);
    }

    @Override
    public void ensureUserCreationAllowed(String email, String userName) throws ConflictException, BadRequestException, ServerException {
        if (!userSelfCreationAllowed) {
            throw new ConflictException("Currently only admins can create accounts. Please contact our Admin Team for further info.");
        }

        if (isNullOrEmpty(email)) {
            throw new BadRequestException("Email cannot be empty or null");
        }

        if (isNullOrEmpty(userName)) {
            throw new BadRequestException("User name cannot be empty or null");
        }

        if (!userValidator.isValidName(userName)) {
            throw new BadRequestException("User name must contain letters and digits only");
        }

        if (reservedNames.contains(userName.toLowerCase())) {
            throw new ConflictException(String.format("User name \"%s\" is reserved. Please, choose another one", userName));
        }

        try {
            userDao.getByEmail(email);
            throw new ConflictException("User with given email already exists. Please, choose another one.");
        } catch (NotFoundException e) {
            // ok
        }

        try {
            userDao.getByName(userName);
            throw new ConflictException("User with given name already exists. Please, choose another one.");
        } catch (NotFoundException e) {
            // ok
        }
    }
}
