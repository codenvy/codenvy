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
package com.codenvy.im.cli.command;

import com.codenvy.cli.command.builtin.MultiRemoteCodenvy;
import com.codenvy.cli.command.builtin.Remote;
import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.preferences.PreferencesAPI;
import com.codenvy.client.CodenvyClient;
import com.codenvy.client.dummy.DummyCodenvyClient;
import com.codenvy.im.facade.IMArtifactLabeledFacade;
import com.google.common.io.Files;
import org.apache.felix.service.command.CommandSession;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/** @author Dmytro Nochevnov */
public class TestAbstractIMCommand {
    private TestedAbstractIMCommand spyCommand;
    private Preferences             globalPreferences;

    @Mock
    private IMArtifactLabeledFacade service;
    @Mock
    private CommandSession          session;
    @Mock
    private MultiRemoteCodenvy      mockMultiRemoteCodenvy;
    @Mock
    private Remote mockSaasServerRemote;
    @Mock
    private Remote mockAnotherRemote;

    private final static String CODENVY_ONPREM_SERVER_URL  = "https://codenvy.onprem";
    private final static String TEST_TOKEN                 = "authToken";

    private static final String ANOTHER_REMOTE_NAME = "another remote";
    private static final String ANOTHER_REMOTE_URL  = "another remote url";

    private String DEFAULT_PREFERENCES_FILE                        = "default-preferences.json";
    private String PREFERENCES_WITH_SAAS_SERVER_FILE               = "preferences-with-codenvy-onprem.json";
    private String PREFERENCES_WITH_SAAS_SERVER_WITHOUT_LOGIN_FILE = "preferences-with-codenvy-onprem-without-login.json";

    private Remote saasServerRemote;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);

        spyCommand = spy(new TestedAbstractIMCommand());
        doReturn(true).when(spyCommand).isInteractive();
        doNothing().when(spyCommand).initConsole();

        saasServerRemote = new Remote();
        saasServerRemote.setUrl(CODENVY_ONPREM_SERVER_URL);
    }

    @Test
    public void testInit() {
        globalPreferences = loadPreferences(PREFERENCES_WITH_SAAS_SERVER_FILE);
        prepareTestAbstractIMCommand(spyCommand);
        spyCommand.init();

        assertNotNull(spyCommand.getCodenvyOnpremPreferences());
    }

    private void prepareTestAbstractIMCommand(TestedAbstractIMCommand command) {
        doReturn(globalPreferences).when(session).get(Preferences.class.getName());

        DummyCodenvyClient codenvyClient = new DummyCodenvyClient();
        doReturn(codenvyClient).when(session).get(DummyCodenvyClient.class.getName());
        command.setCodenvyClient(codenvyClient);
        command.setSession(session);
    }

    private Preferences loadPreferences(String preferencesFileRelativePath) {
        String preferencesFileFullPath = getClass().getClassLoader().getResource(preferencesFileRelativePath).getPath();
        String tempPreferencesFileFullPath = preferencesFileFullPath + ".temp";
        File preferencesFile = new File(preferencesFileFullPath);
        File tempPreferencesFile = new File(tempPreferencesFileFullPath);

        try {
            Files.copy(preferencesFile, tempPreferencesFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return PreferencesAPI.getPreferences(tempPreferencesFile.toURI());
    }

    class TestedAbstractIMCommand extends AbstractIMCommand {
        @Override
        protected Void execute() throws Exception {
            return null;
        }

        @Override
        protected void doExecuteCommand() throws Exception {
        }

        /** is needed for prepareTestAbstractIMCommand() method */
        @Override
        protected void setCodenvyClient(CodenvyClient codenvyClient) {
            super.setCodenvyClient(codenvyClient);
        }

        /** is needed for prepareTestAbstractIMCommand() method */
        protected void setSession(CommandSession session) {
            this.session = session;
        }

        protected MultiRemoteCodenvy getMultiRemoteCodenvy() {
            return super.getMultiRemoteCodenvy();
        }
    }
}
