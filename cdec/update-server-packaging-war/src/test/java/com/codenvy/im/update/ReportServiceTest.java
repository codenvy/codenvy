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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

/**
 * @author Dmytro Nochevnov
 */
public class ReportServiceTest {
    public static final String TEST_REPORT_TYPE    = ReportType.CODENVY_ONPREM_USER_NUMBER_REPORT.name();
    public static final String UNKNOWN_REPORT_TYPE = "unknown-report-type";

    private ReportService spyReportService;

    /** @see {update-server-packaging-war/src/test/resourses/report.properties} */
    ReportParameters testReportParameters = new ReportParameters("test title", "test@sender", "test@receiver");

    @BeforeMethod
    public void setup() {
        spyReportService = spy(new ReportService());
    }

    @Test
    public void shouldReturnReportParameters() {
        Response response = spyReportService.getReportParameters(TEST_REPORT_TYPE);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(response.getEntity(), testReportParameters);
    }

    @Test
    public void shouldReturnNotFoundError() {
        Response response = spyReportService.getReportParameters(UNKNOWN_REPORT_TYPE);
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
        assertEquals(response.getEntity(), String.format("Report type '%s' not found.", UNKNOWN_REPORT_TYPE));
    }

    @Test
    public void shouldReturnInternalServerError() {
        doThrow(new RuntimeException("Configuration error")).when(spyReportService).getParameters(ReportType.CODENVY_ONPREM_USER_NUMBER_REPORT);
        Response response = spyReportService.getReportParameters(TEST_REPORT_TYPE);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertEquals(response.getEntity(), "Unexpected error: 'Configuration error'.");
    }
}
