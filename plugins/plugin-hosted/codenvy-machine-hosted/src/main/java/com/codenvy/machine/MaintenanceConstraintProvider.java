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
package com.codenvy.machine;

import com.google.inject.Provider;

/**
 * Provides constraint for images and containers for prevent actions with them on node under maintenance.
 *
 * @author Mykola Morhun
 */
public class MaintenanceConstraintProvider implements Provider<String> {
    public static final String MAINTENANCE_CONSTRAINT_KEY = "constraint:com.codenvy.node-state!";
    public static final String MAINTENANCE_CONSTRAINT_VALUE = "maintenance";
    /**
     * This constraint
     * <i>constraint:com.codenvy.node-state!=maintenance</i>
     * prevent build / start machine on a node with maintenance status
     */
    public static final String MAINTENANCE_CONSTRAINT = MAINTENANCE_CONSTRAINT_KEY + '=' + MAINTENANCE_CONSTRAINT_VALUE;

    @Override
    public String get() {
        return MAINTENANCE_CONSTRAINT;
    }

}
