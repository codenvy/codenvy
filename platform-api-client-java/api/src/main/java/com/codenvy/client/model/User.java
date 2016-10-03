/*******************************************************************************
 * Copyright (c) [2012] - [2016] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client.model;

/**
 * This class represents the user resource on Codenvy.
 *
 * @author Florent Benoit
 */
public interface User {

    /**
     * @return ID of this user.
     */
    String id();

    /**
     * @return e-mail of this user.
     */
    String email();

}
