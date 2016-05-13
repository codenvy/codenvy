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

import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.auth.sso.server.RolesExtractor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * System/manager and System/admin roles extractor.
 * Works by matching given systemAdminGroupName   and systemManagerGroupName
 * to the user roles that exist in ldap
 *
 * @author Sergii Kabashniuk
 */
public class LdapRolesExtractor implements RolesExtractor {

    protected final JNDIRealm jndiRealm;
    private final   String    systemAdminGroupName;
    private final   String    systemManagerGroupName;

    /**
     * @param jndiRealm
     *         -  directory  server accessed via the Java Naming and Directory Interface (JNDI) APIs
     * @param systemAdminGroupName
     *         - if user has  given role we identify him as system/admin
     * @param systemManagerGroupName
     *         - if user has  given role we identify him as system/admin
     */
    @Inject
    public LdapRolesExtractor(JNDIRealm jndiRealm,
                              @Named("auth.sysldap.group_system_admin_name") String systemAdminGroupName,
                              @Named("auth.sysldap.group_system_manager_name") String systemManagerGroupName) {
        this.jndiRealm = jndiRealm;
        this.systemAdminGroupName = systemAdminGroupName;
        this.systemManagerGroupName = systemManagerGroupName;
    }


    @Override
    public Set<String> extractRoles(AccessTicket ticket, String workspaceId, String accountId) {
        if (ticket.getAuthHandlerType().equals(LdapAuthenticationHandler.HANDLER_TYPE)) {

            try {
                JNDIRealm.GenericPrincipal principal = jndiRealm.getPrincipal(ticket.getPrincipal().getUserName());
                Set<String> systemRoles = new HashSet<>(2);
                if (principal.getRoles().contains(systemAdminGroupName)) {
                    systemRoles.add("system/admin");
                }
                if (principal.getRoles().contains(systemManagerGroupName)) {
                    systemRoles.add("system/manager");
                }
                return systemRoles;

            } catch (Exception e) {
                throw new RuntimeException(e.getLocalizedMessage(), e);
            }
        }
        return Collections.emptySet();
    }
}
