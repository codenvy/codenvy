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
package com.codenvy.api.license.server.shared.model;

/**
 * Describes issue with license.
 *
 * @author Dmytro Nochevnov
 */
public interface Issue {

    enum Status {
        USER_LICENSE_HAS_REACHED_ITS_LIMIT
    }

    /**
     * Check if license is legal.
     */
    Status getStatus();


    /**
     * Check if license is legal.
     */
    String getMessage();
}
