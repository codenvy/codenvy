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
package com.codenvy.api.license.model;

/**
 * Request to accept Codenvy Fair Source License.
 * It contains user identification fields.
 *
 * @author Anatolii Bazko
 */
public interface FairSourceLicenseAcceptance {
    /**
     * Returns the user's first name who accepted FSL.
     */
    String getFirstName();

    /**
     * Returns the user's last name who accepted FSL.
     */
    String getLastName();

    /**
     * Returns the user's email who accepted FSL.
     */
    String getEmail();
}
