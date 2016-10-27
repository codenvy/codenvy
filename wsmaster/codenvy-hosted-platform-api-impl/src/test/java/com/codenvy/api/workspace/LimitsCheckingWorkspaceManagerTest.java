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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.environment.server.EnvironmentParser;
import org.eclipse.che.api.environment.server.compose.ComposeFileParser;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
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
 * @author Igor Vinokur
 */
@Listeners(MockitoTestNGListener.class)
public class LimitsCheckingWorkspaceManagerTest {
    @Mock
    SystemRamInfoProvider systemRamInfoProvider;

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "You are only allowed to create 2 workspaces.")
    public void shouldNotBeAbleToCreateNewWorkspaceIfLimitIsExceeded() throws Exception {
        final LimitsCheckingWorkspaceManager manager = managerBuilder().build();
        doReturn(ImmutableList.of(mock(WorkspaceImpl.class), mock(WorkspaceImpl.class))) // <- currently used 2
                                                                                         .when(manager)
                                                                                         .getByNamespace(anyString());

        manager.checkCountAndPropagateCreation("user123", null);
    }

    @Test
    public void shouldNotCheckAllowedWorkspacesPerUserWhenItIsSetToMinusOne() throws Exception {
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setWorkspacesPerUser(-1).build();
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
        final LimitsCheckingWorkspaceManager manager = managerBuilder().build();
        doReturn(emptyList()).when(manager).getByNamespace(anyString()); // <- currently used 0

        final WorkspaceCallback callback = mock(WorkspaceCallback.class);
        manager.checkCountAndPropagateCreation("user123", callback);

        verify(callback).call();
    }

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "Low RAM. Your workspace cannot be started until the system has more RAM available.")
    public void shouldNotBeAbleToStartNewWorkspaceIfSystemRamLimitIsExceeded() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(parseSize("2.95 GiB"), parseSize("3 GiB")));
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setSystemRamInfoProvider(systemRamInfoProvider).build();
        doReturn(emptyList()).when(manager).getByNamespace(anyString());

        manager.checkLimitsAndPropagateStart("NameSpace", null);
    }

    @Test
    public void shouldCallStartCallbackIfEverythingIsOkayWithSystemRamLimits() throws Exception {
        final WorkspaceCallback callback = mock(WorkspaceCallback.class);
        final LimitsCheckingWorkspaceManager manager = managerBuilder().build();
        doReturn(singletonList(createRuntime("1gb", "1gb"))).when(manager).getByNamespace(anyString());

        manager.checkLimitsAndPropagateStart("NameSpace", callback);

        verify(callback).call();
    }

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "The maximum RAM per workspace is set to '2048mb' and you requested '3072mb'. " +
                                            "This value is set by your admin with the 'limits.workspace.env.ram' property")
    public void shouldNotBeAbleToCreateWorkspaceWhichExceedsRamLimit() throws Exception {
        final WorkspaceConfig config = createConfig("3gb");
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setMaxRamPerEnv("2gb").build();

        manager.checkMaxEnvironmentRam(config);
    }

    @Test
    public void shouldNotCheckWorkspaceRamLimitIfItIsSetToMinusOne() throws Exception {
        final WorkspaceConfig config = createConfig("3gb");
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setMaxRamPerEnv("-1").build();

        manager.checkMaxEnvironmentRam(config);
    }

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "The maximum RAM per workspace is set to '2048mb' and you requested '2304mb'. " +
                                            "This value is set by your admin with the 'limits.workspace.env.ram' property")
    public void shouldNotBeAbleToCreateWorkspaceWithMultipleMachinesWhichExceedsRamLimit() throws Exception {
        final WorkspaceConfig config = createConfig("1gb", "1gb", "256mb");

        final LimitsCheckingWorkspaceManager manager = managerBuilder().setMaxRamPerEnv("2gb").build();

        manager.checkMaxEnvironmentRam(config);
    }

    @Test
    public void shouldBeAbleToCreateWorkspaceWithMultipleMachinesWhichDoesNotExceedRamLimit() throws Exception {
        final WorkspaceConfig config = createConfig("1gb", "1gb", "256mb");

        final LimitsCheckingWorkspaceManager manager = managerBuilder().setMaxRamPerEnv("3gb").build();

        manager.checkMaxEnvironmentRam(config);
    }

    @Test
    public void shouldBeAbleToCreateWorkspaceWithMultipleMachinesIncludingMachineWithoutLimitsWhichDoesNotExceedRamLimit()
            throws Exception {
        final WorkspaceConfig config = createConfig("256mb", "256mb", null);

        final LimitsCheckingWorkspaceManager manager = managerBuilder().setMaxRamPerEnv("3gb").build();

        manager.checkMaxEnvironmentRam(config);
    }

    @Test
    public void shouldAcquireAndReleaseSemaphoreIfThroughputPropertyIsMoreThanZero() throws Exception {
        final LimitsCheckingWorkspaceManager manager = managerBuilder().build();
        Semaphore semaphore = mock(Semaphore.class);
        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        manager.startSemaphore = semaphore;
        doReturn(singletonList(createRuntime("256mb", "256mb", null))).when(manager)
                                                                      .getByNamespace(anyString());

        manager.checkLimitsAndPropagateLimitedThroughputStart("NameSpace", callback);

        verify(semaphore).acquire();
        verify(semaphore).release();
    }

    @Test(expectedExceptions = Exception.class)
    public void shouldAcquireAndReleaseSemaphoreIfThroughputPropertyIsMoreThanZeroAndExceptionHappened() throws Exception {
        final LimitsCheckingWorkspaceManager manager = managerBuilder().build();
        Semaphore semaphore = mock(Semaphore.class);
        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        manager.startSemaphore = semaphore;
        doThrow(new Exception()).when(manager).checkLimitsAndPropagateStart(anyString(), anyObject());

        manager.checkLimitsAndPropagateLimitedThroughputStart(anyString(), callback);

        verify(semaphore).acquire();
        verify(semaphore).release();
    }

    @Test
    public void shouldSetSemaphoreToNullIfThroughputPropertyIsZero() throws Exception {
        final LimitsCheckingWorkspaceManager manager = managerBuilder().build();
        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        doReturn(singletonList(createRuntime("256mb", "256mb", null))).when(manager).getByNamespace(anyString());

        manager.checkLimitsAndPropagateLimitedThroughputStart("NameSpace", callback);

        assertNull(manager.startSemaphore);
    }

    @Test
    public void shouldSetSemaphoreToNullIfThroughputPropertyIsLessThenZero() throws Exception {
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setMaxSameTimeStartWSRequests(-1).build();
        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        doReturn(singletonList(createRuntime("256mb", "256mb", null))).when(manager)
                                                                      .getByNamespace(anyString());

        manager.checkLimitsAndPropagateLimitedThroughputStart("NameSpace", callback);

        assertNull(manager.startSemaphore);
    }

    @Test(timeOut = 3000)
    public void shouldPermitToCheckRamOnlyForFiveThreadsAtTheSameTime() throws Exception {
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setMaxSameTimeStartWSRequests(5).build();
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
        }).when(manager).checkLimitsAndPropagateStart(anyString(), anyObject());
        Runnable runnable = () -> {
            try {
                final WorkspaceCallback callback = mock(WorkspaceCallback.class);
                manager.checkLimitsAndPropagateLimitedThroughputStart("NameSpace", callback);
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
        verify(manager, timeout(300).times(5)).checkLimitsAndPropagateStart(anyString(), anyObject());

        //Execute paused threads to release the throughput limit for other threads.
        invokeProcessLatch.countDown();
        //Wait for throughput limit will be released and check that RAM check was performed in other threads.
        verify(manager, timeout(300).times(7)).checkLimitsAndPropagateStart(anyString(), anyObject());
    }

    @Test
    public void shouldCallCreateCallBackIfStartedWorkspacesNumberLimitIsNotExceeded() throws Exception {
        //Set started workspaces limit by number of all workspaces statuses.
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setStartedWorkspacesLimit(WorkspaceStatus.values().length).build();
        final WorkspaceCallback callback = mock(WorkspaceCallback.class);
        //Currently started workspaces with all statuses.
        //Workspace with status 'STOPPED' will not be counted in the check.
        List<WorkspaceImpl> workspaces = new ArrayList<>();
        for (WorkspaceStatus status : WorkspaceStatus.values()) {
            WorkspaceImpl workspace = mock(WorkspaceImpl.class);
            when(workspace.getStatus()).thenReturn(status);
            workspaces.add(workspace);
        }
        doReturn(workspaces).when(manager).getByNamespace(anyString());

        manager.checkLimitsAndPropagateStart("user123", callback);

        verify(callback).call();
    }

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "You are only allowed to start \\d workspaces.")
    public void shouldNotBeAbleToStartWorkspaceIfStartedWorkspacesNumberLimitIsExceeded() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));
        //Set started workspaces limit by number of all workspaces statuses minus one to reach the limit.
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setStartedWorkspacesLimit(WorkspaceStatus.values().length - 1)
                                                                       .build();
        //Currently started workspaces with all statuses.
        //Workspace with status 'STOPPED' will not be counted in the check.
        List<WorkspaceImpl> workspaces = new ArrayList<>();
        for (WorkspaceStatus status : WorkspaceStatus.values()) {
            WorkspaceImpl workspace = mock(WorkspaceImpl.class);
            when(workspace.getStatus()).thenReturn(status);
            workspaces.add(workspace);
        }
        doReturn(workspaces).when(manager).getByNamespace(anyString());

        manager.checkLimitsAndPropagateStart("user123", null);
    }

    @Test
    public void shouldNotCheckStartedWorkspacesNumberWhenItIsSetToMinusOne() throws Exception {
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setStartedWorkspacesLimit(-1).build();
        doReturn(ImmutableList.of(mock(WorkspaceImpl.class), mock(WorkspaceImpl.class))).when(manager).getByNamespace(anyString());
        final WorkspaceCallback callback = mock(WorkspaceCallback.class);

        manager.checkLimitsAndPropagateStart("user123", callback);

        verify(callback).call();
        verify(manager, never()).getByNamespace(any());
    }

    private static ManagerBuilder managerBuilder() throws ServerException {
        return new ManagerBuilder();
    }

    private static class ManagerBuilder {

        private SystemRamInfoProvider systemRamInfoProvider;
        private EnvironmentParser     environmentParser;
        private String                maxRamPerEnv;
        private boolean               defaultAutoSnapshot;
        private boolean               defaultAutoRestore;
        private int                   workspacesPerUser;
        private int                   startedWorkspacesLimit;
        private int                   maxSameTimeStartWSRequests;
        private int                   defaultMachineMemorySizeMB;

        ManagerBuilder() throws ServerException {
            workspacesPerUser = 2;
            startedWorkspacesLimit = 2;
            maxRamPerEnv = "1gb";
            maxSameTimeStartWSRequests = 0;
            defaultAutoSnapshot = false;
            defaultAutoRestore = false;
            defaultMachineMemorySizeMB = 2000;

            systemRamInfoProvider = mock(SystemRamInfoProvider.class);
            when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));

            environmentParser = new EnvironmentParser(new ComposeFileParser(), mock(RecipeDownloader.class));
        }

        public LimitsCheckingWorkspaceManager build() {
            return spy(new LimitsCheckingWorkspaceManager(workspacesPerUser,
                                                          startedWorkspacesLimit,
                                                          maxRamPerEnv,
                                                          maxSameTimeStartWSRequests,
                                                          systemRamInfoProvider,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          environmentParser,
                                                          defaultAutoSnapshot,
                                                          defaultAutoRestore,
                                                          defaultMachineMemorySizeMB));
        }

        ManagerBuilder setWorkspacesPerUser(int workspacesPerUser) {
            this.workspacesPerUser = workspacesPerUser;
            return this;
        }

        ManagerBuilder setStartedWorkspacesLimit(int startedWorkspacesLimit) {
            this.startedWorkspacesLimit = startedWorkspacesLimit;
            return this;
        }

        ManagerBuilder setMaxRamPerEnv(String maxRamPerEnv) {
            this.maxRamPerEnv = maxRamPerEnv;
            return this;
        }

        ManagerBuilder setMaxSameTimeStartWSRequests(int maxSameTimeStartWSRequests) {
            this.maxSameTimeStartWSRequests = maxSameTimeStartWSRequests;
            return this;
        }

        ManagerBuilder setSystemRamInfoProvider(SystemRamInfoProvider systemRamInfoProvider) {
            this.systemRamInfoProvider = systemRamInfoProvider;
            return this;
        }
    }
}
