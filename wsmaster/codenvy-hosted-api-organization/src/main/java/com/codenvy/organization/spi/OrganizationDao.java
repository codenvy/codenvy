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

import com.codenvy.organization.spi.impl.OrganizationImpl;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;

/**
 * Defines data access object for {@link OrganizationImpl}
 *
 * @author Sergii Leschenko
 */
public interface OrganizationDao {
    /**
     * Creates organization.
     *
     * @param organization
     *         organization to create
     * @throws NullPointerException
     *         when {@code organization} is null
     * @throws ConflictException
     *         when organization with such id/name already exists
     * @throws ServerException
     *         when any other error occurs during organization creation
     */
    void create(OrganizationImpl organization) throws ServerException, ConflictException;

    /**
     * Updates organization with new entity.
     *
     * @param update
     *         organization update
     * @throws NullPointerException
     *         when {@code update} is null
     * @throws NotFoundException
     *         when organization with id {@code organization.getId()} doesn't exist
     * @throws ConflictException
     *         when name updated with a value which is not unique
     * @throws ServerException
     *         when any other error occurs organization updating
     */
    void update(OrganizationImpl update) throws NotFoundException, ConflictException, ServerException;

    /**
     * Removes organization with given id
     *
     * @param organizationId
     *         organization id
     * @throws NullPointerException
     *         when {@code organizationId} is null
     * @throws ServerException
     *         when any other error occurs during organization removing
     */
    void remove(String organizationId) throws ServerException;

    /**
     * Gets organization by identifier.
     *
     * @param organizationId
     *         organization id
     * @return organization instance
     * @throws NullPointerException
     *         when {@code organizationId} is null
     * @throws NotFoundException
     *         when organization with given id was not found
     * @throws ServerException
     *         when any other error occurs during organization fetching
     */
    OrganizationImpl getById(String organizationId) throws NotFoundException, ServerException;

    /**
     * Gets organization by name.
     *
     * @param organizationName
     *         organization name
     * @return organization instance
     * @throws NullPointerException
     *         when {@code organizationName} is null
     * @throws NotFoundException
     *         when organization with given name was not found
     * @throws ServerException
     *         when any other error occurs during organization fetching
     */
    OrganizationImpl getByName(String organizationName) throws NotFoundException, ServerException;

    /**
     * Gets child organizations by given parent.
     *
     * @param parent
     *         id of parent organizations
     * @param maxItems
     *         the maximum number of organizations to return
     * @param skipCount
     *         the number of organizations to skip
     * @return list of children organizations
     * @throws NullPointerException
     *         when {@code parent} is null
     * @throws ServerException
     *         when any other error occurs during organizations fetching
     */
    Page<OrganizationImpl> getByParent(String parent, int maxItems, long skipCount) throws ServerException;
}
