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
package com.codenvy.api.license.shared.dto;


import com.codenvy.api.license.shared.model.FairSourceLicenseAcceptance;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Anatolii Bazko
 */
@DTO
public interface FairSourceLicenseAcceptanceDto extends FairSourceLicenseAcceptance {
    @Override
    String getFirstName();

    void setFirstName(String firstName);

    FairSourceLicenseAcceptanceDto withFirstName(String firstName);

    @Override
    String getLastName();

    void setLastName(String lastName);

    FairSourceLicenseAcceptanceDto withLastName(String lastName);

    @Override
    String getEmail();

    void setEmail(String email);

    FairSourceLicenseAcceptanceDto withEmail(String email);
}
