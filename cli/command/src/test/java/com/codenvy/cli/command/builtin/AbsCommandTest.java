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
package com.codenvy.cli.command.builtin;

import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.security.RemoteCredentials;
import com.codenvy.client.dummy.DummyCodenvyClient;
import com.codenvy.client.dummy.project.DummyProject;

import org.apache.felix.service.command.CommandSession;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;

import java.util.HashMap;
import java.util.Map;

import static com.codenvy.cli.command.builtin.util.SHA1.sha1;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

/**
 * Super class of tests.
 *
 * @author Florent Benoit
 */
public abstract class AbsCommandTest {




    @BeforeClass
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        // Disable the direct console access on Windows as we are intercepting System.in
        System.setProperty("jline.WindowsTerminal.directConsole", "false");

    }


    /**
     * Prepare the given command by injecting default configuration
     *
     * @param command
     */
    protected CommandSession prepare(AbsCommand command, DummyCodenvyClient codenvyClient) {
        CommandSession commandSession = Mockito.mock(CommandSession.class);
        Preferences globalPreferences = Mockito.mock(Preferences.class);
        Preferences remotesPreferences = Mockito.mock(Preferences.class);
        Map<String, String> preferencesMap = new HashMap<>();
        preferencesMap.put("default", "default");
        doReturn(preferencesMap).when(globalPreferences).get("remotes", Map.class);
        doReturn(remotesPreferences).when(globalPreferences).path("remotes");
        Remote defaultRemote = Mockito.mock(Remote.class);
        doReturn(defaultRemote).when(remotesPreferences).get("default", Remote.class);

        RemoteCredentials defaultRemoteCredentials = Mockito.mock(RemoteCredentials.class);
        doReturn("defaultToken").when(defaultRemoteCredentials).getToken();
        doReturn(defaultRemoteCredentials).when(remotesPreferences).get(eq("default"), eq(RemoteCredentials.class));
        doReturn("http://default").when(defaultRemote).getUrl();
        doReturn(true).when(defaultRemote).isDefaultRemote();

        doReturn("username").when(defaultRemoteCredentials).getUsername();

        MultiRemoteCodenvy multiRemoteCodenvy = new MultiRemoteCodenvy(codenvyClient, globalPreferences, commandSession);

        command.setCodenvyClient(codenvyClient);
        doReturn(multiRemoteCodenvy).when(commandSession).get(MultiRemoteCodenvy.class.getName());
        doReturn(globalPreferences).when(commandSession).get(Preferences.class.getName());
        doReturn(codenvyClient).when(commandSession).get(DummyCodenvyClient.class.getName());

        return commandSession;
    }


    protected String getProjectId(DummyProject dummyProject) {
        // compute short id
        String fullID = dummyProject.workspaceId() + dummyProject.name();

        // p is for project
        return sha1("p", fullID).substring(0, 7);
    }

}
