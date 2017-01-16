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
package com.codenvy.mail;

import com.codenvy.mail.shared.dto.EmailBeanDto;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.lang.Deserializer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Map;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/** Client for MailSender service */
public class MailSenderClient {
    private static final String MAILSENDER_APPLICATION_SERVER_URL = "mailsender.application.server.url";

    /** Base path */
    public static final String BASE_URL = "mail/";

    /** String representation of MailSender Service application address */
    private String server;

    @Inject
    private HttpJsonRequestFactory httpJsonRequestFactory;

    /**
     * Simple constructor to pass application server address
     *
     * @param server
     */
    @Inject
    public MailSenderClient(@Named(MAILSENDER_APPLICATION_SERVER_URL) String server) {
        this.server = server;
    }

    public void sendMail(String from, String to, String replyTo, String subject, String mimeType,
                         String template) throws IOException, MessagingException, ApiException {
        sendMail(from, to, replyTo, subject, mimeType, template, null);
    }

    public void sendMail(String from, String to, String replyTo, String subject, String mimeType, String template,
                         Map<String, String> templateProperties) throws MessagingException, IOException, ApiException {
        EmailBeanDto emailBeanDto = newDto(EmailBeanDto.class)
                .withBody(templateProperties == null ? template : Deserializer.resolveVariables(template, templateProperties))
                .withFrom(from)
                .withTo(to)
                .withReplyTo(replyTo)
                .withSubject(subject)
                .withMimeType(mimeType);

        sendMail(emailBeanDto);
    }

    public void sendMail (EmailBeanDto emailBeanDto)
            throws ApiException, IOException {
        httpJsonRequestFactory.fromUrl(server + BASE_URL + "send").usePostMethod().setBody(emailBeanDto).request();
    }
}
