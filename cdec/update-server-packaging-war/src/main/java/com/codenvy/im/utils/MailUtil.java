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
package com.codenvy.im.utils;

import com.google.inject.Singleton;

import com.codenvy.mail.MailSenderClient;
import org.eclipse.che.api.core.ApiException;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;

/** @author Dmytro Nochevnov */
@Singleton
public class MailUtil {
    private final MailSenderClient      mailClient;
    private final MailUtilConfiguration config;

    @Inject
    public MailUtil(MailSenderClient mailClient, MailUtilConfiguration config) {
        this.mailClient = mailClient;
        this.config = config;
    }

    public void sendNotificationLetter(String accountId, String userEmail) throws IOException, MessagingException, ApiException {
        final String mailSubject = "New On-Prem Trial (IM)";
        final String currentDateTime = new Date().toString();

        String mailContent = String.format("Email address of user: %1$s\n"
                                           + "AccountID of user: %2$s\n"
                                           + "Date and time of request: %3$s",
                                           userEmail,
                                           accountId,
                                           currentDateTime);

        mailClient.sendMail(config.getNotificationSender(),
                            config.getNotificationRecipients(),
                            null,
                            mailSubject,
                            MediaType.TEXT_PLAIN,
                            mailContent);
    }
}
