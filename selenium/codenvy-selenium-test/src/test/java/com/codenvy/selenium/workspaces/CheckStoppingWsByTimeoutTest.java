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
package com.codenvy.selenium.workspaces;

import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.InjectTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckStoppingWsByTimeoutTest {
  private static final String USER_NAME = "user-for-check-ws";

  @InjectTestUser(value = USER_NAME)
  private TestUser testUser;

  @InjectTestWorkspace(user = USER_NAME)
  private TestWorkspace testWorkspace;

  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private ToastLoader toastLoader;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test
  public void checkStoppingWsWithApi() throws Exception {
    projectExplorer.waitProjectExplorer();
    WaitUtils.sleepQuietly(760, TimeUnit.SECONDS);
    Workspace workspace =
        workspaceServiceClient.getByName(
            testWorkspace.getName(), USER_NAME, testUser.getAuthToken());
    assertEquals(workspace.getStatus(), WorkspaceStatus.STOPPED);
    toastLoader.waitStartButtonInToastLoader();
  }
}
