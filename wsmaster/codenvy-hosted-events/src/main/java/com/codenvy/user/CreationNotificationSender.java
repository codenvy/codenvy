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
package com.codenvy.user;

import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.mail.EmailBean;
import com.codenvy.mail.MailSender;
import com.codenvy.service.password.RecoveryStorage;
import com.codenvy.template.processor.html.HTMLTemplateProcessor;
import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;
import com.codenvy.user.email.template.CreateUserWithPasswordTemplate;
import com.codenvy.user.email.template.CreateUserWithoutPasswordTemplate;

import org.eclipse.che.api.core.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URL;

import static javax.ws.rs.core.MediaType.TEXT_HTML;

/**
 * Sends email notification to users about their registration in Codenvy
 *
 * @author Sergii Leschenko
 * @author Anton Korneta
 */
@Singleton
public class CreationNotificationSender {
    private static final Logger LOG = LoggerFactory.getLogger(CreationNotificationSender.class);

    private final String                                   apiEndpoint;
    private final String                                   mailFrom;
    private final MailSender                               mailSender;
    private final RecoveryStorage                          recoveryStorage;
    private final HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf;
    private final DefaultEmailResourceResolver             resourceResolver;

    @Inject
    public CreationNotificationSender(@Named("che.api") String apiEndpoint,
                                      @Named("mailsender.application.from.email.address") String mailFrom,
                                      RecoveryStorage recoveryStorage,
                                      MailSender mailSender,
                                      HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf,
                                      DefaultEmailResourceResolver resourceResolver) {
        this.apiEndpoint = apiEndpoint;
        this.mailFrom = mailFrom;
        this.recoveryStorage = recoveryStorage;
        this.mailSender = mailSender;
        this.thymeleaf = thymeleaf;
        this.resourceResolver = resourceResolver;
    }

    public void sendNotification(String userName,
                                 String userEmail,
                                 boolean withPassword) throws IOException, ApiException {
        final URL urlEndpoint = new URL(apiEndpoint);
        final String masterEndpoint = urlEndpoint.getProtocol() + "://" + urlEndpoint.getHost();
        final ThymeleafTemplate template = withPassword ? templateWithPassword(masterEndpoint, userEmail, userName)
                                                        : templateWithoutPassword(masterEndpoint, userName);
        final EmailBean emailBean = new EmailBean().withBody(thymeleaf.process(template))
                                                   .withFrom(mailFrom)
                                                   .withTo(userEmail)
                                                   .withReplyTo(null)
                                                   .withSubject("Welcome To Codenvy")
                                                   .withMimeType(TEXT_HTML);
        mailSender.sendMail(resourceResolver.resolve(emailBean));
    }

    private ThymeleafTemplate templateWithPassword(String masterEndpoint, String userEmail, String userName) {
        final String uuid = recoveryStorage.generateRecoverToken(userEmail);
        final String resetPasswordLink = UriBuilder.fromUri(masterEndpoint)
                                                   .path("site/setup-password")
                                                   .queryParam("id", uuid)
                                                   .build(userEmail)
                                                   .toString();
        return new CreateUserWithPasswordTemplate(masterEndpoint, resetPasswordLink, userName);
    }

    private ThymeleafTemplate templateWithoutPassword(String masterEndpoint, String userName) {
        return new CreateUserWithoutPasswordTemplate(masterEndpoint, userName);
    }

}
