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
package com.codenvy.service.password;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Michail Kuznyetsov
 */
@Singleton
public class RecoveryStorage {
    private final Cache<String, String> storage;

    @Inject
    public RecoveryStorage(@Named("password.recovery.expiration_timeout_hours") long validationMaxAge) {
        if (validationMaxAge <= 0) {
            throw new IllegalArgumentException("Expiration timeout must not be less or equal 0");
        }
        this.storage = CacheBuilder.<String, String>newBuilder()
                                   .expireAfterAccess(validationMaxAge, TimeUnit.HOURS)
                                   .maximumSize(10000)
                                   .build();
    }

    /**
     * Verify that uuid has corresponding user email in the storage
     *
     * @param uuid
     *         unique identifier that points to user email in storage
     * @return true if there is user email, false otherwise
     */
    public boolean isValid(String uuid) {
        return storage.getIfPresent(uuid) != null;
    }

    /**
     * Put user email to storage, and return generated uuid for it.
     *
     * @param userEmail
     *         user email that needs to be stored
     * @return uuid related to this email
     */
    public String generateRecoverToken(String userEmail) {
        String uuid = UUID.randomUUID().toString();

        storage.put(uuid, userEmail);

        return uuid;
    }

    /**
     * Remove user email from storage by its uuid.
     *
     * @param uuid
     *         unique identifier of user email
     */
    public void remove(String uuid) {
        storage.invalidate(uuid);
    }

    /**
     * Get user email from storage by uuid.
     *
     * @param uuid
     *         unique identifier of user email
     * @return string with user name
     */
    public String get(String uuid) {
        return storage.getIfPresent(uuid);
    }
}
