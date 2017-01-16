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
import org.osgi.framework.Bundle;

/**
 * Exit of the current CLI
 *
 * @author Florent Benoit
 */
@Command(scope = "codenvy", name = "exit", description = "Exit of the shell")
public class ExitCommand extends AbsCommand {

    /**
     * Execute the command
     */
    @Override
    protected Object execute() throws Exception {
        init();

        // stop system bundle
        try {
            Bundle bundle = getBundleContext().getBundle(0);
            bundle.stop();
        } catch (Exception e) {
            log.error("Error when shutting down Apache Karaf", e);
        }
        return null;
    }
}

