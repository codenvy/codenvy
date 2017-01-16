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

import com.codenvy.im.cli.preferences.CodenvyOnpremPreferences;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import static java.lang.String.format;

/**
 * Installation manager Login command.
 */
@Command(scope = "codenvy", name = "login", description = "Login to Codenvy")
public class LoginCommand extends AbstractIMCommand {

    @Argument(name = "username", description = "The username", required = false, multiValued = false, index = 0)
    private String username;

    @Argument(name = "password", description = "The user's password", required = false, multiValued = false, index = 1)
    private String password;

    @Option(name = "--url", description = "Codenvy on-prem URL", required = false)
    private String remoteUrl;

    @Override
    protected void doExecuteCommand() throws Exception {
        try {
            if (remoteUrl == null) {
                remoteUrl = getConfigManager().getHostUrl();
            }

            getCodenvyOnpremPreferences().setUrl(remoteUrl);

            if (username == null) {
                getConsole().print(format("Codenvy user name for '%s': ", remoteUrl));
                username = getConsole().readLine();
            }

            if (password == null) {
                getConsole().print(format("Password for %s: ", username));
                password = getConsole().readPassword();
            }

            if (!getMultiRemoteCodenvy().login(CodenvyOnpremPreferences.CODENVY_ONPREM_REMOTE_NAME, username, password)) {
                getConsole().printErrorAndExit(format("Login failed on '%s'.",
                                                      getCodenvyOnpremPreferences().getUrl()));
                return;
            }

            getConsole().printSuccess(format("Login success on '%s'.",
                                             getCodenvyOnpremPreferences().getUrl()));
        } catch (Exception e) {
            throw e;
        }
    }
}
