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
package com.codenvy.machine;

import com.codenvy.machine.backup.MachineBackupManager;

import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.eclipse.che.plugin.docker.client.MessageProcessor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Igor Vinokur
 */
@Listeners(MockitoTestNGListener.class)
public class RemoteDockerNodeTest {

    @Mock
    private MachineBackupManager        backupManager;
    @Mock
    private DockerConnector             dockerConnector;
    @Mock
    private WorkspaceFolderPathProvider pathProvider;
    @Mock
    private Exec                        exec;

    private RemoteDockerNode remoteDockerNode;

    @BeforeMethod
    public void setUp() throws Exception {
        when(pathProvider.getPath("WorkspaceId")).thenReturn("WorkspacePath");
        when(dockerConnector.createExec(any())).thenReturn(exec);
        when(exec.getId()).thenReturn("ExecId");
        remoteDockerNode = new RemoteDockerNode(dockerConnector, "ContainerId", "WorkspaceId", backupManager, pathProvider);
    }

    @Test
    public void ShouldRestoreWorkspaceBackup() throws Exception {
        //given
        LogMessage message = mock(LogMessage.class);
        when(message.getType()).thenReturn(LogMessage.Type.STDOUT);
        when(message.getContent()).thenReturn("MessageContent");
        doAnswer(invocation -> {
            MessageProcessor<LogMessage> messageProcessor = (MessageProcessor<LogMessage>)invocation.getArguments()[1];
            messageProcessor.process(message);
            messageProcessor.process(message);
            return null;
        }).when(dockerConnector).startExec(any(StartExecParams.class), any(MessageProcessor.class));

        //when
        remoteDockerNode.bindWorkspace();

        //then
        verify(backupManager).restoreWorkspaceBackup(eq("WorkspaceId"),
                                                     eq("MessageContent"),
                                                     eq("MessageContent"),
                                                     eq("127.0.0.1"),
                                                     anyString());
    }

    @Test(expectedExceptions = MachineException.class,
          expectedExceptionsMessageRegExp = "Can't detect container user ids to chown backed up files of workspace WorkspaceId")
    public void ShouldThrowExceptionIfOnlyOneExecMessageReceived() throws Exception {
        //given
        LogMessage message = mock(LogMessage.class);
        when(message.getType()).thenReturn(LogMessage.Type.STDOUT);
        when(message.getContent()).thenReturn("MessageContent");
        doAnswer(invocation -> {
            MessageProcessor<LogMessage> messageProcessor = (MessageProcessor<LogMessage>)invocation.getArguments()[1];
            messageProcessor.process(message);
            return null;
        }).when(dockerConnector).startExec(any(StartExecParams.class), any(MessageProcessor.class));

        //when
        remoteDockerNode.bindWorkspace();
    }

    @Test(expectedExceptions = MachineException.class,
          expectedExceptionsMessageRegExp = "Can't detect container user ids to chown backed up files of workspace WorkspaceId")
    public void ShouldThrowExceptionIfNoExecMessagesReceived() throws Exception {
        //given
        doAnswer(invocation -> null).when(dockerConnector).startExec(any(StartExecParams.class), any(MessageProcessor.class));

        //when
        remoteDockerNode.bindWorkspace();
    }
}
