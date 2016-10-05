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
package com.codenvy.organization.api;

import com.codenvy.organization.shared.model.Organization;
import com.codenvy.organization.spi.MemberDao;
import com.codenvy.organization.spi.OrganizationDao;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.google.common.collect.Sets;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.NameGenerator;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Facade for Organization related operations.
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 */
@Singleton
public class OrganizationManager {
    private final OrganizationDao organizationDao;
    private final MemberDao       memberDao;
    private final Set<String>     reservedNames;

    @Inject
    public OrganizationManager(OrganizationDao organizationDao,
                               MemberDao memberDao,
                               @Named("che.auth.reserved_user_names") String[] reservedNames) {
        this.organizationDao = organizationDao;
        this.memberDao = memberDao;
        this.reservedNames = Sets.newHashSet(reservedNames);
    }

    /**
     * Creates new organization
     *
     * @param newOrganization
     *         organization to create
     * @return created organization
     * @throws NullPointerException
     *         when {@code organization} is null
     * @throws ConflictException
     *         when organization with such id/name already exists
     * @throws ConflictException
     *         when specified organization name is reserved
     * @throws ServerException
     *         when any other error occurs during organization creation
     */
    public Organization create(Organization newOrganization) throws ConflictException, ServerException {
        requireNonNull(newOrganization, "Required non-null organization");
        checkNameReservation(newOrganization.getName());
        final OrganizationImpl organization = new OrganizationImpl(NameGenerator.generate("organization", 16),
                                                                   newOrganization.getName(),
                                                                   newOrganization.getParent());
        organizationDao.create(organization);
        return organization;
    }

    /**
     * Updates organization with new entity.
     *
     * @param organizationId
     *         id of organization to update
     * @param update
     *         organization update
     * @throws NullPointerException
     *         when {@code organizationId} or {@code update} is null
     * @throws NotFoundException
     *         when organization with given id doesn't exist
     * @throws ConflictException
     *         when name updated with a value which is reserved or is not unique
     * @throws ServerException
     *         when any other error occurs organization updating
     */
    public Organization update(String organizationId, Organization update) throws NotFoundException,
                                                                                  ConflictException,
                                                                                  ServerException {
        requireNonNull(organizationId, "Required non-null organization id");
        requireNonNull(update, "Required non-null organization");
        checkNameReservation(update.getName());
        final OrganizationImpl organization = organizationDao.getById(organizationId);
        organization.setName(update.getName());
        organizationDao.update(organization);
        return organization;
    }

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
    public void remove(String organizationId) throws ServerException {
        requireNonNull(organizationId, "Required non-null organization id");
        organizationDao.remove(organizationId);
    }

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
    public Organization getById(String organizationId) throws NotFoundException, ServerException {
        requireNonNull(organizationId, "Required non-null organization id");
        return organizationDao.getById(organizationId);
    }

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
    public Organization getByName(String organizationName) throws NotFoundException, ServerException {
        requireNonNull(organizationName, "Required non-null organization name");
        return organizationDao.getByName(organizationName);
    }

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
    public Page<? extends Organization> getByParent(String parent, int maxItems, int skipCount) throws ServerException {
        requireNonNull(parent, "Required non-null parent");
        return organizationDao.getByParent(parent, maxItems, skipCount);
    }

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
    public Page<? extends Organization> getByMember(String userId, int maxItems, int skipCount) throws ServerException {
        requireNonNull(userId, "Required non-null user id");
        return memberDao.getOrganizations(userId, maxItems, skipCount);
    }

    /**
     * Checks reservation of organization name
     *
     * @param organizationName
     *         organization name to check
     * @throws ConflictException
     *         when organization name is reserved and can be used by user
     */
    private void checkNameReservation(String organizationName) throws ConflictException {
        if (reservedNames.contains(organizationName.toLowerCase())) {
            throw new ConflictException(String.format("Organization name '%s' is reserved", organizationName));
        }
    }
}
