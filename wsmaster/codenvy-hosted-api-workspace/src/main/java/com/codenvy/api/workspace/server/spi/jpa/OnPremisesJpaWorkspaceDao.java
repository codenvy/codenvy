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
package com.codenvy.api.workspace.server.spi.jpa;

import com.google.inject.persist.Transactional;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.jpa.JpaWorkspaceDao;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * JPA {@link WorkspaceDao} implementation that respects workers on get by user.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class OnPremisesJpaWorkspaceDao extends JpaWorkspaceDao {

    @Inject
    private Provider<EntityManager> manager;

    @Override
    @Transactional
    public List<WorkspaceImpl> getWorkspaces(String userId) throws ServerException {

        final String query = "SELECT ws FROM Worker worker  " +
                             "          LEFT JOIN worker.workspace ws " +
                             "          WHERE worker.userId = :userId " +
                             "          AND 'read' MEMBER OF worker.actions";

        try {
            return manager.get()
                          .createQuery(query, WorkspaceImpl.class)
                          .setParameter("userId", userId)
                          .getResultList();
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }
}
