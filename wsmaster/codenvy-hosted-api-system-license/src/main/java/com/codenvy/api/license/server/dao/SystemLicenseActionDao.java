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

    void upsert(SystemLicenseActionImpl systemLicenseAction) throws ServerException, ConflictException;

    /**
     * Removes system license action record.
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
