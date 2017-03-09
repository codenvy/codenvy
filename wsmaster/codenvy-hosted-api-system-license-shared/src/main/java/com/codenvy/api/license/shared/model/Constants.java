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
package com.codenvy.api.license.shared.model;

/**
 * @author Anatolii Bazko
 */
public class Constants {

    public static final String LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE_FOR_REGISTRATION =
            "Your user license has reached its limit. You cannot add more users.";

    public static final String LICENSE_HAS_REACHED_ITS_USER_LIMIT_MESSAGE_FOR_WORKSPACE =
            "The Codenvy license has reached its user limit - "
            + "you can access the user dashboard but not the IDE.";

    public static final String UNABLE_TO_ADD_ACCOUNT_BECAUSE_OF_LICENSE =
            "Unable to add your account. The Codenvy license has reached its user limit.";

    public static final String FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_MESSAGE =
            "Your admin has not accepted the license agreement.";

    public static final String LICENSE_EXPIRING_MESSAGE_TEMPLATE =
            "License expired. Codenvy will downgrade to a %s user Fair Source license in %s days.";

    public static final String LICENSE_COMPLETELY_EXPIRED_MESSAGE_FOR_ADMIN_TEMPLATE =
            "There are currently %s users registered in Codenvy but your license only allows %s. "
            + "Users cannot start workspaces.";

    public static final String LICENSE_COMPLETELY_EXPIRED_MESSAGE_FOR_NON_ADMIN =
            "The Codenvy license is expired - you can access the user dashboard but not the IDE.";

    public static final char[] PRODUCT_ID = "OPL-STN-SM".toCharArray();

    /**
     * System license actions.
     */
    public enum Action {
        ACCEPTED,
        ADDED,
        EXPIRED,
        REMOVED
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
