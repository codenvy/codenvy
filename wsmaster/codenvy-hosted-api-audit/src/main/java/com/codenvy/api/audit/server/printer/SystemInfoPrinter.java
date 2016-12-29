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
