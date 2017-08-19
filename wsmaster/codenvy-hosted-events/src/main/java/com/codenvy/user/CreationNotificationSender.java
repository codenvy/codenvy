/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.user;

import static javax.ws.rs.core.MediaType.TEXT_HTML;

import com.codenvy.mail.DefaultEmailResourceResolver;
import com.codenvy.mail.EmailBean;
import com.codenvy.mail.MailSender;
import com.codenvy.service.password.RecoveryStorage;
import com.codenvy.template.processor.html.HTMLTemplateProcessor;
import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;
import com.codenvy.user.email.template.CreateUserWithPasswordTemplate;
import com.codenvy.user.email.template.CreateUserWithoutPasswordTemplate;
import java.io.IOException;
import java.net.URL;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends email notification to users about their registration in Codenvy
 *
 * @author Sergii Leschenko
 * @author Anton Korneta
 */
@Singleton
public class CreationNotificationSender {
  private static final Logger LOG = LoggerFactory.getLogger(CreationNotificationSender.class);

  private final String apiEndpoint;
  private final String mailFrom;
  private final MailSender mailSender;
  private final RecoveryStorage recoveryStorage;
  private final HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf;
  private final DefaultEmailResourceResolver resourceResolver;
  private final String accountCreatedWithoutPasswordMailSubject;
  private final String accountCreatedWithPasswordMailSubject;

  @Inject
  public CreationNotificationSender(
      @Named("che.api") String apiEndpoint,
      @Named("mailsender.application.from.email.address") String mailFrom,
      RecoveryStorage recoveryStorage,
      MailSender mailSender,
      HTMLTemplateProcessor<ThymeleafTemplate> thymeleaf,
      DefaultEmailResourceResolver resourceResolver,
      @Named("account.created.byuser.mail.subject") String accountCreatedWithoutPasswordMailSubject,
      @Named("account.created.byadmin.mail.subject") String accountCreatedWithPasswordMailSubject) {
    this.apiEndpoint = apiEndpoint;
    this.mailFrom = mailFrom;
    this.recoveryStorage = recoveryStorage;
    this.mailSender = mailSender;
    this.thymeleaf = thymeleaf;
    this.resourceResolver = resourceResolver;
    this.accountCreatedWithoutPasswordMailSubject = accountCreatedWithoutPasswordMailSubject;
    this.accountCreatedWithPasswordMailSubject = accountCreatedWithPasswordMailSubject;
  }

  public void sendNotification(String userName, String userEmail, boolean withPassword)
      throws IOException, ApiException {
    final URL urlEndpoint = new URL(apiEndpoint);
    final String masterEndpoint = urlEndpoint.getProtocol() + "://" + urlEndpoint.getHost();
    final ThymeleafTemplate template =
        withPassword
            ? templateWithPassword(masterEndpoint, userEmail, userName)
            : templateWithoutPassword(masterEndpoint, userName);
    final EmailBean emailBean =
        new EmailBean()
            .withBody(thymeleaf.process(template))
            .withFrom(mailFrom)
            .withTo(userEmail)
            .withReplyTo(null)
            .withMimeType(TEXT_HTML);
    if (withPassword) {
      emailBean.setSubject(accountCreatedWithPasswordMailSubject);
    } else {
      emailBean.setSubject(accountCreatedWithoutPasswordMailSubject);
    }

    mailSender.sendMail(resourceResolver.resolve(emailBean));
  }

  private ThymeleafTemplate templateWithPassword(
      String masterEndpoint, String userEmail, String userName) {
    final String uuid = recoveryStorage.generateRecoverToken(userEmail);
    final String resetPasswordLink =
        UriBuilder.fromUri(masterEndpoint)
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
