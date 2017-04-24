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
package com.codenvy.api.dao.authentication;

import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.user.server.spi.ProfileDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Performs validation of user country by blacklist and blocks login if matched.
 *
 * @author Max Shaposhnik
 */
public class PassportValidator {

    private static final Logger LOG = LoggerFactory.getLogger(PassportValidator.class);

    private ProfileDao   profileDao;
    private List<String> blockedList;

    @Inject
    public PassportValidator(ProfileDao profileDao, @Nullable @Named("auth.blocked_country_names") String[] blockedList) {
        this.profileDao = profileDao;
        if (blockedList == null ||
            blockedList.length == 0 ) {
            this.blockedList = new ArrayList<>();
        } else {
            this.blockedList = Arrays.asList(blockedList);
        }
    }

    public void validate(String userId) throws AuthenticationException {
        try {
            final Profile profile = profileDao.getById(userId);
            final String country = profile.getAttributes().get("country");
            if (country != null && blockedList.stream().anyMatch(country::equalsIgnoreCase)) {
                throw new AuthenticationException("Authentication failed. Please contact support.");
            }
        } catch (ServerException | NotFoundException e) {
            LOG.warn("Unable to validate user's passport.", e);
        }
    }
}
