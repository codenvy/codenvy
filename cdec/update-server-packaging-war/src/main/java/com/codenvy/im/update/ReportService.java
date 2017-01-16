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
package com.codenvy.im.update;

import com.codenvy.report.ReportParameters;
import com.codenvy.report.ReportType;
import com.codenvy.im.utils.InjectorBootstrap;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;

import static java.lang.String.format;

/**
 * Report API.
 *
 * @author Dmytro Nochevnov
 */
@Path("report")
public class ReportService {

    static Logger LOG = LoggerFactory.getLogger(ReportService.class);

    @Inject
    public ReportService() {
    }

    /** Get parameters of certain report. */
    @GenerateLink(rel = "return certain report parameters")
    @GET
    @Path("/parameters/{report_type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportParameters(@PathParam("report_type") String reportTypeStr) {
        try {
            ReportType reportType = ReportType.valueOf(reportTypeStr.toUpperCase());
            ReportParameters parameters = getParameters(reportType);
            return Response.ok(parameters).build();
        } catch (IllegalArgumentException iae) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(format("Report type '%s' not found.", reportTypeStr))
                           .build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(format("Unexpected error: '%s'.", e.getMessage()))
                           .build();
        }
    }

    /** is package private for testing propose **/
    ReportParameters getParameters(ReportType reportType) {
        String parameterPrefix = reportType.name().toLowerCase();
        ReportParameters parameters = new ReportParameters(
            InjectorBootstrap.INJECTOR.getInstance(Key.get(String.class, Names.named(parameterPrefix + ".title"))),
            InjectorBootstrap.INJECTOR.getInstance(Key.get(String.class, Names.named(parameterPrefix + ".sender"))),
            InjectorBootstrap.INJECTOR.getInstance(Key.get(String.class, Names.named(parameterPrefix + ".receiver")))
        );
        return parameters;
    }

}
