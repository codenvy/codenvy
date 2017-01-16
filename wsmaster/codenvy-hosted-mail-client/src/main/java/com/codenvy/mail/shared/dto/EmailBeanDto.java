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
package com.codenvy.mail.shared.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Dto for describing e-mail properties.
 *
 * @author Igor Vinokur
 */
@DTO
public interface EmailBeanDto {

    /**
     * Sender e-mail address.
     */
    String getFrom();

    void setFrom(String from);

    EmailBeanDto withFrom(String from);

    /**
     * Receiver e-mail address.
     */
    String getTo();

    void setTo(String to);

    EmailBeanDto withTo(String to);

    /**
     * Copy of e-mail receiver address.
     */
    String getReplyTo();

    void setReplyTo(String replyTo);

    EmailBeanDto withReplyTo(String replyTo);

    /**
     * MIME type of e-mail body.
     */
    String getMimeType();

    void setMimeType(String mimeType);

    EmailBeanDto withMimeType(String mimeType);

    /**
     * Body content of e-mail.
     */
    String getBody();

    void setBody(String body);

    EmailBeanDto withBody(String body);

    /**
     * Subject of e-mail.
     */
    String getSubject();

    void setSubject(String subject);

    EmailBeanDto withSubject(String subject);

    /**
     * Attachments of e-mail.
     */
    List<AttachmentDto> getAttachments();

    void setAttachments(List<AttachmentDto> attachments);

    EmailBeanDto withAttachments(List<AttachmentDto> attachments);
}
