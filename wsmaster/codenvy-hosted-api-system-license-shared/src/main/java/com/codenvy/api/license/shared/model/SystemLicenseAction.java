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

import java.util.Map;

/**
 * Identifiers any license action happened in the system.
 *
 * @author Anatolii Bazko
 */
public interface SystemLicenseAction {
    /**
     * Returns system license type.
     */
    Constants.PaidLicense getLicenseType();

    /**
     * Returns Codenvy action type. It explains what happened with a license.
     */
    Constants.Action getActionType();

    /**
     * Returns time when action is happened.
     */
    long getActionTimestamp();

    /**
     * License ID.
     */
    String getLicenseId();

    /**
     * Returns any action attributes.
     */
    Map<String, String> getAttributes();
}
