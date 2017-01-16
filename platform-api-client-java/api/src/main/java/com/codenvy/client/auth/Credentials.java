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
package com.codenvy.client.auth;

/**
 * User credentials used for authentication.
 *
 * @author Florent Benoit
 */
public interface Credentials {

    /**
     * @return username
     */
    String username();

    /**
     * @return password
     */
    String password();

    /**
     * @return the token
     */
    Token token();

    /**
     * @return true if store only token
     */
    boolean isStoreOnlyToken();

}
