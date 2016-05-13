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

import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.api.dao.ldap.InitialLdapContextFactory;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.util.Collections.emptySet;

/**
 * Get user principal with roles from account service.
 *
 * @author Alexander Garagatyi
 */
public class OrgServiceRolesExtractor implements RolesExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(OrgServiceRolesExtractor.class);
    private final PreferenceDao             preferenceDao;
    private final InitialLdapContextFactory contextFactory;
    private final String                    containerDn;
    private final String                    userDn;
    private final String                    oldUserDn;
    private final String                    roleAttrName;
    private final String                    allowedRole;

    @Inject
    public OrgServiceRolesExtractor(PreferenceDao preferenceDao,
                                    @Named("user.ldap.user_container_dn") String userContainerDn,
                                    @Named("user.ldap.user_dn") String userDn,
                                    @Named("user.ldap.old_user_dn") String oldUserDn,
                                    @Nullable @Named("user.ldap.attr.role_name") String roleAttrName,
                                    @Nullable @Named("user.ldap.allowed_role") String allowedRole,
                                    InitialLdapContextFactory contextFactory) {
        this.preferenceDao = preferenceDao;
        this.roleAttrName = roleAttrName;
        this.allowedRole = allowedRole;
        this.containerDn = userContainerDn;
        this.userDn = userDn;
        this.oldUserDn = oldUserDn;
        this.contextFactory = contextFactory;
    }

    @Override
    public Set<String> extractRoles(AccessTicket ticket, String workspaceId, String accountId) {
        //TODO: find better solution
        //skip roles extraction if ticket handler type equals to 'sysldap'
        if ("sysldap".equals(ticket.getAuthHandlerType())) {
            return emptySet();
        }
        try {
            final Set<String> ldapRoles = getRoles(ticket.getPrincipal().getUserId());
            if (allowedRole != null && !ldapRoles.contains(allowedRole)) {
                return emptySet();
            }

            final Set<String> userRoles = new HashSet<>();

            final Map<String, String> preferences = preferenceDao.getPreferences(ticket.getPrincipal().getUserId());
            if (parseBoolean(preferences.get("temporary"))) {
                userRoles.add("temp_user");
            } else {
                userRoles.add("user");
            }

            if (ldapRoles.contains("system/admin")) {
                userRoles.add("system/admin");
            }
            if (ldapRoles.contains("system/manager")) {
                userRoles.add("system/manager");
            }

//            User user = userDao.getById(ticket.getPrincipal().getId());

//            if (accountId != null) {
//                Account account = accountDao.getById(accountId);
//                if (account != null) {
//                    accountDao.getMembers(accountId)
//                              .stream()
//                              .filter(accountMember -> accountMember.getUserId().equals(user.getId()))
//                              .forEach(accountMember -> userRoles.addAll(accountMember.getRoles()));
//                }
//            }

//            membershipDao.getMemberships(user.getId())
//                         .stream()
//                         .filter(membership -> membership.getSubjectId().equals(workspaceId))
//                         .forEach(membership -> userRoles.addAll(membership.getRoles()));
            return userRoles;
        } catch (NotFoundException e) {
            return emptySet();
        } catch (ServerException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage());
        }

    }

    //package-private used in test
    Set<String> getRoles(String id) throws ServerException, NotFoundException {
        InitialLdapContext context = null;
        NamingEnumeration rolesEnum = null;
        try {
            context = contextFactory.createContext();

            Attributes userAttrs;
            try {
                userAttrs = context.getAttributes(formatDn(userDn, id));
            } catch (NameNotFoundException ex) {
                //if not found -> try to find user using old dn
                userAttrs = context.getAttributes(formatDn(oldUserDn, id));

                //if attributes were found then rename current entity
                final String fromDnVal = userAttrs.get(oldUserDn).get().toString();
                final String toDnVal = userAttrs.get(userDn).get().toString();
                context.rename(formatDn(oldUserDn, fromDnVal), formatDn(userDn, toDnVal));
            }
            final Attribute rolesAttr = userAttrs.get(roleAttrName);
            final Set<String> roles = new HashSet<>();
            if (rolesAttr != null) {
                rolesEnum = rolesAttr.getAll();
                while (rolesEnum.hasMoreElements()) {
                    roles.add(rolesEnum.next().toString());
                }
            }
            return roles;
        } catch (NameNotFoundException nfEx) {
            throw new NotFoundException(format("User with id '%s' was not found", id));
        } catch (NamingException e) {
            throw new ServerException(e.getMessage(), e);
        } finally {
            close(context);
            close(rolesEnum);
        }
    }

    private String formatDn(String userDn, String id) {
        return userDn + '=' + id + ',' + containerDn;
    }

    private void close(Context ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void close(NamingEnumeration<?> enumeration) {
        if (enumeration != null) {
            try {
                enumeration.close();
            } catch (NamingException e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }
}
