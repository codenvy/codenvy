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

import com.codenvy.api.license.SystemLicense;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.annotation.Nullable;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static java.lang.String.format;

/**
 * Prints system info into audit report.
 *
 * @author Dmytro Nochevnov
 * @author Igor Vinokur
 */
public class SystemInfoPrinter extends Printer {

    private long allUsersNumber;
    private SystemLicense license;

    public SystemInfoPrinter(Path auditReport, long allUsersNumber, @Nullable SystemLicense license) {
        super(auditReport);

        this.allUsersNumber = allUsersNumber;
        this.license = license;
    }

    @Override
    public void print() throws ServerException {
        printRow(format("Number of users: %s\n", allUsersNumber));
        if (license != null) {
            printRow(format("Number of licensed seats: %s\n", license.getNumberOfUsers()));
            printRow(format("License expiration: %s\n",
                            new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(license.getExpirationDateFeatureValue())));
        } else {
            printError("Failed to retrieve license");
        }
    }
}
