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
package com.codenvy.workspace;

import com.codenvy.machine.backup.WorkspaceIdHashLocationFinder;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceFilesCleanUpScriptExecutorTest {

    private static final String TEXT              = "Catch the moment.";
    private static final int    CLEAN_UP_DURATION = 1;

    @Mock
    private File                          file;
    @Mock
    private Workspace                     workspace;
    @Mock
    private WorkspaceIdHashLocationFinder workspaceIdHashLocationFinder;

    private WorkspaceFilesCleanUpScriptExecutor workspaceFilesCleaner;

    @BeforeMethod
    public void cleanUp() throws Exception {
        workspaceFilesCleaner = spy(new WorkspaceFilesCleanUpScriptExecutor(workspaceIdHashLocationFinder,
                                                                            file,
                                                                            TEXT,
                                                                            CLEAN_UP_DURATION));
        doReturn(file).when(workspaceIdHashLocationFinder).calculateDirPath(file, TEXT);
        when(workspace.getId()).thenReturn(TEXT);
        when(file.getAbsolutePath()).thenReturn(TEXT);
        doNothing().when(workspaceFilesCleaner).execute(new String[] {TEXT, TEXT}, CLEAN_UP_DURATION);
    }

    @Test
    public void workspaceScriptForCleanUpWorkspaceStorageShouldBeLaunched() throws Exception {
        workspaceFilesCleaner.clear(workspace);

        verify(workspaceIdHashLocationFinder).calculateDirPath(file, TEXT);
        verify(file).getAbsolutePath();
        verify(workspaceFilesCleaner).execute(new String[] {TEXT, TEXT}, CLEAN_UP_DURATION);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionInCaseWorkspaceFileCleanerThrowTimeOutException() throws Exception {
        doThrow(TimeoutException.class).when(workspaceFilesCleaner).execute(new String[] {TEXT, TEXT}, CLEAN_UP_DURATION);

        workspaceFilesCleaner.clear(workspace);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionInCaseWorkspaceFileCleanerThrowInterruptedException() throws Exception {
        doThrow(InterruptedException.class).when(workspaceFilesCleaner).execute(new String[] {TEXT, TEXT}, CLEAN_UP_DURATION);

        workspaceFilesCleaner.clear(workspace);
    }
}
