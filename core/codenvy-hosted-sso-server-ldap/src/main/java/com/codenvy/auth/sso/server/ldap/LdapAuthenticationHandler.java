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
package com.codenvy.auth.sso.server.ldap;

import org.eclipse.che.api.auth.AuthenticationException;
import com.codenvy.api.dao.authentication.AuthenticationHandler;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;

import javax.inject.Inject;
import java.util.Collections;

/**
 * @author Sergii Kabashniuk
 */
public class LdapAuthenticationHandler implements AuthenticationHandler {

    public final static String HANDLER_TYPE = "sysldap";

    @Inject
    protected JNDIRealm jndiRealm;


    @Override
    public Subject authenticate(final String userId, final String password) throws AuthenticationException {
        JNDIRealm.GenericPrincipal user = jndiRealm.authenticate(userId, password);
        if (user == null) {
            throw new AuthenticationException("Invalid user name or password");
        }
        return new SubjectImpl(user.getName(), user.getDn(), null, Collections.<String>emptyList(), false);

    }

    @Override
    public String getType() {
        return HANDLER_TYPE;
    }
}
