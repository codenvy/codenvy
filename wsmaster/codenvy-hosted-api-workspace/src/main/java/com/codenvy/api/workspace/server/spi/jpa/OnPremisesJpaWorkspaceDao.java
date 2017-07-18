/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
