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
package com.codenvy.spi.invite;

import com.codenvy.api.invite.InviteImpl;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;

import java.util.Optional;

/**
 * Defines data access object contract for {@link InviteImpl}.
 *
 * @author Sergii Leschenko
 */
public interface InviteDao {
    /**
     * Stores (create or updates) invite.
     *
     * @param invite
     *         invite to store
     * @return optional with previous state of invite
     * or empty optional if there was not a existing one
     * @throws ServerException
     *         when any other error occurs during invite storing
     */
    Optional<InviteImpl> store(InviteImpl invite) throws ServerException;

    /**
     * Returns invites for specified email.
     *
     * @param email
     *         email to retrieve invites
     * @param maxItems
     *         the maximum number of invites to return
     * @param skipCount
     *         the number of invites to skip
     * @return invites for specified email
     * @throws ServerException
     *         when any other error occurs during invites fetching
     */
    Page<InviteImpl> getInvites(String email, long skipCount, int maxItems) throws ServerException;

    /**
     * Returns invites for specified instance.
     *
     * @param domainId
     *         domain id to which specified instance belong to
     * @param instanceId
     *         instance id
     * @param maxItems
     *         the maximum number of invites to return
     * @param skipCount
     *         the number of invites to skip
     * @return invites for specified instance
     * @throws ServerException
     *         when any other error occurs during invites fetching
     */
    Page<InviteImpl> getInvites(String domainId, String instanceId, long skipCount, int maxItems) throws ServerException;

    /**
     * Returns invite for specified email and instance
     *
     * @param domainId
     *         domain id
     * @param instanceId
     *         instance id
     * @param email
     *         email to retrieve invite
     * @return invite for specified email and instance
     * @throws NotFoundException
     *         when invite for specified email and instance does not exist
     * @throws ServerException
     *         when any other error occurs during invite fetching
     */
    InviteImpl getInvite(String domainId, String instanceId, String email) throws NotFoundException, ServerException;

    /**
     * Removes invite of email related to the particular instanceId
     *
     * @param domainId
     *         domain id
     * @param instanceId
     *         instance id
     * @param email
     *         email
     * @throws ServerException
     *         when any other error occurs during permissions removing
     */
    void remove(String domainId, String instanceId, String email) throws ServerException;
}
