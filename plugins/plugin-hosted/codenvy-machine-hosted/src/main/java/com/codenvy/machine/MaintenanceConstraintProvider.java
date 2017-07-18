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
