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

import com.codenvy.api.permission.server.model.impl.AbstractPermissions;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
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

    public static Printer createUserPrinter(Path auditReport, UserImpl user, List<WorkspaceImpl> workspaces, Map<String, AbstractPermissions> wsPermissions) {
        return new UserInfoPrinter(auditReport, user, workspaces, wsPermissions);
    }

    public static Printer createErrorPrinter(Path auditReport, String error) {
        return new ErrorInfoPrinter(auditReport, error);
    }

    public static DelimiterPrinter createDelimiterPrinter(Path auditReport, String title) {
        return new DelimiterPrinter(auditReport, title);
    }
}
