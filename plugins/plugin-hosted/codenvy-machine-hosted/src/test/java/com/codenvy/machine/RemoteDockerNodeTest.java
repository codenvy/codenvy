/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.machine;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

import com.codenvy.machine.backup.DockerEnvironmentBackupManager;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Igor Vinokur */
@Listeners(MockitoTestNGListener.class)
public class RemoteDockerNodeTest {
  private static final String PATH = "WorkspacePath";

  @Mock private DockerEnvironmentBackupManager backupManager;
  @Mock private DockerConnector dockerConnector;
  @Mock private WorkspaceFolderPathProvider pathProvider;
  @Mock private Exec exec;

  private RemoteDockerNode remoteDockerNode;

  @BeforeMethod
  public void setUp() throws Exception {
    when(pathProvider.getPath("WorkspaceId")).thenReturn(PATH);
    when(dockerConnector.createExec(any())).thenReturn(exec);
    when(exec.getId()).thenReturn("ExecId");
    remoteDockerNode =
        new RemoteDockerNode(dockerConnector, "ContainerId", "WorkspaceId", backupManager);
  }

  @Test
  public void shouldRestoreWorkspaceBackupOnNodeBinding() throws Exception {
    // when
    remoteDockerNode.bindWorkspace();

    // then
    verify(backupManager)
        .restoreWorkspaceBackup(eq("WorkspaceId"), eq("ContainerId"), eq("127.0.0.1"));
  }

  @Test
  public void shouldBackupAndCleanupWSOnNodeUnbinding() throws Exception {
    remoteDockerNode.bindWorkspace();

    // when
    remoteDockerNode.unbindWorkspace();

    // then
    verify(backupManager)
        .backupWorkspaceAndCleanup(eq("WorkspaceId"), eq("ContainerId"), eq("127.0.0.1"));
  }

  @Test
  public void backupIsNotCalledWhenRestoreIsNotCalled() throws Exception {
    remoteDockerNode.unbindWorkspace();

    verifyBackupIsNeverCalled();
  }

  @Test
  public void backupIsNotCalledWhenRestoreIsFailed() throws Exception {
    doThrow(new ServerException("no!"))
        .when(backupManager)
        .restoreWorkspaceBackup(anyString(), anyString(), anyString());
    try {
      remoteDockerNode.bindWorkspace();
      fail("Had to throw an exception");
    } catch (ServerException ignored) {
      // mocked to behave like this
    }

    remoteDockerNode.unbindWorkspace();

    verifyBackupIsNeverCalled();
  }

  @Test
  public void backupIsNotCalledWhenRestoreIsInProgress() throws Exception {
    doAnswer(
            inv -> {
              remoteDockerNode.unbindWorkspace();
              verifyBackupIsNeverCalled();
              return null;
            })
        .when(backupManager)
        .restoreWorkspaceBackup(anyString(), anyString(), anyString());

    remoteDockerNode.bindWorkspace();
  }

  private void verifyBackupIsNeverCalled() throws Exception {
    verify(backupManager, never()).backupWorkspaceAndCleanup(anyString(), anyString(), anyString());
  }
}
