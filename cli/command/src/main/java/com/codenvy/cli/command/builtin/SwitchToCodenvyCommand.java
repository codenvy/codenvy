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

/**
 * This command will use codenvy subshell and then default commands to codenvy namespace
 * @author Florent Benoit
 */
@Command(scope = "prompt", name = "default-codenvy-namespace", description = "Switch to Codenvy shell namepsace")
public class SwitchToCodenvyCommand extends AbsCommand {

    /**
     * Change to codenvy subshell
     */
    @Override
    protected Object execute() throws Exception {


        // Change default scope/subshell after login to be ready with codenvy commands
        session.put("SCOPE", "codenvy:*");
        session.put("SUBSHELL", "codenvy");

        // remove exit command
        session.execute("removecommand * exit");

        init();

        return null;
    }
}
