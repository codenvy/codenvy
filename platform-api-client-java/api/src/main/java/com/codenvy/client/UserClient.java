/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client;

import com.codenvy.client.model.User;

/**
 * The Codenvy user API client.
 *
 * @author Florent Benoit
 */
public interface UserClient {

    /**
     * Returns the current user.
     *
     * @return the current user.
     */
    Request<User> current();
}
