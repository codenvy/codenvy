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

import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Attaches to the e-mail message, Codenvy common resources such as logos, style sheets, fonts and etc.
 *
 * @author Anton Korneta
 */
@Singleton
public class DefaultEmailResourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultEmailResourceResolver.class);

    private final Map<String, String> logos;

    @Inject
    public DefaultEmailResourceResolver(@Named("codenvy.email.logos") Map<String, String> logos) {
        this.logos = logos;
    }

    public EmailBean resolve(EmailBean emailBean) {
        final List<Attachment> attachments = new ArrayList<>(logos.size());
        for (Map.Entry<String, String> entry : logos.entrySet()) {
            final URL resource = this.getClass().getResource(entry.getValue());
            if (resource != null) {
                final File logo = new File(resource.getPath());
                try {
                    final String encodedImg = Base64.getEncoder().encodeToString(Files.toByteArray(logo));
                    attachments.add(new Attachment().withContent(encodedImg)
                                                    .withContentId(entry.getKey())
                                                    .withFileName(entry.getKey()));
                } catch (IOException ex) {
                    LOG.warn("Failed to attach default logos for email bean cause {}", ex.getCause());
                }
            }
        }
        return emailBean.withAttachments(attachments);
    }

}
