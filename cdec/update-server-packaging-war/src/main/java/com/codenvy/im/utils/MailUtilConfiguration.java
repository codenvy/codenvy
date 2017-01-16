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

import com.google.inject.Inject;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/** @author Dmytro Nochevnov */
@Singleton
public class MailUtilConfiguration {

    public static final String MAIL_NOTIFICATION_SENDER = "mail.notification.sender";
    public static final String MAIL_NOTIFICATION_RECIPIENTS = "mail.notification.recipients";

    private final String notificationRecipients;
    private final String notificationSender;

    @Inject
    public MailUtilConfiguration(@Named(MAIL_NOTIFICATION_SENDER) @NotNull String notificationSender,
                                 @Named(MAIL_NOTIFICATION_RECIPIENTS) @NotNull String notificationRecipients) {
        if (notificationSender == null || notificationSender.isEmpty()) {
            throw new IllegalArgumentException(MAIL_NOTIFICATION_SENDER + " property cannot be null or empty.");
        }

        if (notificationRecipients == null || notificationRecipients.isEmpty()) {
            throw new IllegalArgumentException(MAIL_NOTIFICATION_RECIPIENTS + " property cannot be null or empty.");
        }

        this.notificationSender = notificationSender;
        this.notificationRecipients = notificationRecipients;
    }

    public String getNotificationRecipients() {
        return notificationRecipients;
    }

    public String getNotificationSender() {
        return notificationSender;
    }
}
