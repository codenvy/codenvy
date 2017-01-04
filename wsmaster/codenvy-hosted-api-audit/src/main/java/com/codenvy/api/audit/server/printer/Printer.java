/*
 *  [2012] - [2016] Codenvy, S.A.
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
import com.codenvy.api.license.shared.model.Constants;
import com.codenvy.api.license.shared.model.SystemLicenseAction;
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.io.Files.append;
import static java.lang.String.format;

/**
 * Printer into audit report.
 *
 * @author Dmytro Nochevnov
 * @author Igor Vinokur
 */
public abstract class Printer {

    private static final Logger LOG = LoggerFactory.getLogger(Printer.class);

    private Path auditReport;

    public Printer(Path auditReport) {
        this.auditReport = auditReport;
    }

    /**
     * Prints info into the audit report.
     *
     * @throws ServerException
     *         if an error occurs
     */
    public abstract void print() throws ServerException;

    protected void printRow(String row) throws ServerException {
        try {
            append(row, auditReport.toFile(), Charset.defaultCharset());
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new ServerException("Failed to generate audit report. " + e.getMessage(), e);
        }
    }

    /**
     * Prints error in format:
     * [ERROR] <error text>!
     *
     * @param error
     *         text of error
     * @throws ServerException
     *         if an error occurs
     */
    protected void printError(String error) throws ServerException {
        printRow(format("[ERROR] %s!\n", error));
    }

    protected String timestampToString(long timestamp) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy MMM dd - HH:mm:ss", Locale.ENGLISH);
        return df.format(timestamp);
    }

    public static Printer createSystemInfoPrinter(Path auditReport, long allUsersNumber, @Nullable SystemLicense license) {
        return new SystemInfoPrinter(auditReport, allUsersNumber, license);
    }

    public static Printer createUserPrinter(Path auditReport, UserImpl user, List<WorkspaceImpl> workspaces, Map<String, AbstractPermissions> wsPermissions) {
        return new UserInfoPrinter(auditReport, user, workspaces, wsPermissions);
    }

    public static Printer createErrorPrinter(Path auditReport, String error) {
        return new ErrorInfoPrinter(auditReport, error);
    }

    public static Printer createActionPrinter(Path auditReport, SystemLicenseAction licenseAction) {
        switch (licenseAction.getActionType()) {
            case ACCEPTED:
                if (licenseAction.getLicenseType() == Constants.PaidLicense.FAIR_SOURCE_LICENSE) {
                   return new FairSourceLicenseAcceptanceInfoPrinter(auditReport, licenseAction);
                }
                break;

            case ADDED:
                if (licenseAction.getLicenseType() == Constants.PaidLicense.PRODUCT_LICENSE) {
                    return new ProductLicenseAdditionInfoPrinter(auditReport, licenseAction);
                }
                break;

            case EXPIRED:
                if (licenseAction.getLicenseType() == Constants.PaidLicense.PRODUCT_LICENSE) {
                    return new ProductLicenseExpirationInfoPrinter(auditReport, licenseAction);
                }
                break;

            case REMOVED:
                if (licenseAction.getLicenseType() == Constants.PaidLicense.PRODUCT_LICENSE) {
                    return new ProductLicenseRemovalInfoPrinter(auditReport, licenseAction);
                }
                break;

            default:
        }

        return new NullPrinter(auditReport);
    }

    public static DelimiterPrinter createDelimiterPrinter(Path auditReport, String title) {
        return new DelimiterPrinter(auditReport, title);
    }
}
