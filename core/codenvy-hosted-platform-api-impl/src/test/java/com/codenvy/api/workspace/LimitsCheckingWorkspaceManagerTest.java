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

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.workspace.server.model.impl.UsersWorkspaceImpl;
import org.testng.annotations.Test;

import static com.codenvy.api.workspace.TestObjects.createConfig;
import static com.codenvy.api.workspace.TestObjects.createRuntime;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link LimitsCheckingWorkspaceManager}.
 *
 * @author Yevhenii Voevodin
 */
public class LimitsCheckingWorkspaceManagerTest {

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "The maximum workspaces allowed per user is set to '2' and you are currently at that limit. " +
                                            "This value is set by your admin with the 'limits.user.workspaces.count' property")
    public void shouldNotBeAbleToCreateNewWorkspaceIfLimitIsExceeded() throws Exception {
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2, // <- workspaces max count
                                                                                              "2gb",
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));
        doReturn(ImmutableList.of(mock(UsersWorkspaceImpl.class), mock(UsersWorkspaceImpl.class))) // <- currently used 2
                .when(manager)
                .getWorkspaces(anyString());

        manager.checkCountAndPropagateCreation("user123", null);
    }

    @Test
    public void shouldCallCreateCallBackIfEverythingIsOkayWithLimits() throws Exception {
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2, // <- workspaces max count
                                                                                              "2gb",
                                                                                              null,
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

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "This workspace cannot be started as it would exceed the maximum available RAM allocated to you." +
                                            " Users are each currently allocated '2048mb' RAM across their active workspaces. " +
                                            "This value is set by your admin with the 'limits.user.workspaces.ram' property")
    public void shouldNotBeAbleToStartNewWorkspaceIfRamLimitIsExceeded() throws Exception {
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "2gb", // <- workspaces ram limit
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));
        doReturn(singletonList(createRuntime("1gb", "1gb"))).when(manager).getRuntimeWorkspaces(anyString()); // <- currently running 2gb

        manager.checkRamAndPropagateStart(createConfig("1gb"), null, "user123", null);
    }

    @Test
    public void shouldCallStartCallbackIfEverythingIsOkayWithLimits() throws Exception {
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb", // <- workspaces ram limit
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null));
        doReturn(singletonList(createRuntime("1gb", "1gb"))).when(manager).getRuntimeWorkspaces(anyString()); // <- currently running 2gb

        final WorkspaceCallback callback =  mock(WorkspaceCallback.class);
        manager.checkRamAndPropagateStart(createConfig("1gb"), null, "user123", callback);

        verify(callback).call();
    }
}
