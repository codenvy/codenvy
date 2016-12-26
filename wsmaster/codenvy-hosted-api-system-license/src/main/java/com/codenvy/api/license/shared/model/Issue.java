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
package com.codenvy.api.license.shared.model;

/**
 * Describes issue with license.
 *
 * @author Dmytro Nochevnov
 */
public interface Issue {

    enum Status {
        USER_LICENSE_HAS_REACHED_ITS_LIMIT,
        FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED,
        LICENSE_EXPIRING,
        LICENSE_EXPIRED
    }

    /**
     * Get status of issue.
     */
    Status getStatus();


    /**
     * Get message of issue.
     */
    String getMessage();
}
