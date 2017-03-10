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
package com.codenvy.auth.sso.server.handler;

import com.codenvy.api.dao.authentication.TokenGenerator;

import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.valueOf;

/**
 * Class provide ability to login user with one-time tokens.
 * Used to authenticate user from source code.
 * Workflow is following: some part of our code recognize user and want to authenticate it.
 * It generate one bearer token and give user for one time authentication.
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class BearerTokenAuthenticationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BearerTokenAuthenticationHandler.class);
    private final ConcurrentMap<String, Map<String, String>> tokenMap;
    private final Timer                                      timer;
    /** Period of time when bearer ticked keep valid */
    @Named("auth.sso.bearer_ticket_lifetime_seconds")
    @Inject
    private       int                                        ticketLifeTimeSeconds;
    @Inject
    private       TokenGenerator                             tokenGenerator;
    @Inject
    private       UserManager                                userManager;


    public BearerTokenAuthenticationHandler() {
        this.tokenMap = new ConcurrentHashMap<>();
        // Remove all invalid token once per hour;
        timer = new Timer("bearer-token-timer", true);
    }

    @PostConstruct
    public void startTimer() {
        //wait 1 second and run each minute.
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (String token : tokenMap.keySet()) {
                    if (!isValid(token)) {
                        LOG.debug("Ticket {} invalidated ", token);
                        tokenMap.remove(token);
                    }
                }
            }
        }, 5000, 60 * 1000);
    }

    @PreDestroy
    public void cancelTimer() {
        timer.cancel();
    }

    /**
     * Authenticate user by token.
     *
     * @throws AuthenticationException
     */
    public void authenticate(final String userSecret) throws AuthenticationException {
        if (isValid(userSecret)) {
            tokenMap.remove(userSecret);
        } else {
            throw new AuthenticationException(403,
                                              "Authentication of user failed. Token " + userSecret +
                                              " not found or expired.");
        }
    }

    public String getType() {
        return "bearer";
    }

    /**
     * Generate new token for given user.
     *
     * @param email
     *         - email of the user.
     * @return - token for one time authentication.
     */
    public String generateBearerToken(String email, String username, Map<String, String> payload) {
        String token = tokenGenerator.generate();
        Map<String, String> payloadCopy = payload == null ? new HashMap() : new HashMap(payload);
        payloadCopy.put("email", email);
        payloadCopy.put("username", username);
        payloadCopy.put("creation.time", Long.toString(System.currentTimeMillis()));
        tokenMap.put(token, payloadCopy);
        return token;
    }

    /**
     * Generate new token for given user.
     *
     * @param email
     *         email of the user.
     * @return token for one time authentication.
     */
    public String generateBearerToken(String email, Map<String, String> payload) throws ServerException {
        return generateBearerToken(email, findAvailableUsername(email), payload);
    }

    /**
     * Get token payload.
     *
     * @param bearerToken
     *         - bearer token that associated with payload.
     * @return - map with payload
     */
    public Map<String, String> getPayload(String bearerToken) {
        Map<String, String> payload = tokenMap.get(bearerToken);

        return payload == null ? Collections.<String, String>emptyMap() : new HashMap<>(payload);

    }

    /**
     * Add some payload to existed token
     *
     * @param bearerToken
     *         - bearer token that associated with payload.
     * @return - map with payload
     */
    public void addPayload(String bearerToken, Map<String, String> addPayload) {
        Map<String, String> payload = tokenMap.get(bearerToken);
        if (payload != null) {
            payload.putAll(addPayload);
        } else {
            throw new IllegalStateException("Token " + bearerToken + " is not found ");
        }
    }

    /**
     * Check is bearer token still valid.
     *
     * @param token
     *         - bearer token
     * @return - true if it is valid, false otherwise
     */
    public boolean isValid(String token) {
        Map<String, String> payload = tokenMap.get(token);
        if (payload != null) {
            // verify token's age
            long creationTime = Long.valueOf(payload.get("creation.time"));
            long currentTime = System.currentTimeMillis();

            return (creationTime + ticketLifeTimeSeconds * 1000) > currentTime;
        }
        return false;
    }

    private String findAvailableUsername(String email) throws ServerException {
        String candidate = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        int count = 1;
        while (getUserByName(candidate).isPresent()) {
            candidate = candidate.concat(valueOf(count++));
        }
        return candidate;
    }

    private Optional<User> getUserByName(String name) throws ServerException {
        try {
            User user = userManager.getByName(name);
            return Optional.of(user);
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }
}
