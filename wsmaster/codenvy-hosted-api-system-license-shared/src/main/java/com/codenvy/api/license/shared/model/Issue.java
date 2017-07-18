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
