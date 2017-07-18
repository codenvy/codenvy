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
package com.codenvy.api.audit.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.Files.copy;

/**
 * Defines Audit report REST API.
 *
 * @author Igor Vinokur
 */
@Path("/audit")
@Api(value = "audit", description = "Audit service")
public class AuditService extends Service {

    private final AuditManager auditManager;

    @Inject
    public AuditService(AuditManager auditManager) {
        this.auditManager = auditManager;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Generate audit log")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK"),
                           @ApiResponse(code = 500, message = "Server error")})
    public Response downloadReport() throws ServerException, ConflictException, IOException {
        java.nio.file.Path report = auditManager.generateAuditReport();

        StreamingOutput stream = outputStream -> {
            try {
                copy(report, outputStream);
            } finally {
                auditManager.deleteReportDirectory(report);
            }
        };

        return Response.ok(stream, MediaType.TEXT_PLAIN)
                       .header("Content-Length", String.valueOf(Files.size(report)))
                       .header("Content-Disposition", "attachment; filename=" + report.getFileName().toString())
                       .build();
    }
}
