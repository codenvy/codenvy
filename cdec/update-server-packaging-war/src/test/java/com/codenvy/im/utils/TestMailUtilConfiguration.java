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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Dmytro Nochevnov
 */
public class TestMailUtilConfiguration {

    @Test
    public void shouldCreateObject() {
        MailUtilConfiguration config = new MailUtilConfiguration("sender", "emails");
        assertEquals(config.getNotificationSender(), "sender");
        assertEquals(config.getNotificationRecipients(), "emails");
    }

    @Test(dataProvider = "dataToTestIncorrectArguments")
    public void shouldThrowExceptionOnIncorrectArguments(String sender, String recipients, String expectedErrorMessage) {
        try {
            new MailUtilConfiguration(sender, recipients);
        } catch(IllegalArgumentException e) {
            assertEquals(e.getMessage(), expectedErrorMessage);
            return;
        }

        fail("There should be IllegalArgumentException above.");
    }

    @DataProvider
    public Object[][] dataToTestIncorrectArguments() {
        return new Object[][] {
            {null, null, MailUtilConfiguration.MAIL_NOTIFICATION_SENDER + " property cannot be null or empty."},
            {"", null, MailUtilConfiguration.MAIL_NOTIFICATION_SENDER + " property cannot be null or empty."},
            {"sender", null, MailUtilConfiguration.MAIL_NOTIFICATION_RECIPIENTS + " property cannot be null or empty."},
            {"sender", "", MailUtilConfiguration.MAIL_NOTIFICATION_RECIPIENTS + " property cannot be null or empty."},
        };
    }
}
