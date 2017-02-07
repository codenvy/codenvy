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

import java.util.Objects;

/**
 * Describing e-mail attachment.
 *
 * @author Igor Vinokur
 * @author Alexander Garagatyi
 */
public class Attachment {
    private String content;
    private String contentId;
    private String fileName;

    /**
     * Base-64 encoded string that represents attachment content.
     */
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Attachment withContent(String content) {
        this.content = content;
        return this;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public Attachment withContentId(String contentId) {
        this.contentId = contentId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Attachment withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attachment)) return false;
        Attachment that = (Attachment)o;
        return Objects.equals(getContent(), that.getContent()) &&
               Objects.equals(getContentId(), that.getContentId()) &&
               Objects.equals(getFileName(), that.getFileName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContent(), getContentId(), getFileName());
    }

    @Override
    public String toString() {
        return "Attachment{" +
               "content='" + content + '\'' +
               ", contentId='" + contentId + '\'' +
               ", fileName='" + fileName + '\'' +
               '}';
    }
}
