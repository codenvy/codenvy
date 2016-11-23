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
package com.codenvy.report;

import com.codenvy.api.license.exception.LicenseException;
import com.codenvy.api.license.server.CodenvyLicenseManager;
import com.codenvy.mail.MailSenderClient;
import com.codenvy.report.shared.dto.Ip;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmytro Nochevnov
 */
public class ReportSenderTest {
    public static final String TEST_TITLE                = "title";
    public static final String TEST_SENDER               = "test@sender";
    public static final String TEST_RECEIVER             = "test@receiver";
    public static final String CLIENT_IP                 = "192.168.1.1";
    public static final Ip     TEST_IP                   = DtoFactory.newDto(Ip.class).withValue(CLIENT_IP);
    public static final long   USER_NUMBER               = 150L;
    public static final String HOSTNAME                  = "codenvy.onprem";
    public static final String API_ENDPOINT              = "http://" + HOSTNAME + "/api";
    public static final String UPDATE_SERVER_ENDPOINT    = "update/endpoint";
    public static final String CLIENT_IP_SERVICE         = UPDATE_SERVER_ENDPOINT + "/util/client-ip";
    public static final String REPORT_PARAMETERS_SERVICE =
            UPDATE_SERVER_ENDPOINT + "/report/parameters/" + ReportType.CODENVY_ONPREM_USER_NUMBER_REPORT.name().toLowerCase();

    public static ReportParameters REPORT_PARAMETERS;

    private ReportSender spyReportSender;

    @Mock
    private MailSenderClient       mockMailClient;
    @Mock
    private HttpJsonRequestFactory mockHttpJsonRequestFactory;
    @Mock
    private HttpJsonRequest        mockHttpJsonRequest;
    @Mock
    private HttpJsonResponse       mockHttpJsonResponse;
    @Mock
    private UserManager            userManager;
    @Mock
    private CodenvyLicenseManager  mockLicenseManager;

    @BeforeMethod
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        spyReportSender = spy(new ReportSender(UPDATE_SERVER_ENDPOINT,
                                               API_ENDPOINT,
                                               mockMailClient,
                                               mockHttpJsonRequestFactory,
                                               mockLicenseManager,
                                               userManager));

        REPORT_PARAMETERS = new ReportParameters(TEST_TITLE, TEST_SENDER, TEST_RECEIVER);

        doReturn(mockHttpJsonRequest).when(mockHttpJsonRequestFactory).fromUrl(REPORT_PARAMETERS_SERVICE);
        doReturn(mockHttpJsonRequest).when(mockHttpJsonRequestFactory).fromUrl(CLIENT_IP_SERVICE);

        doReturn(mockHttpJsonResponse).when(mockHttpJsonRequest).request();
        doReturn(TEST_IP).when(mockHttpJsonResponse).asDto(Ip.class);
        doReturn(REPORT_PARAMETERS).when(mockHttpJsonResponse).as(ReportParameters.class,
                                                                  ReportParameters.class.getGenericSuperclass());

        when(userManager.getTotalCount()).thenReturn(USER_NUMBER);
    }

    @Test
    public void shouldSendWeeklyReportBecauseOfExpiredLicense() throws IOException, JsonParseException, MessagingException, ApiException {
        doReturn(false).when(mockLicenseManager).isSystemUsageLegal();

        spyReportSender.sendWeeklyReports();

        verify(mockMailClient)
                .sendMail(TEST_SENDER, TEST_RECEIVER, null, TEST_TITLE, MediaType.TEXT_PLAIN, "External IP address: " + CLIENT_IP + "\n"
                                                                                              + "Hostname: " + HOSTNAME + "\n"
                                                                                              + "Number of users: " + USER_NUMBER + "\n");
    }

    @Test
    public void shouldSendWeeklyReportBecauseOfLicenseException() throws IOException, JsonParseException, MessagingException, ApiException {
        doThrow(LicenseException.class).when(mockLicenseManager).isSystemUsageLegal();

        spyReportSender.sendWeeklyReports();

        verify(mockMailClient)
                .sendMail(TEST_SENDER, TEST_RECEIVER, null, TEST_TITLE, MediaType.TEXT_PLAIN, "External IP address: " + CLIENT_IP + "\n"
                                                                                              + "Hostname: " + HOSTNAME + "\n"
                                                                                              + "Number of users: " + USER_NUMBER + "\n");
    }

    @Test
    public void shouldNotSendWeeklyReportBecauseOfNonExpiredLicense()
            throws IOException, JsonParseException, MessagingException, ApiException {
        doReturn(true).when(mockLicenseManager).isSystemUsageLegal();

        spyReportSender.sendWeeklyReports();

        verify(mockMailClient, never()).sendMail(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(userManager, never()).getAll(30,
                                            0);    // TODO Replace it with UserManager#getTotalCount when codenvy->jpa-integration branch will be merged to master
        verify(mockHttpJsonRequestFactory, never()).fromUrl(REPORT_PARAMETERS_SERVICE);
        verify(mockHttpJsonRequestFactory, never()).fromUrl(CLIENT_IP_SERVICE);
    }

}
