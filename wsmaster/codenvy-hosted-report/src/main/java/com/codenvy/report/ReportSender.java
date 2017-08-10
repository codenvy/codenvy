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
package com.codenvy.report;

import com.codenvy.mail.MailSender;
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

    private final MailSender             mailSender;
    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final String                 updateServerEndpoint;
    private final UserManager            userManager;
    private final String                 apiEndpoint;

    @Inject
    public ReportSender(@Named("report-sender.update_server_endpoint") String updateServerEndpoint,
                        @Named("che.api") String apiEndpoint,
                        MailSender mailSender,
                        HttpJsonRequestFactory httpJsonRequestFactory,
                        UserManager userManager) {
        this.mailSender = mailSender;
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.updateServerEndpoint = updateServerEndpoint;
        this.userManager = userManager;
        this.apiEndpoint = apiEndpoint;
    }

    @ScheduleCron(cron = "0 0 1 ? * SUN *")  // send each Sunday at 1:00 AM. Use "0 0/1 * 1/1 * ? *" to send every 1 minute.
    public void sendWeeklyReports() {
        try {
            sendNumberOfUsers();
        } catch (JsonParseException | IOException | ApiException e) {
            LOG.error("Error of sending weekly reports.", e);
        }
    }

    private void sendNumberOfUsers() throws IOException, JsonParseException, ApiException {
        ReportParameters parameters = obtainReportParameters(ReportType.CODENVY_ONPREM_USER_NUMBER_REPORT);

        Ip externalIP = obtainExternalIP();

        StringBuilder msg = new StringBuilder();
        msg.append(String.format("External IP address: %s\n", externalIP.getValue()));
        msg.append(String.format("Hostname: %s\n", new URL(apiEndpoint).getHost()));
        msg.append(String.format("Number of users: %s\n", userManager.getTotalCount()));

        mailSender.sendMail(parameters.getSender(), parameters.getReceiver(), null, parameters.getTitle(), MediaType.TEXT_PLAIN,
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
