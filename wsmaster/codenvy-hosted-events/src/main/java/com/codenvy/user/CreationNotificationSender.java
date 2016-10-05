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
package com.codenvy.user;

import com.codenvy.mail.MailSenderClient;
import com.codenvy.mail.shared.dto.AttachmentDto;
import com.codenvy.mail.shared.dto.EmailBeanDto;
import com.codenvy.service.password.RecoveryStorage;
import com.google.api.client.repackaged.com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Files;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.commons.lang.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.eclipse.che.commons.lang.IoUtil.getResource;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Sends email notification to users about their registration in Codenvy
 *
 * @author Sergii Leschenko
 */
public class CreationNotificationSender {
    private static final Logger LOG = LoggerFactory.getLogger(CreationNotificationSender.class);

    public static final String EMAIL_TEMPLATE_USER_CREATED_WITH_PASSWORD    = "email-templates/user_created_with_password.html";
    public static final String EMAIL_TEMPLATE_USER_CREATED_WITHOUT_PASSWORD = "email-templates/user_created_without_password.html";

    private static final String LOGO     = "/email-templates/header.png";
    private static final String LOGO_CID = "codenvyLogo";

    @VisibleForTesting
    @Inject
    @Named("che.api")
    String apiEndpoint;

    @VisibleForTesting
    @Inject
    @Named("mailsender.application.from.email.address")
    String mailSender;

    @Inject
    private MailSenderClient mailSenderClient;

    @Inject
    private RecoveryStorage recoveryStorage;

    @VisibleForTesting
    public void sendNotification(String userName, String userEmail, String template) throws IOException, ApiException {
        final URL urlEndpoint = new URL(apiEndpoint);

        String uuid = recoveryStorage.generateRecoverToken(userEmail);

        UriBuilder resetPasswordLinkUriBuilder = UriBuilder.fromUri(urlEndpoint.getProtocol() + "://" + urlEndpoint.getHost())
                                                           .path("site/setup-password")
                                                           .queryParam("id", uuid);
        String resetPasswordLink = resetPasswordLinkUriBuilder.build(userEmail).toString();

        Map<String, String> properties = new HashMap<>();
        properties.put("logo.cid", "codenvyLogo");
        properties.put("user.name", userName);
        properties.put("user.mail", userEmail);
        properties.put("setup.password.link", resetPasswordLink);
        properties.put("com.codenvy.masterhost.url", urlEndpoint.getProtocol() + "://" + urlEndpoint.getHost());

        File logo = new File(this.getClass().getResource(LOGO).getPath());

        AttachmentDto attachmentDto = newDto(AttachmentDto.class)
                .withContent(Base64.getEncoder().encodeToString(Files.toByteArray(logo)))
                .withContentId(LOGO_CID)
                .withFileName("logo.png");

        EmailBeanDto emailBeanDto = newDto(EmailBeanDto.class)
                .withBody(Deserializer.resolveVariables(readAndCloseQuietly(getResource("/" + template)), properties))
                .withFrom(mailSender)
                .withTo(userEmail)
                .withReplyTo(null)
                .withSubject("Welcome To Codenvy")
                .withMimeType(TEXT_HTML)
                .withAttachments(Collections.singletonList(attachmentDto));

        mailSenderClient.sendMail(emailBeanDto);

        LOG.info("User created message send to {}", userEmail);
    }
}
