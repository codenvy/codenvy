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
 * @author Anatolii Bazko
 */
public class Constants {

    public static final char[] PRODUCT_ID = "OPL-STN-SM".toCharArray();

    /**
     * System license actions.
     */
    public enum Action {
        ACCEPTED,
        ADDED,
        EXPIRED
    }

    /**
     * Paid system license types.
     */
    public enum PaidLicense {
        FAIR_SOURCE_LICENSE,
        PRODUCT_LICENSE
    }

    private Constants() { }
}
