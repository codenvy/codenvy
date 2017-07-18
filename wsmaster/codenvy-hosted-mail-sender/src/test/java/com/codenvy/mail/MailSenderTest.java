/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.mail;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import org.eclipse.che.api.core.ApiException;
import org.everrest.assured.EverrestJetty;
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
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collections;
import java.util.Random;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class MailSenderTest {
    private final Random portRandomizer = new Random();
    private Session          mailSession;
    private MailSender       mailSender;
    private File             testConfig;
    private SimpleSmtpServer server;

    public static void assertMail(SimpleSmtpServer server, String from, String to, String replyTo, String subject,
                                  String mimeType, String body, String attachmentContentID, String attachmentFileName) {
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
        String testConfigContent =
                Resources.toString(Resources.getResource("mail-configuration.properties"), Charset.defaultCharset())
                         .replace("mail.smtp.port=9000", "mail.smtp.port=" + server.getPort());
        testConfig = File.createTempFile("mail-config", "properties");
        testConfig.deleteOnExit();
        Files.append(testConfigContent, testConfig, Charset.defaultCharset());


        mailSender = new MailSender(new SessionHolder(testConfig.getAbsolutePath()));
    }

    @AfterMethod
    public void stop() {
        server.stop();
        testConfig.delete();
    }

    @Test
    public void shouldBeAbleToSendMessage() throws IOException, MessagingException, ApiException {
        EmailBean emailBean = new EmailBean()
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

        mailSender.sendMail("noreply@cloud-ide.com",
                            "dev-test@cloud-ide.com",
                            "dev-test@cloud-ide.com",
                            "Subject",
                            "text/html",
                            "hello ${username} user", Collections.singletonMap("username", "Dead Moroz"));

        assertMail(server,
                   "noreply@cloud-ide.com",
                   "dev-test@cloud-ide.com",
                   "dev-test@cloud-ide.com",
                   "Subject",
                   "text/html",
                   "hello Dead Moroz user",
                   null,
                   null);
    }

    @Test
    public void shouldBeAbleToSendMessageWithFormattedFields() throws Exception {
        EmailBean emailBean = new EmailBean()
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
        EmailBean emailBean = new EmailBean()
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
        EmailBean emailBean = new EmailBean()
                .withFrom("noreply@cloud-ide.com")
                .withTo("dev-test@cloud-ide.com")
                .withReplyTo("dev-test@cloud-ide.com")
                .withSubject("Subject")
                .withMimeType("text/html")
                .withBody("hello user");

        Attachment attachment = new Attachment()
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
