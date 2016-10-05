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
package com.codenvy.service.http;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.name.Named;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Cache ~ 1000 Workspace entries for 10 minutes.
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class WorkspaceInfoCache {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceInfoCache.class);

    private final LoadingCache<Key, Workspace> workspaceCache;

    @Inject
    public WorkspaceInfoCache(WorkspaceCacheLoader cacheLoader) {
        this.workspaceCache = CacheBuilder.newBuilder()
                                          .maximumSize(1000)
                                          .expireAfterWrite(10, TimeUnit.MINUTES)
                                          .build(cacheLoader);

    }

    /**
     * @param wsName
     *         - workspace name
     * @return - workspace entry
     * @throws ServerException
     * @throws NotFoundException
     */
    public Workspace getByName(String wsName, String wsOwner) throws ServerException, NotFoundException {
        try {
            return doGet(new Key(wsName, wsOwner, false));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof NotFoundException) {
                throw ((NotFoundException)e.getCause());
            } else if (e.getCause() instanceof ServerException) {
                throw ((ServerException)e.getCause());
            }
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @param id
     *         - workspace id.
     * @return - workspace entry
     * @throws ServerException
     * @throws NotFoundException
     */
    public Workspace getById(String id) throws ServerException, NotFoundException {
        try {
            return doGet(new Key(id, null, true));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof NotFoundException) {
                throw ((NotFoundException)e.getCause());
            } else if (e.getCause() instanceof ServerException) {
                throw ((ServerException)e.getCause());
            }
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }


    /**
     * Remove workspace from cache by workspace id.
     *
     * @param id
     *         - id of workspace to remove.
     */
    public void removeById(String id) {
        workspaceCache.invalidate(new Key(id, null, true));
    }

    /**
     * Remove workspace by workspace name.
     *
     * @param wsName
     *         - name workspace to remove
     */
    public void removeByName(String wsName, String wsOwner) {
        workspaceCache.invalidate(new Key(wsName, wsName, false));
    }

    private Workspace doGet(Key key) throws ServerException, NotFoundException, ExecutionException {
        Workspace workspace = workspaceCache.get(key);
        if (workspace.isTemporary()) {
            if (workspace.getAttributes().containsKey("allowAnyoneAddMember")) {
                return workspace;
            }
            workspaceCache.invalidate(key);
            workspaceCache.invalidate(key.isUuid ?
                                      new Key(workspace.getConfig().getName(), null, false) :
                                      new Key(workspace.getId(), workspace.getNamespace(), true));
            workspace = workspaceCache.get(key);
        }
        return workspace;

    }

    public abstract static class WorkspaceCacheLoader extends CacheLoader<Key, Workspace> {

    }

    /**
     * Cacheloader that gets Workspace from DAO
     */
    public static class ManagerCacheLoader extends WorkspaceCacheLoader {
        @Inject
        WorkspaceManager manager;

        @Override
        public Workspace load(Key key) throws Exception {
            LOG.debug("Load {} from manager ", key.key);
            try {
                if (key.isUuid) {
                    return manager.getWorkspace(key.key);
                } else {
                    return manager.getWorkspace(key.key, key.userId);
                }
            } catch (Exception e) {
                LOG.debug(e.getLocalizedMessage(), e);
                throw e;
            }
        }
    }

    /**
     * Cacheloader that gets Workspace from API
     */
    public static class HttpWorkspaceCacheLoader extends WorkspaceCacheLoader {

        @Inject
        @Named("che.api")
        URI apiEndpoint;

        @Inject
        HttpJsonRequestFactory requestFactory;

        @Override
        public Workspace load(Key key) throws Exception {
            LOG.debug("Load {} from manager ", key.key);
            try {
                String getWorkspaceUrl;
                if (key.isUuid) {
                    getWorkspaceUrl = "workspace/" + key.key;
                } else {
                    getWorkspaceUrl = "workspace?name=" + key.key;
                }

                return requestFactory.fromUrl(apiEndpoint.resolve(getWorkspaceUrl).toString())
                                     .useGetMethod()
                                     .request()
                                     .asDto(WorkspaceDto.class);
            } catch (Exception e) {
                LOG.warn("Not able to get information for {} - {}", key.key, key.isUuid);
                LOG.debug(e.getLocalizedMessage(), e);
                throw e;
            }
        }
    }

    private static final class Key {
        final String  key;
        final String  userId;
        final boolean isUuid;

        private Key(String key, String userId, boolean isUuid) {
            this.key = key;
            this.userId = userId;
            this.isUuid = isUuid;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key)obj;
            return isUuid == other.isUuid &&
                   Objects.equals(key, other.key) &&
                   Objects.equals(userId, other.userId);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = hash * 31 + Objects.hashCode(key);
            hash = hash * 31 + Objects.hashCode(userId);
            hash = hash * 31 + Boolean.hashCode(isUuid);
            return hash;
        }
    }
}
