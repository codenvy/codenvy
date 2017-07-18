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
import org.eclipse.che.api.workspace.server.jpa.JpaStackDao;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 *  JPA {@link StackDao} implementation that respects permissions on search by user.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class OnPremisesJpaStackDao extends JpaStackDao {

    @Inject
    private Provider<EntityManager> managerProvider;

    private static final String findByPermissionsQuery = " SELECT stack FROM StackPermissions perm " +
                                                         "        LEFT JOIN perm.stack stack  " +
                                                         "        WHERE (perm.userId IS NULL OR perm.userId  = :userId) " +
                                                         "        AND 'search' MEMBER OF perm.actions";

    private static final String findByPermissionsAndTagsQuery = " SELECT stack FROM StackPermissions perm " +
                                                                "        LEFT JOIN perm.stack stack  " +
                                                                "        LEFT JOIN stack.tags tag    " +
                                                                "        WHERE (perm.userId IS NULL OR perm.userId  = :userId) " +
                                                                "        AND 'search' MEMBER OF perm.actions" +
                                                                "        AND tag IN :tags " +
                                                                "        GROUP BY stack.id HAVING COUNT(tag) = :tagsSize";

    @Override
    @Transactional
    public List<StackImpl> searchStacks(@Nullable String userId,
                                        @Nullable List<String> tags,
                                        int skipCount,
                                        int maxItems) throws ServerException {
        final TypedQuery<StackImpl> query;
        if (tags == null || tags.isEmpty()) {
            query = managerProvider.get().createQuery(findByPermissionsQuery, StackImpl.class);
        } else {
            query = managerProvider.get()
                                   .createQuery(findByPermissionsAndTagsQuery, StackImpl.class)
                                   .setParameter("tags", tags)
                                   .setParameter("tagsSize", tags.size());
        }
        try {
            return query.setParameter("userId", userId)
                        .setMaxResults(maxItems)
                        .setFirstResult(skipCount)
                        .getResultList();
        } catch (RuntimeException x) {
            throw new ServerException(x.getLocalizedMessage(), x);
        }
    }
}
