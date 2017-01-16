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
package com.codenvy.organization.api;

import com.codenvy.organization.shared.model.Organization;

import org.eclipse.che.account.spi.AccountValidator;
import org.eclipse.che.api.core.BadRequestException;

import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Utils for organization validation.
 *
 * @author Sergii Leschenko
 */
public class OrganizationValidator {
    @Inject
    private AccountValidator accountValidator;

    /**
     * Checks whether given organization is valid.
     *
     * @param organization
     *         organization to check
     * @throws BadRequestException
     *         when organization is not valid
     */
    public void checkOrganization(Organization organization) throws BadRequestException {
        if (organization == null) {
            throw new BadRequestException("Organization required");
        }
        if (isNullOrEmpty(organization.getName())) {
            throw new BadRequestException("Organization name required");
        }
        if (!accountValidator.isValidName(organization.getName())) {
            throw new BadRequestException("Organization name must contain only letters and digits");
        }
    }
}
