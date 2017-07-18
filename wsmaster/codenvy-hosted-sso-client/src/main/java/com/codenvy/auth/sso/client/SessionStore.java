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
package com.codenvy.auth.sso.client;

import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage HttpSession's. Used to transfer relation's between session and token inside of SSO client.
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class SessionStore {
    private final ConcurrentHashMap<String, HttpSession> sessionsByToken;
    private final ConcurrentHashMap<String, HttpSession> sessionsById;
    private final ConcurrentHashMap<String, Set<String>> tokensBySessionId;

    public SessionStore() {
        this.sessionsByToken = new ConcurrentHashMap<>();
        this.sessionsById = new ConcurrentHashMap<>();
        this.tokensBySessionId = new ConcurrentHashMap<>();
    }

    public synchronized void saveSession(String token, HttpSession session) {
        String sessionId = session.getId();
        sessionsByToken.put(token, session);
        sessionsById.putIfAbsent(sessionId, session);
        Set<String> tokens = tokensBySessionId.get(sessionId);
        if (tokens == null) {
            tokens = new HashSet<>();
            tokensBySessionId.put(sessionId, tokens);
        }
        tokens.add(token);

    }

    public synchronized HttpSession removeSessionByToken(String token) {

        HttpSession session = sessionsByToken.remove(token);
        if (session != null) {
            String sessionId = session.getId();
            sessionsById.remove(sessionId);
            Set<String> tokens = tokensBySessionId.remove(sessionId);
            for (String otherToken : tokens) {
                if (!token.equals(otherToken)) {
                    sessionsByToken.remove(otherToken);
                }
            }

        }
        return session;

    }

    public HttpSession getSession(String token) {

        return sessionsByToken.get(token);

    }

    public synchronized void removeSessionById(String sessionId) {

        HttpSession session = sessionsById.remove(sessionId);
        if (session != null) {

            Set<String> tokens = tokensBySessionId.remove(sessionId);
            for (String token : tokens) {
                sessionsByToken.remove(token);
            }

        }

    }
}
