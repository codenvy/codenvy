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

import com.codenvy.organization.api.event.OrganizationCreatedEvent;
import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.codenvy.organization.spi.MemberDao;
import com.codenvy.organization.spi.OrganizationDao;
import com.codenvy.organization.shared.model.Organization;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.commons.lang.NameGenerator;

import javax.inject.Inject;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Facade for Organization related operations.
 *
 * @author gazarenkov
 * @author Sergii Leschenko
 */
public class OrganizationManager {
    private final OrganizationDao organizationDao;
    private final MemberDao       memberDao;
    private final EventService    eventService;

    @Inject
    public OrganizationManager(OrganizationDao organizationDao,
                               MemberDao memberDao,
                               EventService eventService) {
        this.organizationDao = organizationDao;
        this.memberDao = memberDao;
        this.eventService = eventService;
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
     * @throws ServerException
     *         when any other error occurs during organization creation
     */
    public OrganizationImpl create(Organization newOrganization) throws ConflictException, ServerException {
        requireNonNull(newOrganization, "Required non-null organization");
        final OrganizationImpl organization = new OrganizationImpl(NameGenerator.generate("organization", 16),
                                                                   newOrganization.getName(),
                                                                   newOrganization.getParent());
        organizationDao.create(organization);
        eventService.publish(new OrganizationCreatedEvent(organization));
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
     *         when name updated with a value which is not unique
     * @throws ServerException
     *         when any other error occurs organization updating
     */
    public OrganizationImpl update(String organizationId, Organization update) throws NotFoundException,
                                                                                      ConflictException,
                                                                                      ServerException {
        requireNonNull(organizationId, "Required non-null organization id");
        requireNonNull(update, "Required non-null organization");
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
    public OrganizationImpl getById(String organizationId) throws NotFoundException, ServerException {
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
    public OrganizationImpl getByName(String organizationName) throws NotFoundException, ServerException {
        requireNonNull(organizationName, "Required non-null organization name");
        return organizationDao.getByName(organizationName);
    }

    /**
     * Gets child organizations by given parent.
     *
     * @param parent
     *         id of parent organizations
     * @return list of children organizations
     * @throws NullPointerException
     *         when {@code parent} is null
     * @throws ServerException
     *         when any other error occurs during organizations fetching
     */
    public List<OrganizationImpl> getByParent(String parent) throws ServerException {
        requireNonNull(parent, "Required non-null parent");
        return organizationDao.getByParent(parent);
    }

    /**
     * Gets list organizations where user is member.
     *
     * @param userId
     *         user id
     * @return list of organizations where user is member
     * @throws NullPointerException
     *         when {@code userId} is null
     * @throws ServerException
     *         when any other error occurs during organizations fetching
     */
    public List<OrganizationImpl> getByMember(String userId) throws ServerException {
        requireNonNull(userId, "Required non-null user id");
        return memberDao.getOrganizations(userId);
    }
}
