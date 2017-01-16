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
