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

import com.codenvy.api.license.shared.model.SystemLicenseAction;
import org.eclipse.che.api.core.ServerException;

import java.nio.file.Path;

import static java.lang.String.format;

/**
 * Prints info about product license addition action into the audit report.
 *
 * @author Dmytro Nochevnov
 * @author Igor Vinokur
 */
public class ProductLicenseAdditionInfoPrinter extends Printer {

    private SystemLicenseAction licenseAction;

    public ProductLicenseAdditionInfoPrinter(Path auditReport, SystemLicenseAction licenseAction) {
        super(auditReport);

        this.licenseAction = licenseAction;
    }

    @Override
    public void print() throws ServerException {
        String email = licenseAction.getAttributes().get("email");
        String acceptanceTime = timestampToString(licenseAction.getActionTimestamp());

        printRow(format("%s: %s added paid license %s.\n",
                        acceptanceTime, email, licenseAction.getLicenseId()));
    }
}
