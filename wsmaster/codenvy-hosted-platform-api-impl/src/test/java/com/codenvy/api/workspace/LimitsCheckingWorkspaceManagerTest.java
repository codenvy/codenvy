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
import com.codenvy.resource.api.RamResourceType;
import com.codenvy.resource.api.RuntimeResourceType;
import com.codenvy.resource.api.WorkspaceResourceType;
import com.codenvy.resource.api.exception.NoEnoughResourcesException;
import com.codenvy.resource.api.usage.ResourceUsageManager;
import com.codenvy.resource.spi.impl.ResourceImpl;
import com.codenvy.service.systemram.SystemRamInfo;
import com.codenvy.service.systemram.SystemRamInfoProvider;

import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
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
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class LimitsCheckingWorkspaceManagerTest {
    public static final String NAMESPACE  = "namespace";
    public static final String ACCOUNT_ID = "accountId";
    @Mock
    private WorkspaceDao             workspaceDao;
    @Mock
    private SystemRamInfoProvider    systemRamInfoProvider;
    @Mock
    private EnvironmentRamCalculator environmentRamCalculator;
    @Mock
    private Account                  account;
    @Mock
    private ResourceUsageManager     resourceUsageManager;

    @Test
    public void shouldUseRamOfSpecifiedEnvironmentOnCheckingAvailabilityOfRamResource() throws Exception {
        //given
        LimitsCheckingWorkspaceManager manager = managerBuilder().setResourceUsageManager(resourceUsageManager)
                                                                 .setEnvironmentRamCalculator(environmentRamCalculator)
                                                                 .build();

        when(environmentRamCalculator.calculate(any())).thenReturn(3000L);

        WorkspaceConfig config = createConfig("3gb");
        String envToStart = config.getDefaultEnv();

        //when
        manager.checkRamResourcesAvailability(ACCOUNT_ID, NAMESPACE, config, envToStart);

        //then
        verify(environmentRamCalculator).calculate(config.getEnvironments().get(envToStart));
        verify(resourceUsageManager).checkResourcesAvailability(ACCOUNT_ID, singletonList(new ResourceImpl(RamResourceType.ID,
                                                                                                           3000,
                                                                                                           RamResourceType.UNIT)));
    }

    @Test
    public void shouldUseRamOfDefaultEnvironmentOnCheckingAvailabilityOfRamResourceWhen() throws Exception {
        //given
        LimitsCheckingWorkspaceManager manager = managerBuilder().setResourceUsageManager(resourceUsageManager)
                                                                 .setEnvironmentRamCalculator(environmentRamCalculator)
                                                                 .build();

        when(environmentRamCalculator.calculate(any())).thenReturn(3000L);

        WorkspaceConfig config = createConfig("3gb");

        //when
        manager.checkRamResourcesAvailability(ACCOUNT_ID, NAMESPACE, config, null);

        //then
        verify(environmentRamCalculator).calculate(config.getEnvironments().get(config.getDefaultEnv()));
        verify(resourceUsageManager).checkResourcesAvailability(ACCOUNT_ID, singletonList(new ResourceImpl(RamResourceType.ID,
                                                                                                           3000,
                                                                                                           RamResourceType.UNIT)));
    }

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "Workspace namespace/workspace.. needs 3000MB to start. Your account has 200MB and 100MB " +
                                            "in use\\. The workspace can't be start\\. Stop other workspaces or grant more resources\\.")
    public void shouldThrowLimitExceedExceptionIfAccountDoesNotHaveEnoughAvailableRamResource() throws Exception {
        doThrow(new NoEnoughResourcesException(singletonList(new ResourceImpl(RamResourceType.ID,
                                                                              200L,
                                                                              RamResourceType.UNIT)),
                                               singletonList(new ResourceImpl(RamResourceType.ID,
                                                                              3000L,
                                                                              RamResourceType.UNIT)),
                                               emptyList()))
                .when(resourceUsageManager).checkResourcesAvailability(any(), any());
        doReturn(singletonList(new ResourceImpl(RamResourceType.ID,
                                                100L,
                                                RamResourceType.UNIT)))
                .when(resourceUsageManager).getUsedResources(any());

        //given
        LimitsCheckingWorkspaceManager manager = managerBuilder().setResourceUsageManager(resourceUsageManager)
                                                                 .setEnvironmentRamCalculator(environmentRamCalculator)
                                                                 .build();

        when(environmentRamCalculator.calculate(any())).thenReturn(3000L);

        WorkspaceConfig config = createConfig("3gb");

        //when
        manager.checkRamResourcesAvailability(ACCOUNT_ID, NAMESPACE, config, null);
    }

    @Test
    public void shouldNotThrowLimitExceedExceptionIfAccountHasEnoughAvailableWorkspaceResource() throws Exception {
        //given
        LimitsCheckingWorkspaceManager manager = managerBuilder().setResourceUsageManager(resourceUsageManager)
                                                                 .build();

        //when
        manager.checkWorkspaceResourceAvailability(ACCOUNT_ID);

        //then
        verify(resourceUsageManager).checkResourcesAvailability(ACCOUNT_ID, singletonList(new ResourceImpl(WorkspaceResourceType.ID,
                                                                                                           1,
                                                                                                           WorkspaceResourceType.UNIT)));
    }

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "You are only allowed to create 5 workspaces.")
    public void shouldThrowLimitExceedExceptionIfAccountDoesNotHaveEnoughAvailableWorkspaceResource() throws Exception {
        //given
        doThrow(new NoEnoughResourcesException(singletonList(new ResourceImpl(WorkspaceResourceType.ID,
                                                                              5,
                                                                              WorkspaceResourceType.UNIT)),
                                               emptyList(),
                                               emptyList()))
                .when(resourceUsageManager).checkResourcesAvailability(any(), any());
        LimitsCheckingWorkspaceManager manager = managerBuilder().setResourceUsageManager(resourceUsageManager)
                                                                 .build();

        //when
        manager.checkWorkspaceResourceAvailability(ACCOUNT_ID);
    }

    @Test
    public void shouldNotThrowLimitExceedExceptionIfAccountHasEnoughAvailableRuntimeResource() throws Exception {
        //given
        LimitsCheckingWorkspaceManager manager = managerBuilder().setResourceUsageManager(resourceUsageManager)
                                                                 .build();

        //when
        manager.checkRuntimeResourceAvailability(ACCOUNT_ID);

        //then
        verify(resourceUsageManager).checkResourcesAvailability(ACCOUNT_ID, singletonList(new ResourceImpl(RuntimeResourceType.ID,
                                                                                                           1,
                                                                                                           RuntimeResourceType.UNIT)));
    }

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "You are only allowed to start 5 workspaces.")
    public void shouldThrowLimitExceedExceptionIfAccountDoesNotHaveEnoughAvailableRuntimeResource() throws Exception {
        //given
        doThrow(new NoEnoughResourcesException(singletonList(new ResourceImpl(RuntimeResourceType.ID,
                                                                              5,
                                                                              RuntimeResourceType.UNIT)),
                                               emptyList(),
                                               emptyList()))
                .when(resourceUsageManager).checkResourcesAvailability(any(), any());
        LimitsCheckingWorkspaceManager manager = managerBuilder().setResourceUsageManager(resourceUsageManager)
                                                                 .build();

        //when
        manager.checkRuntimeResourceAvailability(ACCOUNT_ID);
    }

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "Low RAM. Your workspace cannot be started until the system has more RAM available.")
    public void shouldNotBeAbleToStartNewWorkspaceIfSystemRamLimitIsExceeded() throws Exception {
        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(parseSize("2.95 GiB"), parseSize("3 GiB")));
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setSystemRamInfoProvider(systemRamInfoProvider).build();
        doReturn(emptyList()).when(manager).getByNamespace(anyString());

        manager.checkSystemRamLimitAndPropagateStart(null);
    }

    @Test
    public void shouldCallStartCallbackIfEverythingIsOkayWithSystemRamLimits() throws Exception {
        final WorkspaceCallback callback = mock(WorkspaceCallback.class);
        final LimitsCheckingWorkspaceManager manager = managerBuilder().build();
        doReturn(singletonList(createRuntime("1gb", "1gb"))).when(manager).getByNamespace(anyString());

        manager.checkSystemRamLimitAndPropagateStart(callback);

        verify(callback).call();
    }

    @Test(expectedExceptions = LimitExceededException.class,
          expectedExceptionsMessageRegExp = "You are only allowed to use 2048 mb. RAM per workspace.")
    public void shouldNotBeAbleToCreateWorkspaceWhichExceedsRamLimit() throws Exception {
        when(environmentRamCalculator.calculate(any())).thenReturn(3072L);
        final WorkspaceConfig config = createConfig("3gb");
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setMaxRamPerEnv("2gb")
                                                                       .setEnvironmentRamCalculator(environmentRamCalculator)
                                                                       .build();

        manager.checkMaxEnvironmentRam(config);
    }

    @Test
    public void shouldNotCheckWorkspaceRamLimitIfItIsSetToMinusOne() throws Exception {
        final WorkspaceConfig config = createConfig("3gb");
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setMaxRamPerEnv("-1")
                                                                       .setEnvironmentRamCalculator(environmentRamCalculator)
                                                                       .build();

        manager.checkMaxEnvironmentRam(config);

        verify(environmentRamCalculator, never()).calculate(anyObject());
    }

    @Test
    public void shouldAcquireAndReleaseSemaphoreIfThroughputPropertyIsMoreThanZero() throws Exception {
        final LimitsCheckingWorkspaceManager manager = managerBuilder().build();
        Semaphore semaphore = mock(Semaphore.class);
        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        manager.startSemaphore = semaphore;
        doReturn(singletonList(createRuntime("256mb", "256mb", null))).when(manager)
                                                                      .getByNamespace(anyString());

        manager.checkSystemRamLimitAndPropagateLimitedThroughputStart(callback);

        verify(semaphore).acquire();
        verify(semaphore).release();
    }

    @Test(expectedExceptions = Exception.class)
    public void shouldAcquireAndReleaseSemaphoreIfThroughputPropertyIsMoreThanZeroAndExceptionHappened() throws Exception {
        final LimitsCheckingWorkspaceManager manager = managerBuilder().build();
        Semaphore semaphore = mock(Semaphore.class);
        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        manager.startSemaphore = semaphore;
        doThrow(new Exception()).when(manager).checkSystemRamLimitAndPropagateStart(anyObject());

        manager.checkSystemRamLimitAndPropagateLimitedThroughputStart(callback);

        verify(semaphore).acquire();
        verify(semaphore).release();
    }

    @Test
    public void shouldSetSemaphoreToNullIfThroughputPropertyIsZero() throws Exception {
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setMaxSameTimeStartWSRequests(0)
                                                                       .build();
        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        doReturn(singletonList(createRuntime("256mb", "256mb", null))).when(manager).getByNamespace(anyString());

        manager.checkSystemRamLimitAndPropagateLimitedThroughputStart(callback);

        assertNull(manager.startSemaphore);
    }

    @Test
    public void shouldSetSemaphoreToNullIfThroughputPropertyIsLessThenZero() throws Exception {
        final LimitsCheckingWorkspaceManager manager = managerBuilder().setMaxSameTimeStartWSRequests(-1).build();
        WorkspaceCallback callback = mock(WorkspaceCallback.class);
        doReturn(singletonList(createRuntime("256mb", "256mb", null))).when(manager)
                                                                      .getByNamespace(anyString());

        manager.checkSystemRamLimitAndPropagateLimitedThroughputStart(callback);

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
        }).when(manager).checkSystemRamLimitAndPropagateStart(anyObject());
        Runnable runnable = () -> {
            try {
                final WorkspaceCallback callback = mock(WorkspaceCallback.class);
                manager.checkSystemRamLimitAndPropagateLimitedThroughputStart(callback);
            } catch (Exception ignored) {
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
        verify(manager, timeout(300).times(5)).checkSystemRamLimitAndPropagateStart(anyObject());

        //Execute paused threads to release the throughput limit for other threads.
        invokeProcessLatch.countDown();
        //Wait for throughput limit will be released and check that RAM check was performed in other threads.
        verify(manager, timeout(300).times(7)).checkSystemRamLimitAndPropagateStart(anyObject());
    }

    private static ManagerBuilder managerBuilder() throws ServerException {
        return new ManagerBuilder();
    }

    private static class ManagerBuilder {

        private String                   maxRamPerEnv;
        private int                      maxSameTimeStartWSRequests;
        private SystemRamInfoProvider    systemRamInfoProvider;
        private EnvironmentRamCalculator environmentRamCalculator;
        private ResourceUsageManager     resourceUsageManager;

        ManagerBuilder() throws ServerException {
            maxRamPerEnv = "1gb";
            maxSameTimeStartWSRequests = 0;

            systemRamInfoProvider = mock(SystemRamInfoProvider.class);
            when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));
        }

        public LimitsCheckingWorkspaceManager build() {
            return spy(new LimitsCheckingWorkspaceManager(null,
                                                          null,
                                                          null,
                                                          null,
                                                          false,
                                                          false,
                                                          null,
                                                          null,
                                                          maxRamPerEnv,
                                                          maxSameTimeStartWSRequests,
                                                          systemRamInfoProvider,
                                                          environmentRamCalculator,
                                                          resourceUsageManager,
                                                          null));
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

        ManagerBuilder setEnvironmentRamCalculator(EnvironmentRamCalculator environmentRamCalculator) {
            this.environmentRamCalculator = environmentRamCalculator;
            return this;
        }

        ManagerBuilder setResourceUsageManager(ResourceUsageManager resourceUsageManager) {
            this.resourceUsageManager = resourceUsageManager;
            return this;
        }
    }
}
