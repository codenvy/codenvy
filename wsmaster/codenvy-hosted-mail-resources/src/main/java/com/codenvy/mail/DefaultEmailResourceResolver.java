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
package com.codenvy.mail;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.mail.Attachment;
import org.eclipse.che.mail.EmailBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attaches to the e-mail message, Codenvy common resources such as logos, style sheets, fonts and
 * etc.
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
          attachments.add(
              new Attachment()
                  .withContent(encodedImg)
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
