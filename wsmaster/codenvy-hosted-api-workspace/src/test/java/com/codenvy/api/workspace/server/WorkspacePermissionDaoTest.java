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
package com.codenvy.api.workspace.server;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.model.impl.PermissionsImpl;
import com.codenvy.api.workspace.server.spi.WorkerDao;
import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.NotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.codenvy.api.workspace.server.WorkspaceDomain.DELETE;
import static com.codenvy.api.workspace.server.WorkspaceDomain.DOMAIN_ID;
import static com.codenvy.api.workspace.server.WorkspaceDomain.READ;
import static com.codenvy.api.workspace.server.WorkspaceDomain.USE;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link WorkspacePermissionDao}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class WorkspacePermissionDaoTest {
    @Mock
    WorkerDao workerDao;

    @InjectMocks
    WorkspacePermissionDao permissionStorage;

    @Test
    public void shouldBeAbleToStorePermissions() throws Exception {
        permissionStorage.store(new PermissionsImpl("user123",
                                                    "workspace",
                                                    "workspace123",
                                                    asList(READ, USE)));

        verify(workerDao).store(eq(new WorkerImpl("workspace123", "user123", asList(READ, USE))));
    }

    @Test
    public void shouldBeAbleToGetPermissionsByUserAndDomainAndInstance() throws Exception {
        when(workerDao.getWorker(anyString(), anyString()))
                .thenReturn(new WorkerImpl("workspace123", "user123", asList(READ, USE)));

        PermissionsImpl result = permissionStorage.get("user123", DOMAIN_ID, "workspace123");

        verify(workerDao).getWorker(eq("workspace123"), eq("user123"));
        assertEquals(result, new PermissionsImpl("user123",
                                                 "workspace",
                                                 "workspace123",
                                                 asList(READ, USE)));
    }

    @Test
    public void shouldBeAbleToGetPermissionsByDomainAndInstance() throws Exception {
        when(workerDao.getWorkers(anyString()))
                .thenReturn(Collections.singletonList(new WorkerImpl("workspace123", "user123", asList(READ, USE))));

        List<PermissionsImpl> result = permissionStorage.getByInstance(DOMAIN_ID, "workspace123");

        assertEquals(result.size(), 1);
        verify(workerDao).getWorkers(eq("workspace123"));
        assertEquals(result.get(0), new PermissionsImpl("user123",
                                                        "workspace",
                                                        "workspace123",
                                                        asList(READ, USE)));
    }

    @Test
    public void shouldBeAbleToCheckPermissionExistence() throws Exception {
        when(workerDao.getWorker(anyString(), anyString()))
                .thenReturn(new WorkerImpl("workspace123", "user123", asList(READ,
                                                                             USE)));

        boolean existence = permissionStorage.exists("user123", DOMAIN_ID, "workspace123", READ);
        boolean nonExistence = permissionStorage.exists("user123", DOMAIN_ID, "workspace123", DELETE);

        assertTrue(existence);
        assertFalse(nonExistence);
    }

    @Test
    public void shouldReturnFalseOnCheckPermissionExistenceWhenWorkerDoesNotExist() throws Exception {
        when(workerDao.getWorker(anyString(), anyString())).thenThrow(new NotFoundException(""));

        boolean result = permissionStorage.exists("user123", DOMAIN_ID, "workspace123", DELETE);

        assertFalse(result);
    }

    @Test
    public void shouldBeAbleToRemovePermissions() throws Exception {
        permissionStorage.remove("user123", DOMAIN_ID, "workspace123");

        verify(workerDao).removeWorker(eq("workspace123"), eq("user123"));
    }

    @Test
    public void shouldReturnWorkspaceDomain() {
        Set<AbstractPermissionsDomain> supportedDomains = permissionStorage.getDomains();

        assertEquals(supportedDomains, ImmutableSet.of(new WorkspaceDomain()));
    }
}
