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

import com.codenvy.client.dummy.builder.DummyBuilderClient;
import com.codenvy.client.dummy.builder.DummyBuilderStatus;
import com.codenvy.client.model.BuilderState;
import com.codenvy.client.model.BuilderStatus;
import com.codenvy.client.model.ProjectReference;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.codenvy.client.model.BuilderState.CANCELLED;
import static com.codenvy.client.model.BuilderState.IN_PROGRESS;
import static com.codenvy.client.model.BuilderState.IN_QUEUE;
import static com.codenvy.client.model.BuilderState.SUCCESSFUL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Florent Benoit
 */
public class BuilderTest {

    private ProjectReference   projectReference1;
    private ProjectReference   projectReference2;
    private DummyBuilderClient builderClient;

    @BeforeClass
    public void init() {
        DummyCodenvyClient codenvyClient = new DummyCodenvyClient();
        DummyCodenvy codenvy = codenvyClient.newCodenvyBuilder("url1", "florent").build();
        this.builderClient = codenvy.builder();

        // add a project
        this.projectReference1 = codenvyClient.newProjectBuilder().withName("project1").build();
        this.projectReference2 = codenvyClient.newProjectBuilder().withName("project2").build();

        // needs to create available status
        DummyBuilderStatus build1 = codenvyClient.newDummyBuilderStatus(1L).withLog("Hello").build();


        // register this build
        builderClient.registerBuilderStatus(projectReference1, build1);


    }

    @Test(groups = "project1")
    public void testLaunchBuild() {
        // no current processes
        List<BuilderStatus> currentBuilds = builderClient.builds(projectReference1).execute();
        assertNotNull(currentBuilds);
        assertTrue(currentBuilds.isEmpty());

        // start a process
        BuilderStatus status = builderClient.build(projectReference1).execute();

        // Check result
        assertNotNull(status);
        assertEquals(status.taskId(), 1L);

    }

    @Test(groups = "project1", dependsOnMethods = "testLaunchBuild")
    public void testCheckGlobalBuilds() {
        List<BuilderStatus> builds = builderClient.builds(projectReference1).execute();
        boolean found = false;
        for (BuilderStatus builderStatus : builds) {
            if (1L == builderStatus.taskId()) {
                found = true;
            }
        }
        assertTrue(found);

    }

    @Test(groups = "project1", dependsOnMethods = "testCheckGlobalBuilds")
    public void testCheckLogsBuilds() {
        String log = builderClient.logs(projectReference1, 1L).execute();
        assertEquals(log, "Hello");
    }


    @Test(groups = "project1", dependsOnMethods = "testCheckLogsBuilds")
    public void testCheckCancelBuild() {

        // get build
        BuilderStatus builderStatus = builderClient.status(projectReference1, 1L).execute();

        // get State (should be stopped or running
        BuilderState state = builderStatus.status();
        assertTrue(state == SUCCESSFUL || state == IN_PROGRESS || state == IN_PROGRESS || state == IN_QUEUE);

        // call cancel
        BuilderStatus newStatus = builderClient.cancel(projectReference1, 1L).execute();
        assertTrue(newStatus.status() == CANCELLED);

    }


}
