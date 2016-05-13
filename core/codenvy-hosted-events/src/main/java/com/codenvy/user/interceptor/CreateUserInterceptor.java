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
package com.codenvy.user.interceptor;

import com.codenvy.mail.MailSenderClient;
import com.codenvy.mail.shared.dto.AttachmentDto;
import com.codenvy.mail.shared.dto.EmailBeanDto;
import com.codenvy.service.password.RecoveryStorage;
import com.google.common.io.Files;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Deserializer;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
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
 * Intercepts {@link UserService} methods.
 * <p>
 * The purpose of the interceptor is to send "welcome to codenvy" email to user after its creation.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class CreateUserInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserInterceptor.class);

    private static final String EMAIL_TEMPLATE_USER_CREATED_WITH_PASSWORD    = "email-templates/user_created_with_password.html";
    private static final String EMAIL_TEMPLATE_USER_CREATED_WITHOUT_PASSWORD = "email-templates/user_created_without_password.html";
    private static final String LOGO                                         = "/email-templates/header.png";
    private static final String LOGO_CID                                     = "codenvyLogo";

    @Inject
    private MailSenderClient mailSenderClient;

    @Inject
    private RecoveryStorage recoveryStorage;

    @Inject
    @Named("api.endpoint")
    private String apiEndpoint;

    //Do not remove ApiException. It used to tell dependency plugin that api-core is need not only for tests.
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable, ApiException {
        invocation.proceed();

        try {
            User user = (User)invocation.getArguments()[0];

            String userEmail = user.getEmail();
            URL urlEndpoint = new URL(apiEndpoint);
            String template = isUserCreatedByAdmin() ? EMAIL_TEMPLATE_USER_CREATED_WITH_PASSWORD
                                                     : EMAIL_TEMPLATE_USER_CREATED_WITHOUT_PASSWORD;

            String uuid = recoveryStorage.generateRecoverToken(user.getEmail());

            UriBuilder resetPasswordLinkUriBuilder = UriBuilder.fromUri(urlEndpoint.getProtocol() + "://" + urlEndpoint.getHost())
                                                               .path("site/setup-password")
                                                               .queryParam("id", uuid);
            String resetPasswordLink = resetPasswordLinkUriBuilder.build(user.getEmail()).toString();

            Map<String, String> properties = new HashMap<>();
            properties.put("logo.cid", "codenvyLogo");
            properties.put("user.name", user.getName());
            properties.put("user.mail", user.getEmail());
            properties.put("setup.password.link", resetPasswordLink);
            properties.put("com.codenvy.masterhost.url", urlEndpoint.getProtocol() + "://" + urlEndpoint.getHost());

            File logo = new File(this.getClass().getResource(LOGO).getPath());

            AttachmentDto attachmentDto = newDto(AttachmentDto.class)
                    .withContent(Base64.getEncoder().encodeToString(Files.toByteArray(logo)))
                    .withContentId(LOGO_CID)
                    .withFileName("logo.png");

            EmailBeanDto emailBeanDto = newDto(EmailBeanDto.class)
                    .withBody(Deserializer.resolveVariables(readAndCloseQuietly(getResource("/" + template)), properties))
                    .withFrom("Codenvy <noreply@codenvy.com>")
                    .withTo(userEmail)
                    .withReplyTo(null)
                    .withSubject("Welcome To Codenvy")
                    .withMimeType(TEXT_HTML)
                    .withAttachments(Collections.singletonList(attachmentDto));

            mailSenderClient.sendMail(emailBeanDto);

            LOG.info("User created message send to {}", userEmail);
        } catch (Exception e) {
            LOG.warn("Unable to send creation notification email", e);
        }

        return Void.TYPE;
    }

    protected boolean isUserCreatedByAdmin() {
        EnvironmentContext context = EnvironmentContext.getCurrent();
        Subject contextSubject = context.getSubject();
        return contextSubject != null && (contextSubject.isMemberOf("system/admin") || contextSubject.isMemberOf("system/manager"));
    }
}
