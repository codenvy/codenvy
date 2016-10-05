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

import com.codenvy.api.license.server.CodenvyLicenseManager;
import com.codenvy.mail.MailSenderClient;
import com.codenvy.report.shared.dto.Ip;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.schedule.ScheduleCron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;

/**
 * Sends reports:
 * <li>to Codenvy with number of users.</li>
 *
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
@Singleton
public class ReportSender {
    private static final Logger LOG = LoggerFactory.getLogger(ReportSender.class);

    private final MailSenderClient       mailClient;
    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final String                 updateServerEndpoint;
    private final CodenvyLicenseManager  licenseManager;
    private final UserManager            userManager;
    private final String                 apiEndpoint;

    @Inject
    public ReportSender(@Named("report-sender.update_server_endpoint") String updateServerEndpoint,
                        @Named("che.api") String apiEndpoint,
                        MailSenderClient mailClient,
                        HttpJsonRequestFactory httpJsonRequestFactory,
                        CodenvyLicenseManager licenseManager,
                        UserManager userManager) {
        this.mailClient = mailClient;
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.updateServerEndpoint = updateServerEndpoint;
        this.licenseManager = licenseManager;
        this.userManager = userManager;
        this.apiEndpoint = apiEndpoint;
    }

    @ScheduleCron(cron = "0 0 1 ? * SUN *")  // send each Sunday at 1:00 AM. Use "0 0/1 * 1/1 * ? *" to send every 1 minute.
    public void sendWeeklyReports() {
        try {
            sendNumberOfUsers();
        } catch (JsonParseException | IOException | MessagingException | ApiException e) {
            LOG.error("Error of sending weekly reports.", e);
        }
    }

    private void sendNumberOfUsers() throws IOException, MessagingException, JsonParseException, ApiException {
        try {
            if (licenseManager.isCodenvyUsageLegal()) {
                // do not send a report if codenvy usage is legal.
                return;
            }
        } catch (Exception e) {
            LOG.error("There is a problem with Codenvy License.", e);
            // send report if there is a problem with license
        }

        ReportParameters parameters = obtainReportParameters(ReportType.CODENVY_ONPREM_USER_NUMBER_REPORT);

        Ip externalIP = obtainExternalIP();

        StringBuilder msg = new StringBuilder();
        msg.append(String.format("External IP address: %s\n", externalIP.getValue()));
        msg.append(String.format("Hostname: %s\n", new URL(apiEndpoint).getHost()));
        msg.append(String.format("Number of users: %s\n", userManager.getTotalCount()));

        mailClient.sendMail(parameters.getSender(), parameters.getReceiver(), null, parameters.getTitle(), MediaType.TEXT_PLAIN,
                            msg.toString());
    }

    private Ip obtainExternalIP() throws IOException,
                                         ForbiddenException,
                                         BadRequestException,
                                         ConflictException,
                                         NotFoundException,
                                         ServerException,
                                         UnauthorizedException {
        String requestUrl = String.format("%s/util/client-ip", updateServerEndpoint);

        return httpJsonRequestFactory.fromUrl(requestUrl)
                                     .request()
                                     .asDto(Ip.class);

    }

    private ReportParameters obtainReportParameters(ReportType reportType) throws
                                                                           IOException,
                                                                           JsonParseException,
                                                                           ForbiddenException,
                                                                           BadRequestException,
                                                                           ConflictException,
                                                                           NotFoundException,
                                                                           ServerException,
                                                                           UnauthorizedException {
        String requestUrl = String.format("%s/report/parameters/%s",
                                          updateServerEndpoint,
                                          reportType.name().toLowerCase());

        return httpJsonRequestFactory.fromUrl(requestUrl)
                                     .request()
                                     .as(ReportParameters.class, ReportParameters.class.getGenericSuperclass());

    }
}
