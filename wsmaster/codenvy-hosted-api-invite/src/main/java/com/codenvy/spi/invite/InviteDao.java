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
package com.codenvy.spi.invite;

import com.codenvy.api.invite.InviteImpl;
import java.util.Optional;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;

/**
 * Defines data access object contract for {@link InviteImpl}.
 *
 * @author Sergii Leschenko
 */
public interface InviteDao {
  /**
   * Stores (create or updates) invite.
   *
   * @param invite invite to store
   * @return optional with previous state of invite or empty optional if there was not a existing
   *     one
   * @throws ServerException when any other error occurs during invite storing
   */
  Optional<InviteImpl> store(InviteImpl invite) throws ServerException;

  /**
   * Returns invites for specified email.
   *
   * @param email email to retrieve invites
   * @param maxItems the maximum number of invites to return
   * @param skipCount the number of invites to skip
   * @return invites for specified email
   * @throws ServerException when any other error occurs during invites fetching
   */
  Page<InviteImpl> getInvites(String email, int maxItems, long skipCount) throws ServerException;

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
  Page<InviteImpl> getInvites(String domainId, String instanceId, long skipCount, int maxItems)
      throws ServerException;

  /**
   * Returns invite for specified email and instance
   *
   * @param domainId domain id
   * @param instanceId instance id
   * @param email email to retrieve invite
   * @return invite for specified email and instance
   * @throws NotFoundException when invite for specified email and instance does not exist
   * @throws ServerException when any other error occurs during invite fetching
   */
  InviteImpl getInvite(String domainId, String instanceId, String email)
      throws NotFoundException, ServerException;

  /**
   * Removes invite of email related to the particular instanceId
   *
   * @param domainId domain id
   * @param instanceId instance id
   * @param email email
   * @throws ServerException when any other error occurs during permissions removing
   */
  void remove(String domainId, String instanceId, String email) throws ServerException;
}
