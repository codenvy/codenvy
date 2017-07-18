/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
