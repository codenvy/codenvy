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
package com.codenvy.organization.spi;

import com.codenvy.organization.spi.impl.MemberImpl;
import com.codenvy.organization.spi.impl.OrganizationImpl;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;

import java.util.List;

/**
 * Defines data access object for {@link MemberImpl}
 *
 * @author Sergii Leschenko
 */
public interface MemberDao {
    /**
     * Stores (adds or updates) member.
     *
     * @param member
     *         member to store
     * @throws NullPointerException
     *         when {@code member} is null
     * @throws ServerException
     *         when organization or user doesn't exist
     * @throws ServerException
     *         when any other error occurs during member storing
     */
    void store(MemberImpl member) throws ServerException;

    /**
     * Removes member with given organization and user
     *
     * @param userId
     *         id of user
     * @param organizationId
     *         id of organization
     * @throws NullPointerException
     *         when {@code organizationId} or {@code userId} is null
     * @throws ServerException
     *         when any other error occurs during member removing
     */
    void remove(String userId, String organizationId) throws ServerException;

    /**
     * Returns member for specified organization and user
     *
     * @param organizationId
     *         organization id
     * @param userId
     *         user id
     * @return member for specified organization and user
     * @throws NullPointerException
     *         when {@code organizationId} or {@code userId} is null
     * @throws NotFoundException
     *         when member for given user and organization was not found
     * @throws ServerException
     *         when any other error occurs during member fetching
     */
    MemberImpl getMember(String organizationId, String userId) throws NotFoundException, ServerException;

    /**
     * Returns all members of given organization
     *
     * @param organizationId
     *         organization id
     * @throws NullPointerException
     *         when {@code organizationId} is null
     * @throws ServerException
     *         when any other error occurs during members fetching
     */
    List<MemberImpl> getMembers(String organizationId) throws ServerException;

    /**
     * Returns all memberships of given user
     *
     * @param userId
     *         user id
     * @throws NullPointerException
     *         when {@code userId} is null
     * @throws ServerException
     *         when any other error occurs during members fetching
     */
    List<MemberImpl> getMemberships(String userId) throws ServerException;

    /**
     * Gets list organizations where user is member.
     *
     * @param userId
     *         user id
     * @param maxItems
     *         the maximum number of organizations to return
     * @param skipCount
     *         the number of organizations to skip
     * @return list of organizations where user is member
     * @throws NullPointerException
     *         when {@code userId} is null
     * @throws ServerException
     *         when any other error occurs during organizations fetching
     */
    Page<OrganizationImpl> getOrganizations(String userId, int maxItems, int skipCount) throws ServerException;
}
