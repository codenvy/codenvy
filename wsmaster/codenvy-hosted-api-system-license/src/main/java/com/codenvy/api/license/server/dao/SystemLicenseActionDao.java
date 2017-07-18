/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.api.license.server.dao;

import com.codenvy.api.license.server.model.impl.SystemLicenseActionImpl;
import com.codenvy.api.license.shared.model.Constants;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

/**
 * Defines data access object contract for {@link SystemLicenseActionImpl}.
 *
 * @author Anatolii Bazko
 */
public interface SystemLicenseActionDao {

    /**
     * Inserts license action record.
     *
     * @param systemLicenseAction
     *      any license action
     * @throws ConflictException
     *      if action already exists in the system
     * @throws ServerException
     *      any other error occurred
     */
    void insert(SystemLicenseActionImpl systemLicenseAction) throws ServerException, ConflictException;

    /**
     * Inserts new license action record, or updates existed one.
     *
     * @param systemLicenseAction
     *      any license action
     * @throws ServerException
     *      any other error occurred
     */
    void upsert(SystemLicenseActionImpl systemLicenseAction) throws ServerException, ConflictException;

    /**
     * Removes system license action record. Don't throw NotFoundException if record doesn't exist.
     *
     * @param licenseType
     *          the type of the license
     * @param actionType
     *          the action happened with license
     * @throws ServerException
     *      any other error occurred
     */
    void remove(Constants.PaidLicense licenseType, Constants.Action actionType) throws ServerException;

    /**
     * Removes action record of system license with certain license id. Don't throw NotFoundException if record doesn't exist.
     *
     * @param licenseId
     *          the id of the license
     * @param actionType
     *          the action happened with license
     * @throws ServerException
     *      any other error occurred
     */
    void remove(String licenseId, Constants.Action actionType) throws ServerException;

    /**
     * Finds license action for certain license type.
     *
     * @param licenseType
     *          the type of the license
     * @param actionType
     *          the action happened with license
     * @return {@link SystemLicenseActionImpl}
     * @throws NotFoundException
     *      no license action found
     * @throws ServerException
     *      any other error occurred
     */
    SystemLicenseActionImpl getByLicenseTypeAndAction(Constants.PaidLicense licenseType, Constants.Action actionType) throws ServerException,
                                                                                                                             NotFoundException;

    /**
     * Finds license action for certain license id.
     *
     * @param licenseId
     *          the id of the license
     * @param actionType
     *          the action happened with license
     * @return {@link SystemLicenseActionImpl}
     * @throws NotFoundException
     *      no license action found
     * @throws ServerException
     *      any other error occurred
     */
     SystemLicenseActionImpl getByLicenseIdAndAction(String licenseId, Constants.Action actionType) throws ServerException,
                                                                                                           NotFoundException;

}
