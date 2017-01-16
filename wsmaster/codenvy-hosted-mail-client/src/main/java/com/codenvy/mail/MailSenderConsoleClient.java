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

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.commons.lang.IoUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.eclipse.che.commons.lang.Deserializer.resolveVariables;

public class MailSenderConsoleClient {
    private static final Logger LOG = LoggerFactory.getLogger(MailSenderConsoleClient.class);

    private MailSenderConsoleClient() {
    }

    /** Entry point for the application */
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new RuntimeException("Usage: [path to conf file]");
        }

        File configuration = new File(args[0]);
        if (configuration.isDirectory() || !configuration.exists()) {
            throw new RuntimeException("Configuration file" + configuration.getAbsolutePath() + " is a directory or " +
                                       "doesn't exist");
        }

        InputStream is = null;

        try {
            is = new FileInputStream(configuration);

            Properties props = new Properties();
            props.load(is);

            String server = props.getProperty("sendemails.server");
            String from = props.getProperty("sendemails.from");
            String subject = props.getProperty("sendemails.subject");
            String mimeType = props.getProperty("sendemails.mimetype");
            String replyTo = props.getProperty("sendemails.replyto");
            String templateName = props.getProperty("sendemails.file.template");
            String recipientsFileName = props.getProperty("sendemails.file.to");

            String template = getTemplate(templateName);

            sendMails(server, from, recipientsFileName, replyTo, subject, mimeType, template);

        } catch (ApiException | IOException | MessagingException | InterruptedException e) {
            LOG.error("Mails sending failed - {}", e.getLocalizedMessage(), e);
            throw new RuntimeException(e.getLocalizedMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.error("Mails sending failed - {}", e.getLocalizedMessage(), e);
                    throw new RuntimeException(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private static void sendMails(String server, String from, String recipientsFileName, String replyTo, String subject,
                                  String mimeType,
                                  String template) throws MessagingException, IOException, InterruptedException, ApiException {
        MailSenderClient mailService = new MailSenderClient(server);

        Map<String, String> templateVariables = new HashMap<String, String>();

        FileInputStream fis = null;
        DataInputStream in = null;
        try {
            fis = new FileInputStream(recipientsFileName);
            in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;

            while ((strLine = br.readLine()) != null) {
                templateVariables.put("user.email", strLine);

                mailService.sendMail(from, strLine, null, subject, mimeType, resolveVariables(template, templateVariables));

                LOG.info("Mail to {} was sent.", strLine);

                TimeUnit.SECONDS.sleep(1);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private static String getTemplate(String templateName) throws IOException {
        if (templateName == null) {
            throw new IOException("Template name is null.");
        }

        InputStream templateInputStream = null;
        try {
            File templateFile = new File(templateName);

            if (templateFile.exists() && templateFile.isFile()) {
                templateInputStream = new FileInputStream(templateName);
            }

            if (templateInputStream == null) {
                throw new IOException("Not found template file: " + templateName);
            }

            return IoUtil.readStream(templateInputStream);
        } finally {
            if (templateInputStream != null) {
                templateInputStream.close();
            }
        }
    }
}
