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


import com.codenvy.auth.sso.server.organization.UserCreator;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.Constants;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.user.server.ProfileManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Sergii Kabashniuk
 */
public class OrgServiceUserCreator implements UserCreator {
    private static final Logger LOG = LoggerFactory.getLogger(OrgServiceUserCreator.class);

    private final UserManager       userManager;
    private final ProfileManager    profileManager;
    private final PreferenceManager preferenceManager;
    private final boolean           userSelfCreationAllowed;

    @Inject
    public OrgServiceUserCreator(UserManager userManager,
                                 ProfileManager profileManager,
                                 PreferenceManager preferenceManager,
                                 @Named("che.auth.user_self_creation") boolean userSelfCreationAllowed) {
        this.userManager = userManager;
        this.profileManager = profileManager;
        this.preferenceManager = preferenceManager;
        this.userSelfCreationAllowed = userSelfCreationAllowed;
    }

    @Override
    public User createUser(String email, String userName, String firstName, String lastName) throws IOException {
        //TODO check this method should only call if user is not exists.
        try {
            return userManager.getByEmail(email);
        } catch (NotFoundException e) {
            if (!userSelfCreationAllowed) {
                throw new IOException("Currently only admins can create accounts. Please contact our Admin Team for further info.");
            }

            final Map<String, String> attributes = new HashMap<>();
            if (firstName != null) {
                attributes.put("firstName", firstName);
            }
            if (lastName != null) {
                attributes.put("lastName", lastName);
            }
            attributes.put("email", email);


            try {
                User user = createNonReservedUser(userName, email);
                while (user == null) {
                    user = createNonReservedUser(NameGenerator.generate(userName, 4), email);
                }

                ProfileImpl profile = new ProfileImpl(profileManager.getById(user.getId()));
                profile.getAttributes().putAll(attributes);
                profileManager.update(profile);

                final Map<String, String> preferences = new HashMap<>();
                preferences.put("codenvy:created", Long.toString(System.currentTimeMillis()));
                preferences.put("resetPassword", "true");
                preferenceManager.save(user.getId(), preferences);

                return user;
            } catch (NotFoundException | ServerException ex) {
                throw new IOException(ex.getLocalizedMessage(), ex);
            }
        } catch (ServerException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }

    }

    @Override
    public User createTemporary() throws IOException {

        String id = NameGenerator.generate(User.class.getSimpleName(), Constants.ID_LENGTH);
        try {
            String testName;
            while (true) {
                testName = NameGenerator.generate("AnonymousUser_", 6);
                try {
                    userManager.getByName(testName);
                } catch (NotFoundException e) {
                    break;
                } catch (ApiException e) {
                    throw new IOException(e.getLocalizedMessage(), e);
                }
            }


            final String anonymousUser = testName;
            // generate password and delete all "-" symbols which are generated by randomUUID()
            final String password = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

            final UserImpl user = new UserImpl(id, anonymousUser, anonymousUser + "@noreply.com");
            user.setPassword(password);
            userManager.create(user, true);

            profileManager.create(new ProfileImpl(id));

            final Map<String, String> preferences = new HashMap<>();
            preferences.put("temporary", String.valueOf(true));
            preferences.put("codenvy:created", Long.toString(System.currentTimeMillis()));
            preferenceManager.save(id, preferences);

            LOG.info("Temporary user {} created", anonymousUser);
            return user;
        } catch (ApiException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }


    /**
     * Create user via user manager, ensuring the name is not reserved and conflicting.
     *
     * @param username
     *        user name
     * @param email
     *        user email
     * @return created user if succesfully created, null otherwise
     * @throws ServerException
     */
    private User createNonReservedUser(String username, String email) throws ServerException {
        try {
            userManager.create(new UserImpl(null, email, username), false);
            return userManager.getByName(username);
        } catch (ServerException | NotFoundException e) {
            throw new ServerException(e);
        } catch (ConflictException e) {
            return null;
        }
    }
}
