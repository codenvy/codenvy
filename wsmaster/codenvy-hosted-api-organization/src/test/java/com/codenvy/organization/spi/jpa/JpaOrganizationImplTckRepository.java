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
package com.codenvy.organization.spi.jpa;

import com.codenvy.organization.spi.impl.OrganizationImpl;
import com.google.inject.Inject;
import com.google.inject.persist.UnitOfWork;

import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Organizations require to have own repository because it is important
 * to delete organization in reverse order that they were stored. It allows
 * to resolve problems with removing suborganization before parent organization removing.
 *
 * @author Sergii Leschenko
 */
public class JpaOrganizationImplTckRepository extends JpaTckRepository<OrganizationImpl> {
    @Inject
    protected Provider<EntityManager> managerProvider;

    @Inject
    protected UnitOfWork uow;

    private final List<OrganizationImpl> createdOrganizations = new ArrayList<>();

    public JpaOrganizationImplTckRepository() {
        super(OrganizationImpl.class);
    }

    @Override
    public void createAll(Collection<? extends OrganizationImpl> entities) throws TckRepositoryException {
        super.createAll(entities);
        //It's important to save organization to remove them in the reverse order
        createdOrganizations.addAll(entities);
    }

    @Override
    public void removeAll() throws TckRepositoryException {
        uow.begin();
        final EntityManager manager = managerProvider.get();
        try {
            manager.getTransaction().begin();

            for (int i = createdOrganizations.size() - 1; i > -1; i--) {
                // The query 'DELETE FROM ....' won't be correct as it will ignore orphanRemoval
                // and may also ignore some configuration options, while EntityManager#remove won't
                try {
                    final OrganizationImpl organizationToRemove = manager.createQuery("SELECT o FROM Organization o " +
                                                                                      "WHERE o.id = :id",
                                                                                      OrganizationImpl.class)
                                                                         .setParameter("id", createdOrganizations.get(i).getId())
                                                                         .getSingleResult();
                    manager.remove(organizationToRemove);
                } catch (NoResultException ignored) {
                    //it is already removed
                }
            }
            createdOrganizations.clear();

            manager.getTransaction().commit();
        } catch (RuntimeException x) {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            throw new TckRepositoryException(x.getLocalizedMessage(), x);
        } finally {
            uow.end();
        }

        //remove all objects that was created in tests
        super.removeAll();
    }
}
