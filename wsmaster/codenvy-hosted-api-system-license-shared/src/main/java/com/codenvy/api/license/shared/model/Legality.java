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
package com.codenvy.api.license.shared.model;

import com.codenvy.api.license.shared.dto.IssueDto;

import java.util.List;

/**
 * Describes legality of usage of Codenvy according to actual license.
 *
 * @author Dmytro Nochevnov
 */
public interface Legality {

    /**
     * Check if Codenvy usage is legal.
     */
    boolean getIsLegal();

    /**
     * Get list of issues related to actual license.
     */
    List<IssueDto> getIssues();
}
