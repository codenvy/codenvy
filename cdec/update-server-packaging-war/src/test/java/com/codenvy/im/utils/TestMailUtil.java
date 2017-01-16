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

import com.codenvy.mail.MailSenderClient;
import org.eclipse.che.api.core.ApiException;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * @author Dmytro Nochevnov
 */
public class TestMailUtil {
    public static final String RECEIVER_EMAILS = "to@com";
    public static final String SENDER_EMAIL = "from@com";
    private MailUtil spyMailUtil;
    private MailUtilConfiguration mailTransportConfig = new MailUtilConfiguration(SENDER_EMAIL, RECEIVER_EMAILS);

    @Mock
    private MailSenderClient mockMailService;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
        spyMailUtil = spy(new MailUtil(mockMailService, mailTransportConfig));
    }

    @Test
    public void testSendOnPremSubscriptionInfo() throws IOException, MessagingException, ApiException {
        String currentDateStringWithHoursOnly = new Date().toString().substring(0, 14);  // "Thu Jan 29 12:"
        String expectedSubscriptionInfo = "Email address of user: userEmail\n"
                                          + "AccountID of user: accountId\n"
                                          + "Date and time of request: " + currentDateStringWithHoursOnly;

        spyMailUtil.sendNotificationLetter("accountId", "userEmail");

        verify(mockMailService).sendMail(eq(SENDER_EMAIL),
                                         eq(RECEIVER_EMAILS),
                                         (String)Matchers.isNull(),
                                         eq("New On-Prem Trial (IM)"),
                                         eq(MediaType.TEXT_PLAIN),
                                         startsWith(expectedSubscriptionInfo));
    }

}
