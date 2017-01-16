/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.im.commands;

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.utils.Version;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author Anatoliy Bazko
 */
public class TestWaitOnAliveArtifactOfCorrectVersionCommand {

    public static final Version EXPECTED_VERSION = Version.valueOf("3.3.0");
    public static final Version WRONG_VERSION = Version.valueOf("3.1.0");

    private Artifact                                   artifact;
    private WaitOnAliveArtifactOfCorrectVersionCommand command;

    @BeforeTest
    public void setUp() throws Exception {
        artifact = mock(CDECArtifact.class);
        command = spy(new WaitOnAliveArtifactOfCorrectVersionCommand(artifact, EXPECTED_VERSION));
    }

    @Test
    public void testExecute() throws Exception {
        doReturn(false)
            .doReturn(true)
            .when(artifact).isAlive();

        doReturn(Optional.empty())
            .doReturn(Optional.of(WRONG_VERSION))
            .doReturn(Optional.of(EXPECTED_VERSION))
            .when(artifact).getInstalledVersion();
        command.execute();

        verify(artifact, times(4)).isAlive();
        verify(artifact, times(3)).getInstalledVersion();
    }

    @Test
    public void testGetDescription() throws Exception {
        assertNotNull(command.getDescription());
    }

    @Test
    public void testToString() throws Exception {
        assertNotNull(command.toString());
    }
}
