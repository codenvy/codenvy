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
package com.codenvy.organization.api.permissions;

import com.codenvy.organization.spi.MemberDao;
import com.codenvy.organization.spi.impl.MemberImpl;
import com.codenvy.organization.shared.model.Member;
import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.PermissionsImpl;
import com.codenvy.api.permission.server.dao.PermissionsStorage;
import com.codenvy.api.permission.shared.Permissions;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link PermissionsStorage} for storing permissions of {@link OrganizationDomain}
 *
 * <p>This implementation adapts {@link Permissions} and {@link Member} and use
 * {@link MemberDao} as storage of permissions
 *
 * @author Sergii Leschenko
 */
@Singleton
public class OrganizationPermissionStorage implements PermissionsStorage {
    private final Set<AbstractPermissionsDomain> supportedDomain;
    private final MemberDao                      memberDao;

    @Inject
    public OrganizationPermissionStorage(MemberDao memberDao) {
        this.memberDao = memberDao;
        this.supportedDomain = ImmutableSet.of(new OrganizationDomain());
    }

    @Override
    public Set<AbstractPermissionsDomain> getDomains() {
        return supportedDomain;
    }

    @Override
    public void store(PermissionsImpl permissions) throws ServerException{
        memberDao.store(new MemberImpl(permissions.getUser(),
                                       permissions.getInstance(),
                                       permissions.getActions()));
    }

    @Override
    public PermissionsImpl get(String user, String domain, String instance) throws ServerException, NotFoundException {
        return toPermission(memberDao.getMember(instance, user));
    }

    @Override
    public List<PermissionsImpl> getByInstance(String domain, String instance) throws ServerException {
        return toPermissions(memberDao.getMembers(instance));
    }

    @Override
    public boolean exists(String user, String domain, String instance, String action) throws ServerException {
        try {
            return memberDao.getMember(instance, user)
                            .getActions()
                            .stream()
                            .anyMatch(action::equals);
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public void remove(String user, String domain, String instance) throws ServerException, NotFoundException {
        memberDao.remove(instance, user);
    }

    private PermissionsImpl toPermission(MemberImpl member) {
        return new PermissionsImpl(member.getUserId(),
                                   OrganizationDomain.DOMAIN_ID,
                                   member.getOrganizationId(),
                                   member.getActions());
    }

    private List<PermissionsImpl> toPermissions(List<MemberImpl> workers) {
        return workers.stream()
                      .map(this::toPermission)
                      .collect(Collectors.toList());
    }
}
