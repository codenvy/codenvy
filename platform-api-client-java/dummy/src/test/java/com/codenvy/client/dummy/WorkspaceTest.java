/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client.dummy;

import com.codenvy.client.WorkspaceClient;
import com.codenvy.client.model.Workspace;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Florent Benoit
 */
public class WorkspaceTest {

    private Workspace        workspace1;

    private WorkspaceClient workspaceClient;

    @BeforeClass
    public void init() {
        DummyCodenvyClient codenvyClient = new DummyCodenvyClient();
        DummyCodenvy codenvy = codenvyClient.newCodenvyBuilder("url1", "florent").build();
        this.workspaceClient = codenvy.workspace();

        // Create a workspace
        this.workspace1 = codenvyClient.newWorkspaceBuilder("workspace1").build();


    }

    @Test
    public void testNoWorkspaces() {
        // check we don't have any workspaces
        List<Workspace> workspaces = workspaceClient.all().execute();
        assertNotNull(workspaces);
        assertTrue(workspaces.isEmpty());
    }

    @Test(dependsOnMethods = "testNoWorkspaces")
    public void createWorkspace() {

        // Create it
        Workspace builtWorkspace = workspaceClient.create(workspace1).execute();
        assertNotNull(builtWorkspace);
        assertEquals(builtWorkspace.name(), workspace1.name());
    }

    @Test(dependsOnMethods = "createWorkspace")
    public void testGetAllWorkspaces() {
        // check we have it now
        List<Workspace> workspaces = workspaceClient.all().execute();
        assertNotNull(workspaces);
        assertEquals(workspaces.size(), 1);
        assertEquals(workspaces.get(0).name(), workspace1.name());

    }

    @Test(dependsOnMethods = "testGetAllWorkspaces")
    public void testGetWorkspaceByName() {
        // check we have it now
        Workspace found = workspaceClient.withName(workspace1.name()).execute();
        assertNotNull(found);
        assertEquals(found.name(), workspace1.name());
    }


}
