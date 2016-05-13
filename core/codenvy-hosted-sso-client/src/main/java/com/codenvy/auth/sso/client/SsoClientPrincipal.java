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
package com.codenvy.auth.sso.client;

import org.eclipse.che.commons.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** @author Sergii Kabashniuk */
public class SsoClientPrincipal implements Principal {
    private static final Logger LOG = LoggerFactory.getLogger(SsoClientPrincipal.class);

    private final Map<RolesContext, Subject> contextUserMap;
    private final String                     clientUrl;
    private final String                     token;
    private final ServerClient               ssoServerClient;
    private final String                     userName;
    private final String                     userId;


    public SsoClientPrincipal(String token,
                              String clientUrl,
                              RolesContext initialContext,
                              Subject initialPrincipal,
                              ServerClient ssoServerClient) {
        this.clientUrl = clientUrl;
        this.token = token;
        this.ssoServerClient = ssoServerClient;
        this.userName = initialPrincipal.getUserName();
        this.userId = initialPrincipal.getUserId();
        this.contextUserMap = new ConcurrentHashMap<>();
        this.contextUserMap.put(initialContext, initialPrincipal);
    }

    public boolean hasUserInContext(RolesContext context) {
        if (context != null) {
            Subject subject = contextUserMap.get(context);
            if (subject == null) {
                subject = ssoServerClient.getSubject(token, clientUrl,
                                                     context.getWorkspaceId(),
                                                     context.getAccountId());
                if (subject != null && userId.equals(subject.getUserId())) {
                    contextUserMap.put(context, subject);
                }
            }
            return subject != null;
        }
        return true;


    }

    public String getClientUrl() {
        return clientUrl;
    }

    public String getToken() {
        return token;
    }

    public Subject getUser(RolesContext rolesContext) {
        return contextUserMap.get(rolesContext);
    }

    /**
     * Remove roles for given context.
     *
     * @param context
     */
    void invialidateRoles(RolesContext context) {
        contextUserMap.remove(context);
    }

    public boolean isUserInRole(String roleName, RolesContext context) {

        Subject contextRoles = contextUserMap.get(context);
        if (contextRoles != null) {
            return contextRoles.isMemberOf(roleName);
        }
        return false;
    }

    @Override
    public String getName() {
        return userName;
    }
}
