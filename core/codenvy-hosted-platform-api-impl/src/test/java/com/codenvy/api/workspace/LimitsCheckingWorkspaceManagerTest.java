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
package com.codenvy.api.workspace;

import com.codenvy.api.workspace.LimitsCheckingWorkspaceManager.WorkspaceCallback;
import com.google.common.collect.ImmutableList;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.codenvy.api.workspace.TestObjects.createConfig;
import static com.codenvy.api.workspace.TestObjects.createRuntime;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link LimitsCheckingWorkspaceManager}.
 *
 * @author Yevhenii Voevodin
 */
public class LimitsCheckingWorkspaceManagerTest {

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "The maximum workspaces allowed per user is set to '2' and you are currently at that limit. " +
                                            "This value is set by your admin with the 'limits.user.workspaces.count' property")
    public void shouldNotBeAbleToCreateNewWorkspaceIfLimitIsExceeded() throws Exception {
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2, // <- workspaces max count
                                                                                              "2gb",
                                                                                              "1gb",
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));
        doReturn(ImmutableList.of(mock(WorkspaceImpl.class), mock(WorkspaceImpl.class))) // <- currently used 2
                                                                                         .when(manager)
                                                                                         .getWorkspaces(anyString());

        manager.checkCountAndPropagateCreation("user123", null);
    }

    @Test
    public void shouldNotCheckAllowedWorkspacesPerUserWhenItIsSetToMinusOne() throws Exception {
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(-1, // <- workspaces max count
                                                                                              "2gb",
                                                                                              "1gb",
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));
        doReturn(ImmutableList.of(mock(WorkspaceImpl.class), mock(WorkspaceImpl.class))) // <- currently used 2
                                                                                         .when(manager)
                                                                                         .getWorkspaces(anyString());
        final WorkspaceCallback callback = mock(WorkspaceCallback.class);

        manager.checkCountAndPropagateCreation("user123", callback);

        verify(callback).call();
        verify(manager, never()).getWorkspaces(any());
    }


    @Test
    public void shouldCallCreateCallBackIfEverythingIsOkayWithLimits() throws Exception {
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2, // <- workspaces max count
                                                                                              "2gb",
                                                                                              "1gb",
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));
        doReturn(emptyList()).when(manager).getWorkspaces(anyString()); // <- currently used 0

        final WorkspaceCallback callback = mock(WorkspaceCallback.class);
        manager.checkCountAndPropagateCreation("user123", callback);

        verify(callback).call();
    }



    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp ="Unable to start this workspace. There are '2' " +
                                           "running workspaces consuming '4096mb' RAM. " +
                                           "Your current RAM limit is '4096mb'. " +
                                           "You can stop other workspaces to free resources.")
    public void shouldNotBeAbleToStartNewWorkspaceIfRamLimitIsExceeded() throws Exception {
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "4gb", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));
        List<Workspace> workspaces = new ArrayList<>();
        workspaces.add(createRuntime("1gb", "1gb"));
        workspaces.add(createRuntime("1gb", "1gb"));
        doReturn(workspaces).when(manager).getWorkspaces(anyString()); // <- currently running 2gb

        manager.checkRamAndPropagateStart(createConfig("1gb"), null, "user123", null);
    }



    @Test
    public void shouldSkipWorkspacesRamCheckIfItIsSetToMinusOne() throws Exception {
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "-1", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));
        doReturn(singletonList(createRuntime("1gb", "1gb"))).when(manager).getWorkspaces(anyString()); // <- currently running 2gb
        final WorkspaceCallback callback = mock(WorkspaceCallback.class);

        manager.checkRamAndPropagateStart(createConfig("1gb"), null, "user123", callback);

        verify(callback).call();
        verify(manager, never()).getWorkspaces(any());
    }


    @Test
    public void shouldCallStartCallbackIfEverythingIsOkayWithLimits() throws Exception {
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));
        doReturn(singletonList(createRuntime("1gb", "1gb"))).when(manager).getWorkspaces(anyString()); // <- currently running 2gb

        final WorkspaceCallback callback = mock(WorkspaceCallback.class);
        manager.checkRamAndPropagateStart(createConfig("1gb"), null, "user123", callback);

        verify(callback).call();
    }

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "The maximum RAM per workspace is set to '2048mb' and you requested '3072mb'. " +
                                            "This value is set by your admin with the 'limits.workspace.env.ram' property")
    public void shouldNotBeAbleToCreateWorkspaceWhichExceedsRamLimit() throws Exception {
        final WorkspaceConfig config = createConfig("3gb");
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb",
                                                                                              "2gb", // <- workspaces env ram limit
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));

        manager.checkMaxEnvironmentRam(config);
    }

    @Test
    public void shouldNotCheckWorkspaceRamLimitIfItIsSetToMinusOne() throws Exception {
        final WorkspaceConfig config = createConfig("3gb");
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb",
                                                                                              "-1", // <- workspaces env ram limit
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));

        manager.checkMaxEnvironmentRam(config);
    }

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "The maximum RAM per workspace is set to '2048mb' and you requested '2304mb'. " +
                                            "This value is set by your admin with the 'limits.workspace.env.ram' property")
    public void shouldNotBeAbleToCreateWorkspaceWithMultipleMachinesWhichExceedsRamLimit() throws Exception {
        final WorkspaceConfig config = createConfig("1gb", "1gb", "256mb");

        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb",
                                                                                              "2gb", // <- workspaces env ram limit
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));
        manager.checkMaxEnvironmentRam(config);
    }

    @Test
    public void shouldBeAbleToCreateWorkspaceWithMultipleMachinesWhichDoesNotExceedRamLimit() throws Exception {
        final WorkspaceConfig config = createConfig("1gb", "1gb", "256mb");

        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb",
                                                                                              "3gb", // <- workspaces env ram limit
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));
        manager.checkMaxEnvironmentRam(config);
    }
}
