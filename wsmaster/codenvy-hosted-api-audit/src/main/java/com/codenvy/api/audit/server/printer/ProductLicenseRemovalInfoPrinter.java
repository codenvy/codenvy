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

import com.codenvy.api.license.shared.model.SystemLicenseAction;
import org.eclipse.che.api.core.ServerException;

import java.nio.file.Path;

import static java.lang.String.format;

/**
 * Prints info about product license removal action into the audit report.
 *
 * @author Dmytro Nochevnov
 * @author Igor Vinokur
 */
public class ProductLicenseRemovalInfoPrinter extends Printer {

    private SystemLicenseAction licenseAction;

    public ProductLicenseRemovalInfoPrinter(Path auditReport, SystemLicenseAction licenseAction) {
        super(auditReport);

        this.licenseAction = licenseAction;
    }

    @Override
    public void print() throws ServerException {
        String acceptanceTime = timestampToString(licenseAction.getActionTimestamp());

        printRow(format("%s: Paid license %s removed. System returned to previously accepted Fair Source license.\n",
                        acceptanceTime, licenseAction.getLicenseId()));
    }
}
