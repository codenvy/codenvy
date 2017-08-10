/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.api.audit.server.printer;

import org.eclipse.che.api.core.ServerException;

import java.nio.file.Path;

import static java.lang.String.format;

/**
 * Prints system info into audit report.
 *
 * @author Dmytro Nochevnov
 * @author Igor Vinokur
 */
public class SystemInfoPrinter extends Printer {

    private long allUsersNumber;

    public SystemInfoPrinter(Path auditReport, long allUsersNumber) {
        super(auditReport);
        this.allUsersNumber = allUsersNumber;
    }

    @Override
    public void print() throws ServerException {
        printRow(format("Number of users: %s\n", allUsersNumber));
    }
}
