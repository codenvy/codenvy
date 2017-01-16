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

import com.codenvy.mail.shared.dto.AttachmentDto;
import com.codenvy.mail.shared.dto.EmailBeanDto;
import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.everrest.assured.EverrestJetty;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import javax.mail.Session;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collections;
import java.util.Random;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class MailSenderTest {
    private final Random portRandomizer = new Random();
    private Session          mailSession;
    private MailSender       mailSender;
    private MailSenderClient mailSenderClient;
    private File             testConfig;

    @Mock
    private HttpJsonResponse       httpJsonResponse;
    @Mock
    private HttpJsonRequest        httpJsonRequest;
    @Mock
    private HttpJsonRequestFactory httpJsonRequestFactory;

    private SimpleSmtpServer server;

    public static void assertMail(SimpleSmtpServer server, String from, String to, String replyTo, String subject, String mimeType,
                                  String body, String attachmentContentID, String attachmentFileName) {
        assertEquals(server.getReceivedEmails().size(), 1);
        SmtpMessage email = server.getReceivedEmails().iterator().next();

        assertEquals(email.getHeaderValue("Subject"), subject);
        assertEquals(email.getHeaderValue("From"), from);
        assertEquals(email.getHeaderValue("Reply-To"), replyTo);
        assertEquals(email.getHeaderValue("To"), to);
        assertTrue(email.getBody().contains("Content-Type: " + mimeType));
        assertTrue(email.getBody().contains(body));
        if (attachmentFileName != null && attachmentContentID != null) {
            assertTrue(email.getBody().contains("filename=" + attachmentFileName));
            assertTrue(email.getBody().contains("Content-ID: <" + attachmentContentID + ">"));
        }
    }

    @BeforeMethod
    public void setup(ITestContext context) throws IOException {
        //      mailSender = new MailSender("/mail-configuration.properties");
        server = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT);
        String testConfigContent = Resources.toString(Resources.getResource("mail-configuration.properties"), Charset.defaultCharset())
                                            .replace("mail.smtp.port=9000", "mail.smtp.port=" + server.getPort());
        testConfig = File.createTempFile("mail-config", "properties");
        testConfig.deleteOnExit();
        Files.append(testConfigContent, testConfig, Charset.defaultCharset());


        mailSender = new MailSender(new SessionHolder(testConfig.getAbsolutePath()));
        mailSenderClient = new MailSenderClient("http://localhost:" + context.getAttribute(EverrestJetty.JETTY_PORT) + "/rest/");
    }

    @AfterMethod
    public void stop() {
        server.stop();
        testConfig.delete();
    }

    @Test
    public void shouldBeAbleToSendMessage() throws IOException, MessagingException, ApiException {
        EmailBeanDto emailBean = newDto(EmailBeanDto.class)
                .withFrom("noreply@cloud-ide.com")
                .withTo("dev-test@cloud-ide.com")
                .withReplyTo("dev-test@cloud-ide.com")
                .withSubject("Subject")
                .withMimeType("text/html")
                .withBody("hello user");
        mailSender.sendMail(emailBean);
        assertMail(server, "noreply@cloud-ide.com",
                   "dev-test@cloud-ide.com",
                   "dev-test@cloud-ide.com",
                   "Subject",
                   "text/html",
                   "hello user",
                   null,
                   null);


    }

    @Test
    public void shouldBeAbleToSendMessageWithReplacedVars()
            throws IOException, MessagingException, ApiException, NoSuchFieldException, IllegalAccessException {

        Field field = mailSenderClient.getClass().getDeclaredField("httpJsonRequestFactory");
        field.setAccessible(true);
        field.set(mailSenderClient, httpJsonRequestFactory);

        when(httpJsonRequestFactory.fromUrl(anyString())).thenReturn(httpJsonRequest);
        when(httpJsonRequest.usePostMethod()).thenReturn(httpJsonRequest);
        when(httpJsonRequest.setBody(any(EmailBeanDto.class))).thenReturn(httpJsonRequest);
        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);

        mailSenderClient.sendMail("noreply@cloud-ide.com",
                                  "dev-test@cloud-ide.com",
                                  "dev-test@cloud-ide.com",
                                  "Subject",
                                  "text/html",
                                  "hello ${username} user", Collections.singletonMap("username", "Dead Moroz"));

        ArgumentCaptor<EmailBeanDto> argument = ArgumentCaptor.forClass(EmailBeanDto.class);
        verify(httpJsonRequest).setBody(argument.capture());
        assertEquals(argument.getValue().getFrom(), "noreply@cloud-ide.com");
        assertEquals(argument.getValue().getTo(), "dev-test@cloud-ide.com");
        assertEquals(argument.getValue().getReplyTo(), "dev-test@cloud-ide.com");
        assertEquals(argument.getValue().getSubject(), "Subject");
        assertEquals(argument.getValue().getBody(), "hello Dead Moroz user");
    }

    @Test
    public void shouldBeAbleToSendMessageWithFormattedFields() throws Exception {
        EmailBeanDto emailBean = newDto(EmailBeanDto.class)
                .withFrom("Exo IDE <noreply@cloud-ide.com>")
                .withTo("dev-test@cloud-ide.com")
                .withReplyTo("Developers to reply <dev-test@cloud-ide.com>")
                .withSubject("Subject")
                .withMimeType("text/html")
                .withBody("hello user");
        mailSender.sendMail(emailBean);
        assertMail(server,
                   "Exo IDE <noreply@cloud-ide.com>",
                   "dev-test@cloud-ide.com",
                   "Developers to reply <dev-test@cloud-ide.com>",
                   "Subject",
                   "text/html",
                   "hello user",
                   null,
                   null);
    }

    @Test
    public void shouldBeAbleToSendMessageToFewEmails() throws Exception {
        EmailBeanDto emailBean = newDto(EmailBeanDto.class)
                .withFrom("noreply@cloud-ide.com")
                .withTo("dev-test@cloud-ide.com, dev-test1@cloud-ide.com, dev-test2@cloud-ide.com")
                .withReplyTo("dev-test@cloud-ide.com")
                .withSubject("Subject")
                .withMimeType("text/html")
                .withBody("hello user");
        mailSender.sendMail(emailBean);

        assertMail(server, "noreply@cloud-ide.com",
                   "dev-test@cloud-ide.com, dev-test1@cloud-ide.com, dev-test2@cloud-ide.com",
                   "dev-test@cloud-ide.com",
                   "Subject",
                   "text/html",
                   "hello user",
                   null,
                   null);


    }

    @Test
    public void shouldBeAbleToSendMessageWithAttachment() throws Exception {
        EmailBeanDto emailBean = newDto(EmailBeanDto.class)
                .withFrom("noreply@cloud-ide.com")
                .withTo("dev-test@cloud-ide.com")
                .withReplyTo("dev-test@cloud-ide.com")
                .withSubject("Subject")
                .withMimeType("text/html")
                .withBody("hello user");

        AttachmentDto attachment = newDto(AttachmentDto.class)
                .withContentId("attachmentId")
                .withFileName("attachment.txt")
                .withContent(Base64.getEncoder().encodeToString("attachmentContent".getBytes()));

        emailBean.setAttachments(Collections.singletonList(attachment));

        mailSender.sendMail(emailBean);
        assertMail(server, "noreply@cloud-ide.com",
                   "dev-test@cloud-ide.com",
                   "dev-test@cloud-ide.com",
                   "Subject",
                   "text/html",
                   "hello user",
                   "attachmentId",
                   "attachment.txt");
    }

}
