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
