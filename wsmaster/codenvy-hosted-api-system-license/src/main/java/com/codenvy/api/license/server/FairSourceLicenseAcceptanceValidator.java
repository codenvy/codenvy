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
package com.codenvy.api.license.server;

import com.codenvy.api.license.shared.model.FairSourceLicenseAcceptance;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.BadRequestException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class FairSourceLicenseAcceptanceValidator {

    /**
     * Validates if all fields are correct.
     *
     * @throws BadRequestException
     */
    public void validate(FairSourceLicenseAcceptance request) throws BadRequestException {
        String email = request.getEmail();

        if (isNullOrEmpty(email)
            || isNullOrEmpty(request.getFirstName())
            || isNullOrEmpty(request.getLastName())) {

            throw new BadRequestException("Codenvy Fair Source License can't be accepted until all fields are filled.");
        }

        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
        } catch (AddressException e) {
            throw new BadRequestException(format("Codenvy Fair Source License can't be accepted. Email %s is not valid", email));
        }
    }
}
