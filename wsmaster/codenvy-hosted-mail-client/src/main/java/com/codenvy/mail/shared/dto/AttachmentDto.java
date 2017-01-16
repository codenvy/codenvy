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

/**
 * Dto for describing e-mail attachment.
 *
 * @author Igor Vinokur
 */
@DTO
public interface AttachmentDto {

    /**
     * Base-64 encoded string that represents attachment content.
     */
    String getContent();

    void setContent(String content);

    AttachmentDto withContent(String content);

    /**
     * Content Id.
     */
    String getContentId();

    void setContentId(String contentId);

    AttachmentDto withContentId(String contentId);

    /**
     * Name of the file.
     */
    String getFileName();

    void setFileName(String fileName);

    AttachmentDto withFileName(String fileName);
}
