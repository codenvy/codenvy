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
import com.codenvy.service.systemram.SystemRamInfo;
import com.codenvy.service.systemram.SystemRamInfoProvider;
import com.google.common.collect.ImmutableList;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.environment.server.EnvironmentParser;
import org.eclipse.che.api.environment.server.compose.ComposeFileParser;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static com.codenvy.api.workspace.TestObjects.createConfig;
import static com.codenvy.api.workspace.TestObjects.createRuntime;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.commons.lang.Size.parseSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;

/**
 * Tests for {@link LimitsCheckingWorkspaceManager}.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class LimitsCheckingWorkspaceManagerTest {
    @Mock
    SnapshotDao           snapshotDao;
    @Mock
    SystemRamInfoProvider systemRamInfoProvider;

    @Mock
    RecipeDownloader recipeDownloader;

    @Mock
    AccountManager accountManager;

    ComposeFileParser composeFileParser = new ComposeFileParser();

    EnvironmentParser environmentParser = new EnvironmentParser(composeFileParser, recipeDownloader);

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "The maximum workspaces allowed per user is set to '2' and you are currently at that limit. " +
                                            "This value is set by your admin with the 'limits.user.workspaces.count' property")
    public void shouldNotBeAbleToCreateNewWorkspaceIfLimitIsExceeded() throws Exception {
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2, // <- workspaces max count
                                                                                              "2gb",
                                                                                              "1gb",
                                                                                              0,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        doReturn(ImmutableList.of(mock(WorkspaceImpl.class), mock(WorkspaceImpl.class))) // <- currently used 2
                .when(manager)
                .getByNamespace(anyString());

        manager.checkCountAndPropagateCreation("user123", null);
    }

    @Test
    public void shouldNotCheckAllowedWorkspacesPerUserWhenItIsSetToMinusOne() throws Exception {
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(-1, // <- workspaces max count
                                                                                              "2gb",
                                                                                              "1gb",
                                                                                              0,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        doReturn(ImmutableList.of(mock(WorkspaceImpl.class), mock(WorkspaceImpl.class))) // <- currently used 2
                                                                                         .when(manager)
                                                                                         .getByNamespace(anyString());
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
                                                                                              0,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        doReturn(emptyList()).when(manager).getByNamespace(anyString()); // <- currently used 0

        final WorkspaceCallback callback = mock(WorkspaceCallback.class);
        manager.checkCountAndPropagateCreation("user123", callback);

        verify(callback).call();
    }

    @Test(expectedExceptions = LimitExceededException.class,
            expectedExceptionsMessageRegExp = "There are 1 running workspaces consuming 2GB RAM. Your current RAM " +
                                              "limit is 2GB. This workspaces requires an additional 1GB. You can stop other workspaces to free resources.")
    public void shouldNotBeAbleToStartNewWorkspaceIfUserRamLimitIsExceeded() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "2gb", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              0,
                                                                                              systemRamInfoProvider,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        doReturn(singletonList(createRuntime("1gb", "1gb"))).when(manager).getByNamespace(anyString()); // <- currently running 2gb

        manager.checkRamAndPropagateStart(createConfig("1gb"), null, "user123", null);
    }

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "Low RAM. Your workspace cannot be started until the system has more RAM available.")
    public void shouldNotBeAbleToStartNewWorkspaceIfSystemRamLimitIsExceeded() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(parseSize("2.95 GiB"), parseSize("3 GiB")));
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "2gb", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              0,
                                                                                              systemRamInfoProvider,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        doReturn(emptyList()).when(manager).getByNamespace(anyString());

        manager.checkRamAndPropagateStart(createConfig("1gb"), null, "user123", null);
    }

    @Test
    public void shouldSkipWorkspacesRamCheckIfItIsSetToMinusOne() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "-1", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              0,
                                                                                              systemRamInfoProvider,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        doReturn(singletonList(createRuntime("1gb", "1gb"))).when(manager).getByNamespace(anyString()); // <- currently running 2gb
        final WorkspaceCallback callback = mock(WorkspaceCallback.class);

        manager.checkRamAndPropagateStart(createConfig("1gb"), null, "user123", callback);

        verify(callback).call();
        verify(manager, never()).getWorkspaces(any());
    }


    @Test
    public void shouldCallStartCallbackIfEverythingIsOkayWithLimits() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              0,
                                                                                              systemRamInfoProvider,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        doReturn(singletonList(createRuntime("1gb", "1gb"))).when(manager).getByNamespace(anyString()); // <- currently running 2gb

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
                                                                                              0,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));

        manager.checkMaxEnvironmentRam(config);
    }

    @Test
    public void shouldNotCheckWorkspaceRamLimitIfItIsSetToMinusOne() throws Exception {
        final WorkspaceConfig config = createConfig("3gb");
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb",
                                                                                              "-1", // <- workspaces env ram limit
                                                                                              0,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));

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
                                                                                              0,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        manager.checkMaxEnvironmentRam(config);
    }

    @Test
    public void shouldBeAbleToCreateWorkspaceWithMultipleMachinesWhichDoesNotExceedRamLimit() throws Exception {
        final WorkspaceConfig config = createConfig("1gb", "1gb", "256mb");

        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb",
                                                                                              "3gb", // <- workspaces env ram limit
                                                                                              0,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        manager.checkMaxEnvironmentRam(config);
    }

    @Test
    public void shouldCheckRamLimitOfCreatorUserInsteadOfCurrent() throws Exception {
        final AccountManager accountManager = mock(AccountManager.class);
        final WorkspaceImpl ws = createRuntime("1gb", "1gb");
        final UserImpl user = new UserImpl("id", "email", ws.getNamespace());
        user.setName(ws.getNamespace());
        doReturn(user.getAccount()).when(accountManager).getByName(eq(ws.getNamespace()));

        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "2gb", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              0,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              snapshotDao,
                                                                                              accountManager,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));

        doReturn(ws).when(manager).getWorkspace(anyString()); // <- currently running 2gb
        doReturn(ws).when(manager).checkRamAndPropagateStart(anyObject(), anyString(), anyString(), anyObject());

        manager.startWorkspace(ws.getId(), "envName", true);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(manager).checkRamAndPropagateStart(anyObject(), anyString(), argument.capture(), anyObject());
        verify((WorkspaceManager)manager).startWorkspace(ws.getId(), "envName", true);
        Assert.assertEquals(argument.getValue(), ws.getNamespace());
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Unable to start workspace .*, because its namespace owner is " +
                                            "unavailable and it is impossible to check resources consumption.")
    public void shouldPreventStartIfCreatorNotExistsAnymore() throws Exception {
        final AccountManager accountManager = mock(AccountManager.class);
        final WorkspaceImpl ws = createRuntime("1gb", "1gb");
        doThrow(new NotFoundException("Nope")).when(accountManager).getByName(eq(ws.getNamespace()));


        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "2gb", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              0,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              snapshotDao,
                                                                                              accountManager,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        doReturn(ws).when(manager).getWorkspace(anyString()); // <- currently running 2gb
        manager.startWorkspace(ws.getId(), null, false);
    }

    @Test
    public void shouldBeAbleToCreateWorkspaceWithMultipleMachinesIncludingMachineWithoutLimitsWhichDoesNotExceedRamLimit() throws Exception {
        final WorkspaceConfig config = createConfig("256mb", "256mb", null);

        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb",
                                                                                              "3gb", // <- workspaces env ram limit
                                                                                              0,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000)); // <- default limit for machines without set limit
        manager.checkMaxEnvironmentRam(config);
    }

    @Test
    public void shouldBeAbleToStartWorkspaceWithMultipleMachinesIncludingMachineWithoutLimitsWhichDoesNotExceedRamLimit() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));
        LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                        "3gb",
                                                                                        "1gb", // <- workspaces ram limit
                                                                                        0,
                                                                                        systemRamInfoProvider,
                                                                                        null,
                                                                                        null,
                                                                                        null,
                                                                                        null,
                                                                                        null,
                                                                                        environmentParser,
                                                                                        false,
                                                                                        false,
                                                                                        2000));
        doReturn(singletonList(createRuntime("256mb", "256mb", null))).when(manager)
                                                                      .getByNamespace(anyString()); // <- currently running 2gb

        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        manager.checkRamAndPropagateStart(createConfig("256mb", "256mb", null), null, "user123", callback);

        verify(callback).call();
    }

    @Test
    public void shouldAcquireAndReleaseSemaphoreIfThroughputPropertyIsMoreThanZero() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              5,
                                                                                              systemRamInfoProvider,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        Semaphore semaphore = mock(Semaphore.class);
        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        manager.startSemaphore = semaphore;
        doReturn(singletonList(createRuntime("256mb", "256mb", null))).when(manager)
                                                                      .getByNamespace(anyString());

        manager.checkRamAndPropagateLimitedThroughputStart(createConfig("256mb", "256mb", null), null, "user123", callback);

        verify(semaphore).acquire();
        verify(semaphore).release();
    }

    @Test(expectedExceptions = Exception.class)
    public void shouldAcquireAndReleaseSemaphoreIfThroughputPropertyIsMoreThanZeroAndExceptionHappened() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              5,
                                                                                              systemRamInfoProvider,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        Semaphore semaphore = mock(Semaphore.class);
        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        manager.startSemaphore = semaphore;
        doThrow(new Exception()).when(manager).checkRamAndPropagateStart(anyObject(), anyString(), anyString(), anyObject());

        manager.checkRamAndPropagateLimitedThroughputStart(createConfig("256mb", "256mb", null), null, "user123", callback);

        verify(semaphore).acquire();
        verify(semaphore).release();
    }

    @Test
    public void shouldSetSemaphoreToNullIfThroughputPropertyIsZero() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              0,
                                                                                              systemRamInfoProvider,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        doReturn(singletonList(createRuntime("256mb", "256mb", null))).when(manager).getByNamespace(anyString());

        manager.checkRamAndPropagateLimitedThroughputStart(createConfig("256mb", "256mb", null), null, "user123", callback);

        assertNull(manager.startSemaphore);
    }

    @Test
    public void shouldSetSemaphoreToNullIfThroughputPropertyIsLessThenZero() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              -1,
                                                                                              systemRamInfoProvider,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        doReturn(singletonList(createRuntime("256mb", "256mb", null))).when(manager)
                                                                      .getByNamespace(anyString());

        manager.checkRamAndPropagateLimitedThroughputStart(createConfig("256mb", "256mb", null), null, "user123", callback);

        assertNull(manager.startSemaphore);
    }

    @Test(timeOut = 3000)
    public void shouldPermitToCheckRamOnlyForFiveThreadsAtTheSameTime() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));
        final LimitsCheckingWorkspaceManager manager = spy(new LimitsCheckingWorkspaceManager(2,
                                                                                              "3gb", // <- workspaces ram limit
                                                                                              "1gb",
                                                                                              5,
                                                                                              systemRamInfoProvider,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              null,
                                                                                              environmentParser,
                                                                                              false,
                                                                                              false,
                                                                                              2000));
        doReturn(singletonList(createRuntime("1gb", "1gb"))).when(manager).getByNamespace(anyString()); // <- currently running 2gb
        /*
          The count-down latch is needed to reach the throughput limit by acquiring RAM check permits.
          The lath is configured to 6 invocations: 5 (number of allowed same time requests) + 1 for main thread
          to be able to release the throughput limit.
         */
        final CountDownLatch invokeProcessLatch = new CountDownLatch(6);
        //Pause 5 threads after they will acquire all permits to check RAM.
        doAnswer(invocationOnMock -> {
            invokeProcessLatch.countDown();
            invokeProcessLatch.await();
            return null;
        }).when(manager).checkRamAndPropagateStart(anyObject(), anyString(), anyString(), anyObject());
        Runnable runnable = () -> {
            try {
                final WorkspaceCallback callback = mock(WorkspaceCallback.class);
                manager.checkRamAndPropagateLimitedThroughputStart(createConfig("1gb"), null, "user123", callback);
            } catch (Exception e) {
            }
        };
        //Run 7 threads (more than number of allowed same time requests) that want to request RAM check at the same time.
        ExecutorService executor = Executors.newFixedThreadPool(7);
        executor.submit(runnable);
        executor.submit(runnable);
        executor.submit(runnable);
        executor.submit(runnable);
        executor.submit(runnable);
        executor.submit(runnable);
        executor.submit(runnable);

        //Wait for throughput limit will be reached and check that RAM check was performed only in allowed number of threads.
        verify(manager, timeout(300).times(5)).checkRamAndPropagateStart(anyObject(), anyString(), anyString(), anyObject());

        //Execute paused threads to release the throughput limit for other threads.
        invokeProcessLatch.countDown();
        //Wait for throughput limit will be released and check that RAM check was performed in other threads.
        verify(manager, timeout(300).times(7)).checkRamAndPropagateStart(anyObject(), anyString(), anyString(), anyObject());
    }
}
