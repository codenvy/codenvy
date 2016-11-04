/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.license.server.shared.dto;

import com.codenvy.api.license.server.shared.model.Legality;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Dmytro Nochevnov
 */
@DTO
public interface LegalityDto extends Legality {

    @Override
    boolean getIsLegal();

    void setIsLegal(boolean isLegal);

    LegalityDto withIsLegal(boolean isLegal);

    @Override
    List<IssueDto> getIssues();

    void setIssues(List<IssueDto> issues);

    LegalityDto withIssues(List<IssueDto> issues);
}
