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

import java.util.List;
import java.util.Objects;

/**
 * Describing e-mail properties.
 *
 * @author Igor Vinokur
 * @author Alexander Garagatyi
 */
public class EmailBean {
    private String           from;
    private String           to;
    private String           replyTo;
    private String           mimeType;
    private String           body;
    private String           subject;
    private List<Attachment> attachments;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public EmailBean withFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public EmailBean withTo(String to) {
        this.to = to;
        return this;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public EmailBean withReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public EmailBean withMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public EmailBean withBody(String body) {
        this.body = body;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public EmailBean withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public EmailBean withAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailBean)) return false;
        EmailBean emailBean = (EmailBean)o;
        return Objects.equals(getFrom(), emailBean.getFrom()) &&
               Objects.equals(getTo(), emailBean.getTo()) &&
               Objects.equals(getReplyTo(), emailBean.getReplyTo()) &&
               Objects.equals(getMimeType(), emailBean.getMimeType()) &&
               Objects.equals(getBody(), emailBean.getBody()) &&
               Objects.equals(getSubject(), emailBean.getSubject()) &&
               Objects.equals(getAttachments(), emailBean.getAttachments());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFrom(), getTo(), getReplyTo(), getMimeType(), getBody(), getSubject(), getAttachments());
    }

    @Override
    public String toString() {
        return "EmailBean{" +
               "from='" + from + '\'' +
               ", to='" + to + '\'' +
               ", replyTo='" + replyTo + '\'' +
               ", mimeType='" + mimeType + '\'' +
               ", body='" + body + '\'' +
               ", subject='" + subject + '\'' +
               ", attachments=" + attachments +
               '}';
    }
}
