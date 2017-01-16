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
package com.codenvy.auth.sso.client;

import org.eclipse.che.commons.subject.Subject;

import java.security.Principal;

/** @author Sergii Kabashniuk */
public class SsoClientPrincipal implements Principal {
    private final Subject subject;
    private final String  clientUrl;
    private final String  token;

    public SsoClientPrincipal(String token,
                              String clientUrl,
                              Subject subject) {
        this.clientUrl = clientUrl;
        this.token = token;
        this.subject = subject;
    }


    public String getClientUrl() {
        return clientUrl;
    }

    public String getToken() {
        return token;
    }

    public Subject getUser() {
        return subject;
    }

    @Override
    public String getName() {
        return subject.getUserName();
    }
}
