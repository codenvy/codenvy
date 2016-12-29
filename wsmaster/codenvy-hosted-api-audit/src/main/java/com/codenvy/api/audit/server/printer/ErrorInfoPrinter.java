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

import org.eclipse.che.api.core.ServerException;

import java.nio.file.Path;

/**
 * Prints error in format:
 * [ERROR] <error text>!
 *
 * @author Dmytro Nochevnov
 */
public class ErrorInfoPrinter extends Printer {

    private String error;

    public ErrorInfoPrinter(Path auditReport, String error) {
        super(auditReport);

        this.error = error;
    }

    @Override
    public void print() throws ServerException {
        printError(error);
    }

}
