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
package com.codenvy.api.workspace;

import static org.eclipse.che.commons.lang.Size.parseSize;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;

import com.codenvy.api.workspace.SystemRamCheckingWorkspaceManager.WorkspaceCallback;
import com.codenvy.service.system.SystemRamInfo;
import com.codenvy.service.system.SystemRamInfoProvider;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.multiuser.resource.api.usage.ResourceUsageManager;
import org.eclipse.che.multiuser.resource.api.usage.tracker.EnvironmentRamCalculator;
import org.eclipse.che.multiuser.resource.api.workspace.LimitExceededException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link SystemRamCheckingWorkspaceManager}.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 * @author Igor Vinokur
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class SystemRamCheckingWorkspaceManagerTest {

  public static final String NAMESPACE = "namespace";
  public static final String ACCOUNT_ID = "accountId";
  @Mock private WorkspaceDao workspaceDao;
  @Mock private SystemRamInfoProvider systemRamInfoProvider;
  @Mock private EnvironmentRamCalculator environmentRamCalculator;
  @Mock private Account account;
  @Mock private ResourceUsageManager resourceUsageManager;

  @Test(
    expectedExceptions = LimitExceededException.class,
    expectedExceptionsMessageRegExp =
        "Low RAM. Your workspace cannot be started until the system has more RAM available."
  )
  public void shouldNotBeAbleToStartNewWorkspaceIfSystemRamLimitIsExceeded() throws Exception {
    when(systemRamInfoProvider.getSystemRamInfo())
        .thenReturn(new SystemRamInfo(parseSize("2.95 GiB"), parseSize("3 GiB")));
    final SystemRamCheckingWorkspaceManager manager =
        managerBuilder().setSystemRamInfoProvider(systemRamInfoProvider).build();

    manager.checkSystemRamLimitAndPropagateStart(null);
  }

  @Test
  public void shouldCallStartCallbackIfEverythingIsOkayWithSystemRamLimits() throws Exception {
    final WorkspaceCallback callback = mock(WorkspaceCallback.class);
    final SystemRamCheckingWorkspaceManager manager = managerBuilder().build();

    manager.checkSystemRamLimitAndPropagateStart(callback);

    verify(callback).call();
  }

  @Test
  public void shouldAcquireAndReleaseSemaphoreIfThroughputPropertyIsMoreThanZero()
      throws Exception {
    final SystemRamCheckingWorkspaceManager manager = managerBuilder().build();
    Semaphore semaphore = mock(Semaphore.class);
    WorkspaceCallback callback = mock(WorkspaceCallback.class);
    manager.startSemaphore = semaphore;

    manager.checkSystemRamLimitAndPropagateLimitedThroughputStart(callback);

    verify(semaphore).acquire();
    verify(semaphore).release();
  }

  @Test(expectedExceptions = Exception.class)
  public void
      shouldAcquireAndReleaseSemaphoreIfThroughputPropertyIsMoreThanZeroAndExceptionHappened()
          throws Exception {
    final SystemRamCheckingWorkspaceManager manager = managerBuilder().build();
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
    final SystemRamCheckingWorkspaceManager manager =
        managerBuilder().setMaxSameTimeStartWSRequests(0).build();
    WorkspaceCallback callback = mock(WorkspaceCallback.class);

    manager.checkSystemRamLimitAndPropagateLimitedThroughputStart(callback);

    assertNull(manager.startSemaphore);
  }

  @Test
  public void shouldSetSemaphoreToNullIfThroughputPropertyIsLessThenZero() throws Exception {
    final SystemRamCheckingWorkspaceManager manager =
        managerBuilder().setMaxSameTimeStartWSRequests(-1).build();
    WorkspaceCallback callback = mock(WorkspaceCallback.class);

    manager.checkSystemRamLimitAndPropagateLimitedThroughputStart(callback);

    assertNull(manager.startSemaphore);
  }

  @Test(timeOut = 3000)
  public void shouldPermitToCheckRamOnlyForFiveThreadsAtTheSameTime() throws Exception {
    final SystemRamCheckingWorkspaceManager manager =
        managerBuilder().setMaxSameTimeStartWSRequests(5).build();
    /*
     The count-down latch is needed to reach the throughput limit by acquiring RAM check permits.
     The lath is configured to 6 invocations: 5 (number of allowed same time requests) + 1 for main thread
     to be able to release the throughput limit.
    */
    final CountDownLatch invokeProcessLatch = new CountDownLatch(6);
    // Pause 5 threads after they will acquire all permits to check RAM.
    doAnswer(
            invocationOnMock -> {
              invokeProcessLatch.countDown();
              invokeProcessLatch.await();
              return null;
            })
        .when(manager)
        .checkSystemRamLimitAndPropagateStart(anyObject());
    Runnable runnable =
        () -> {
          try {
            final WorkspaceCallback callback = mock(WorkspaceCallback.class);
            manager.checkSystemRamLimitAndPropagateLimitedThroughputStart(callback);
          } catch (Exception ignored) {
          }
        };
    // Run 7 threads (more than number of allowed same time requests) that want to request RAM check
    // at the same time.
    ExecutorService executor = Executors.newFixedThreadPool(7);
    executor.submit(runnable);
    executor.submit(runnable);
    executor.submit(runnable);
    executor.submit(runnable);
    executor.submit(runnable);
    executor.submit(runnable);
    executor.submit(runnable);

    // Wait for throughput limit will be reached and check that RAM check was performed only in
    // allowed number of threads.
    verify(manager, timeout(300).times(5)).checkSystemRamLimitAndPropagateStart(anyObject());

    // Execute paused threads to release the throughput limit for other threads.
    invokeProcessLatch.countDown();
    // Wait for throughput limit will be released and check that RAM check was performed in other
    // threads.
    verify(manager, timeout(300).times(7)).checkSystemRamLimitAndPropagateStart(anyObject());
  }

  private static ManagerBuilder managerBuilder() throws ServerException {
    return new ManagerBuilder();
  }

  private static class ManagerBuilder {

    private int maxSameTimeStartWSRequests;
    private SystemRamInfoProvider systemRamInfoProvider;

    ManagerBuilder() throws ServerException {
      maxSameTimeStartWSRequests = 0;

      systemRamInfoProvider = mock(SystemRamInfoProvider.class);
      when(systemRamInfoProvider.getSystemRamInfo())
          .thenReturn(new SystemRamInfo(0, parseSize("3 GiB")));
    }

    public SystemRamCheckingWorkspaceManager build() {
      return spy(
          new SystemRamCheckingWorkspaceManager(
              null,
              null,
              null,
              null,
              false,
              false,
              null,
              null,
              "10gb",
              maxSameTimeStartWSRequests,
              systemRamInfoProvider,
              null,
              null,
              null));
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
