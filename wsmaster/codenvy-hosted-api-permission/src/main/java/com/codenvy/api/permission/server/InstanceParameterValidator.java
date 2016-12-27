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
package com.codenvy.api.permission.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;

import javax.inject.Inject;

/**
 * Validates that provided instance parameter is valid
 *
 * @author Sergii Leschenko
 */
public class InstanceParameterValidator {
    private final PermissionsManager permissionsManager;

    @Inject
    public InstanceParameterValidator(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    /**
     * Validates that provided instance parameter is valid for specified domain
     *
     * @param domain
     *         the domain of specified {@code instance}
     * @param instance
     *         the instance to check
     * @throws BadRequestException
     *         if specified {@code instance} is not valid
     * @throws NotFoundException
     *         if specified {@code domain} is unsupported
     */
    public void validate(String domain, String instance) throws BadRequestException, NotFoundException {
        if (permissionsManager.getDomain(domain).isInstanceRequired() && instance == null) {
            throw new BadRequestException("Specified domain requires non nullable value for instance");
        }
    }
}
