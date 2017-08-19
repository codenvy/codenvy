/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.api.invite;

import static java.util.Objects.requireNonNull;

import com.codenvy.api.invite.event.InviteCreatedEvent;
import com.codenvy.api.permission.server.PermissionsManager;
import com.codenvy.api.workspace.server.WorkspaceDomain;
import com.codenvy.organization.api.permissions.OrganizationDomain;
import com.codenvy.shared.invite.model.Invite;
import com.codenvy.spi.invite.InviteDao;
import com.google.inject.persist.Transactional;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;

/**
 * Facade for invite related operations.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class InviteManager {
  private final PermissionsManager permissionsManager;
  private final UserManager userManager;
  private final InviteDao inviteDao;
  private final EventService eventService;

  @Inject
  public InviteManager(
      PermissionsManager permissionsManager,
      UserManager userManager,
      InviteDao inviteDao,
      EventService eventService) {
    this.permissionsManager = permissionsManager;
    this.userManager = userManager;
    this.inviteDao = inviteDao;
    this.eventService = eventService;
  }

  /**
   * Stores (create or updates) invite.
   *
   * <p>It also send email invite on initial invite creation.
   *
   * @param invite invite to store
   * @throws ConflictException when user is specified email is already registered
   * @throws ServerException when any other error occurs during invite storing
   */
  @Transactional(rollbackOn = {RuntimeException.class, ServerException.class})
  public void store(Invite invite) throws NotFoundException, ConflictException, ServerException {
    requireNonNull(invite, "Required non-null invite");
    String domainId = invite.getDomainId();
    if (!OrganizationDomain.DOMAIN_ID.equals(domainId)
        && !WorkspaceDomain.DOMAIN_ID.equals(domainId)) {
      throw new ConflictException("Invitations for specified domain are not supported");
    }
    permissionsManager.checkActionsSupporting(domainId, invite.getActions());

    try {
      userManager.getByEmail(invite.getEmail());
      throw new ConflictException("User with specified id is already registered");
    } catch (NotFoundException ignored) {
    }

    Optional<InviteImpl> existingInvite = inviteDao.store(new InviteImpl(invite));
    if (!existingInvite.isPresent()) {
      Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
      eventService.publish(
          new InviteCreatedEvent(
              currentSubject.isAnonymous() ? null : currentSubject.getUserId(), invite));
    }
  }

  /**
   * Returns invite for specified email and instance.
   *
   * @param domainId domain id
   * @param instanceId instance id
   * @param email email
   * @return invite for specified email and instance
   * @throws NotFoundException when invite for specified email and instance does not exist
   * @throws ServerException when any other exception occur on invite fetching
   */
  public Invite getInvite(String domainId, String instanceId, String email)
      throws NotFoundException, ServerException {
    requireNonNull(domainId, "Required non-null domain id");
    requireNonNull(instanceId, "Required non-null instance id");
    requireNonNull(email, "Required non-null email");
    return inviteDao.getInvite(domainId, instanceId, email);
  }

  /**
   * Returns invites for specified email.
   *
   * @param email email to retrieve invites
   * @param maxItems the maximum number of invites to return
   * @param skipCount the number of invites to skip
   * @return invites for specified email
   * @throws ServerException when any other error occurs during invites fetching
   */
  public Page<? extends Invite> getInvites(String email, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(email, "Required non-null email");
    return inviteDao.getInvites(email, maxItems, skipCount);
  }

  /**
   * Returns invites for specified instance.
   *
   * @param domainId domain id to which specified instance belong to
   * @param instanceId instance id
   * @param maxItems the maximum number of invites to return
   * @param skipCount the number of invites to skip
   * @return invites for specified instance
   * @throws ServerException when any other error occurs during invites fetching
   */
  public Page<? extends Invite> getInvites(
      String domainId, String instanceId, long skipCount, int maxItems) throws ServerException {
    requireNonNull(domainId, "Required non-null domain id");
    requireNonNull(instanceId, "Required non-null instance id");
    return inviteDao.getInvites(domainId, instanceId, skipCount, maxItems);
  }

  /**
   * Removes invite of email related to the particular instance.
   *
   * @param domainId domainId id
   * @param instanceId instanceId id
   * @param email email
   * @throws ServerException when any other error occurs during permissions removing
   */
  public void remove(String domainId, String instanceId, String email) throws ServerException {
    requireNonNull(domainId, "Required non-null domain id");
    requireNonNull(instanceId, "Required non-null instance id");
    requireNonNull(email, "Required non-null email");

    inviteDao.remove(domainId, instanceId, email);
  }
}
