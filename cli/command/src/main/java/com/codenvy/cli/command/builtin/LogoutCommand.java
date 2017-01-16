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

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import static java.lang.String.format;

/**
 * Allow to logout in the default remote or a given remote
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "logout", description = "Logout to a remote Codenvy cloud")
public class LogoutCommand extends AbsCommand {

    @Option(name = "--remote", description = "Name of the remote codenvy", required = false)
    private String remoteName;

    @Override
    protected Object execute() throws Exception {

        init();

        // Is there any available remote ?
        if (!checkifAvailableRemotes()) {
            return null;
        }

        if (getMultiRemoteCodenvy().logout(remoteName)) {
            if (remoteName == null) {
                System.out.println(format("Logout success on default remote '%s' [%s]", getMultiRemoteCodenvy().getDefaultRemoteName(),
                                          getMultiRemoteCodenvy().getDefaultRemote().getUrl()));
            } else {
                System.out.println(format("Logout success on remote '%s' [%s]", remoteName, getMultiRemoteCodenvy().getRemote(remoteName).getUrl()));
            }
        } else {
            System.out.println("Logout failed.");
        }

        return null;
    }
}
