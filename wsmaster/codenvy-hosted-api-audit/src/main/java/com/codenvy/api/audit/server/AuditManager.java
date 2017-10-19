/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.api.audit.server;

import static java.nio.file.Files.createTempDirectory;
import static org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain.DOMAIN_ID;

import com.codenvy.api.audit.server.printer.Printer;
import com.google.common.annotations.VisibleForTesting;
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
import javax.inject.Inject;
import javax.inject.Singleton;
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
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;
import org.eclipse.che.multiuser.api.permission.server.model.impl.AbstractPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for audit report related operations.
 *
 * @author Igor Vinokur
 */
@Singleton
public class AuditManager {

  private static final Logger LOG = LoggerFactory.getLogger(AuditManager.class);

  private final WorkspaceManager workspaceManager;
  private final PermissionsManager permissionsManager;
  private final UserManager userManager;

  private AtomicBoolean inProgress = new AtomicBoolean(false);

  @Inject
  public AuditManager(
      UserManager userManager,
      WorkspaceManager workspaceManager,
      PermissionsManager permissionsManager) {
    this.userManager = userManager;
    this.workspaceManager = workspaceManager;
    this.permissionsManager = permissionsManager;
  }

  /**
   * Generates file with audit report in plain/text format. The audit report contains information
   * about license, users and their workspaces.
   *
   * @return path of the audit report file
   * @throws ServerException if an error occurs
   * @throws ConflictException if generating report is already in progress
   */
  public Path generateAuditReport() throws ServerException, ConflictException {
    if (!inProgress.compareAndSet(false, true)) {
      throw new ConflictException("Generating report is already in progress");
    }

    Path auditReport = null;
    try {
      auditReport = createEmptyAuditReportFile();
      printSystemInfo(auditReport);
      printAllUsersInfo(auditReport);
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

  private Path createEmptyAuditReportFile() throws IOException {
    String dateTime = new SimpleDateFormat("dd-MM-yyyy_hh:mm:ss").format(new Date());
    Path auditReport = createTempDirectory(null).resolve("report_" + dateTime + ".txt");
    Files.createFile(auditReport);
    return auditReport;
  }

  void printSystemInfo(Path auditReport) throws ServerException {
    Printer.createSystemInfoPrinter(auditReport, userManager.getTotalCount()).print();
  }

  private void printDelimiter(Path auditReport, String title) throws ServerException {
    Printer.createDelimiterPrinter(auditReport, title).print();
  }

  private void printAllUsersInfo(Path auditReport) throws ServerException {
    Page<UserImpl> currentPage = userManager.getAll(30, 0);
    do {
      // Print users with their workspaces from current page
      for (UserImpl user : currentPage.getItems()) {
        List<WorkspaceImpl> workspaces;
        try {
          workspaces = workspaceManager.getWorkspaces(user.getId(), false);
          Set<String> workspaceIds =
              workspaces.stream().map(WorkspaceImpl::getId).collect(Collectors.toSet());
          // add workspaces witch are belong to user, but user doesn't have permissions for them.
          workspaceManager
              .getByNamespace(user.getName(), false)
              .stream()
              .filter(workspace -> !workspaceIds.contains(workspace.getId()))
              .forEach(workspaces::add);
        } catch (ServerException exception) {
          Printer.createErrorPrinter(
                  auditReport,
                  "Failed to retrieve the list of related workspaces for user " + user.getId())
              .print();
          continue;
        }
        Map<String, AbstractPermissions> wsPermissions = new HashMap<>();
        for (WorkspaceImpl workspace : workspaces) {
          try {
            wsPermissions.put(
                workspace.getId(),
                permissionsManager.get(user.getId(), DOMAIN_ID, workspace.getId()));
          } catch (NotFoundException | ConflictException ignored) {
            // User doesn't have permissions for workspace
          }
        }
        Printer.createUserPrinter(auditReport, user, workspaces, wsPermissions).print();
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

  @VisibleForTesting
  void deleteReportDirectory(Path auditReport) {
    try {
      FileUtils.deleteDirectory(auditReport.getParent().toFile());
    } catch (IOException exception) {
      LOG.error(exception.getMessage(), exception);
    }
  }
}
