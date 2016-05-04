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
package com.codenvy.machine.authentication.server;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.eclipse.che.api.core.NotFoundException;

import javax.inject.Singleton;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;
import static org.eclipse.che.commons.lang.NameGenerator.generate;

/**
 * Table-based storage of machine security tokens.
 * Table rows is workspace id's, columns - user id's.
 * Table is synchronized externally as required by its javadoc.
 * @see {HashBasedTable}
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
public class MachineTokenRegistry {

    private final Table<String, String, String> tokens = HashBasedTable.create();
    private final ReadWriteLock                 lock   = new ReentrantReadWriteLock();

    /**
     * Generates new machine security token for given user and workspace.
     *
     * @param userId
     *        id of user to generate token for
     * @param workspaceId
     *        id of workspace to generate token for
     */
    public void generateToken(String userId, String workspaceId) {
        lock.writeLock().lock();
        try {
            tokens.put(workspaceId, userId, generate("", 128));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets machine security token for user and workspace.
     *
     * @param userId
     *        id of user to get token
     * @param workspaceId
     *        id of workspace to get token
     * @return machine security token
     * @throws NotFoundException
     *         when no token exists for given user and workspace
     */
    public String getToken(String userId, String workspaceId) throws NotFoundException {
        lock.readLock().lock();
        try {
            final String token = tokens.get(workspaceId, userId);
            if (token == null) {
                throw new NotFoundException(format("Token not found for user %s and workspace %s", userId, workspaceId));
            }
            return token;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets userId by machine token
     *
     * @return user identifier
     * @throws NotFoundException
     *         when no token exists for given user and workspace
     */
    public String getUserId(String token) throws NotFoundException {
        lock.readLock().lock();
        try {
            for (Table.Cell<String, String, String> tokenCell : tokens.cellSet()) {
                if (tokenCell.getValue().equals(token)) {
                    return tokenCell.getColumnKey();
                }
            }
            throw new NotFoundException("User not found for token " + token);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Invalidates machine security tokens for all users of given workspace.
     *
     * @param workspaceId
     *         workspace to invalidate tokens
     */
    public void removeTokens(String workspaceId) {
        lock.writeLock().lock();
        try {
            tokens.row(workspaceId).clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
