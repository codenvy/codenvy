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
package com.codenvy.im.cli.command;

import com.codenvy.cli.command.builtin.MultiRemoteCodenvy;
import com.codenvy.im.cli.preferences.CodenvyOnpremPreferences;
import com.codenvy.im.console.Console;
import com.codenvy.im.facade.IMArtifactLabeledFacade;
import com.codenvy.im.managers.ConfigManager;
import org.apache.felix.service.command.CommandSession;
import org.fusesource.jansi.AnsiOutputStream;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
public abstract class AbstractTestCommand {

    public final static String CODENVY_ONPREM_URL           = "http://codenvy.onprem";
    public final static String CODENVY_ONPREM_AUTH_TOKEN    = "authToken";
    public static final String CODENVY_ONPREM_USER_PASSWORD = "testUserPassword";
    public static final String CODENVY_ONPREM_USER          = "test@codenvy.onprem";
    public static final String SYSTEM_USER_NAME             = System.getProperty("user.name");

    public CommandInvoker commandInvoker;
    public Console        spyConsole;

    @Mock
    public IMArtifactLabeledFacade  mockFacade;
    @Mock
    public CodenvyOnpremPreferences mockPreferences;
    @Mock
    public ConfigManager            mockConfigManager;
    @Mock
    public MultiRemoteCodenvy       mockMultiRemoteCodenvy;
    @Mock
    public CommandSession           mockCommandSession;


    protected void performBaseMocks(AbstractIMCommand spyCommand, boolean interactive) throws IOException {
        MockitoAnnotations.initMocks(this);

        doNothing().when(spyCommand).init();
        doReturn(interactive).when(spyCommand).isInteractive();

        spyConsole = spy(new ConsoleTested(interactive));
        doNothing().when(spyConsole)
                   .exit(anyInt());  // avoid error "The forked VM terminated without properly saying goodbye. VM crash or System.exit called?"

        doReturn(spyConsole).when(spyCommand).getConsole();

        doReturn(mockFacade).when(spyCommand).getFacade();

        doReturn(CODENVY_ONPREM_URL).when(mockPreferences).getUrl();
        doReturn(CODENVY_ONPREM_AUTH_TOKEN).when(mockPreferences).getAuthToken();
        doReturn(mockPreferences).when(spyCommand).getCodenvyOnpremPreferences();

        doReturn(mockConfigManager).when(spyCommand).getConfigManager();

        doReturn(mockMultiRemoteCodenvy).when(spyCommand).getMultiRemoteCodenvy();

        commandInvoker = new CommandInvoker(spyCommand, mockCommandSession);
    }

    String removeAnsi(final String content) {
        if (content == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            AnsiOutputStream aos = new AnsiOutputStream(baos);
            aos.write(content.getBytes());
            aos.flush();
            return baos.toString();
        } catch (IOException e) {
            return content;
        }
    }

    static class ConsoleTested extends Console {

        private ConsoleTested(boolean interactive) throws IOException {
            super(interactive);
        }

        @Override
        public void printProgress(String message) {
            // disable progressor
        }

    }
}
