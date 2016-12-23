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
package com.codenvy.resource.api;

import com.codenvy.resource.model.Resource;
import com.codenvy.resource.spi.impl.ResourceImpl;

import org.eclipse.che.api.core.ServerException;

import java.util.List;

/**
 * Provides information about resources which are not available for usage by account owner.
 *
 * <p>It can be used for example for implementing resources redistribution between accounts or
 * resources usage limitation when limit should be less than account's license has.
 *
 * @author Sergii Leschenko
 */
public interface ResourcesReserveTracker {
    /**
     * Returns resources that are not available for usage by account with specified id.
     *
     * @param accountId
     *         account identifier
     * @return resources that are not available for usage by account with specified id.
     * @throws ServerException
     *         when some exception occurs
     */
    List<? extends Resource> getReservedResources(String accountId) throws ServerException;

    /**
     * Returns account type for which this class tracks resources reserve.
     */
    String getAccountType();
}
