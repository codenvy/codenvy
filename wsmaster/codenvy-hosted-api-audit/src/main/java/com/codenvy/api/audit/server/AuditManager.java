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
package com.codenvy.api.audit.server;

import com.codenvy.api.license.CodenvyLicense;
import com.codenvy.api.license.LicenseException;
import com.codenvy.api.license.server.CodenvyLicenseManager;
import com.codenvy.api.permission.server.PermissionsManager;
import com.codenvy.api.permission.server.model.impl.AbstractPermissions;

import org.apache.commons.io.FileUtils;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.Page.PageRef;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.codenvy.api.workspace.server.WorkspaceDomain.DOMAIN_ID;
import static java.nio.file.Files.createTempDirectory;

/**
 * Facade for audit report related operations.
 *
 * @author Igor Vinokur
 */
@Singleton
public class AuditManager {

    private static final Logger LOG = LoggerFactory.getLogger(AuditManager.class);

    private final WorkspaceManager      workspaceManager;
    private final PermissionsManager    permissionsManager;
    private final CodenvyLicenseManager licenseManager;
    private final AuditReportPrinter    reportPrinter;
    private final UserManager           userManager;

    private AtomicBoolean inProgress = new AtomicBoolean(false);

    @Inject
    public AuditManager(UserManager userManager,
                        WorkspaceManager workspaceManager,
                        PermissionsManager permissionsManager,
                        CodenvyLicenseManager licenseManager,
                        AuditReportPrinter reportPrinter) {
        this.userManager = userManager;
        this.workspaceManager = workspaceManager;
        this.permissionsManager = permissionsManager;
        this.licenseManager = licenseManager;
        this.reportPrinter = reportPrinter;
    }

    /**
     * Generates file with audit report in plain/text format.
     * The audit report contains information about license, users and their workspaces.
     *
     * @return path of the audit report file
     * @throws ServerException
     *         if an error occurs
     * @throws ConflictException
     *         if generating report is already in progress
     */
    public Path generateAuditReport() throws ServerException, ConflictException {
        if (!inProgress.compareAndSet(false, true)) {
            throw new ConflictException("Generating report is already in progress");
        }

        Path auditReport = null;
        try {
            String dateTime = new SimpleDateFormat("dd-MM-yyyy_hh:mm:ss").format(new Date());
            auditReport = createTempDirectory(null).resolve("report_" + dateTime + ".txt");
            Files.createFile(auditReport);

            CodenvyLicense license = null;
            try {
                license = licenseManager.load();
            } catch (LicenseException ignored) {
                //Continue printing report without license info
            }
            reportPrinter.printHeader(auditReport, userManager.getTotalCount(), license);
            printAllUsers(auditReport);
        } catch (Exception exception) {
            if (auditReport != null) {
                deleteReportDirectory(auditReport);
            }
            LOG.error(exception.getMessage(), exception);
            throw new ServerException(exception.getMessage(), exception);
        } finally {
            inProgress.set(false);
        }

        return auditReport;
    }

    private void printAllUsers(Path auditReport) throws ServerException {
        Page<UserImpl> currentPage = userManager.getAll(30, 0);
        do {
            //Print users with their workspaces from current page
            for (UserImpl user : currentPage.getItems()) {
                List<WorkspaceImpl> workspaces;
                try {
                    workspaces = workspaceManager.getWorkspaces(user.getId());
                    Set<String> workspaceIds = workspaces.stream()
                                                         .map(WorkspaceImpl::getId)
                                                         .collect(Collectors.toSet());
                    //add workspaces witch are belong to user, but user doesn't have permissions for them.
                    workspaceManager.getByNamespace(user.getName())
                                    .stream()
                                    .filter(workspace -> !workspaceIds.contains(workspace.getId()))
                                    .forEach(workspaces::add);
                } catch (ServerException exception) {
                    reportPrinter.printError("Failed to receive list of related workspaces for user " + user.getId(), auditReport);
                    continue;
                }
                Map<String, AbstractPermissions> wsPermissions = new HashMap<>();
                for (WorkspaceImpl workspace : workspaces) {
                    try {
                        wsPermissions.put(workspace.getId(), permissionsManager.get(user.getId(), DOMAIN_ID, workspace.getId()));
                    } catch (NotFoundException | ConflictException ignored) {
                        //User doesn't have permissions for workspace
                    }
                }
                reportPrinter.printUserInfoWithHisWorkspacesInfo(auditReport, user, workspaces, wsPermissions);
            }

        } while ((currentPage = getNextPage(currentPage)) != null);
    }

    private Page<UserImpl> getNextPage(Page<UserImpl> currentPage) throws ServerException {
        if (currentPage.hasNextPage()) {
            final PageRef nextPageRef = currentPage.getNextPageRef();
            return userManager.getAll(nextPageRef.getPageSize(), nextPageRef.getItemsBefore());
        } else {
            return null;
        }
    }

    void deleteReportDirectory(Path auditReport) {
        try {
            FileUtils.deleteDirectory(auditReport.getParent().toFile());
        } catch (IOException exception) {
            LOG.error(exception.getMessage(), exception);
        }
    }
}
