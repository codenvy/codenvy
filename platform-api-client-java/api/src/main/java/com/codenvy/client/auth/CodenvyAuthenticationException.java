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

import com.codenvy.client.CodenvyException;

/**
 * Exception thrown if the user cannot be authenticated.
 *
 * @author Kevin Pollet
 */
public class CodenvyAuthenticationException extends CodenvyException {
    private static final long serialVersionUID = -700004295566607674L;

    /**
     * Constructs an instance of {@link CodenvyAuthenticationException}.
     *
     * @param message
     *         the exception message.
     */
    public CodenvyAuthenticationException(String message) {
        super(message);
    }
}
