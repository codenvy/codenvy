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
package org.eclipse.che.ide.ext.microsoft.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Mihail Kuznyetsov
 */
@DTO
public interface NewMicrosoftPullRequest {

    String getTitle();

    void setTitle(String title);

    NewMicrosoftPullRequest withTitle(String title);

    String getSourceRefName();

    void setSourceRefName(String sourceRefName);

    NewMicrosoftPullRequest withSourceRefName(String sourceRefName);

    String getTargetRefName();

    void setTargetRefName(String targetRefName);

    NewMicrosoftPullRequest withTargetRefName(String targetRefName);

    String getDescription();

    void setDescription(String description);

    NewMicrosoftPullRequest withDescription(String description);
}
