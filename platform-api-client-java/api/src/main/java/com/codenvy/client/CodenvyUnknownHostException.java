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

import java.net.UnknownHostException;

/**
 * Exceptions thrown if a request is done on an unknown host.
 *
 * @author Kevin Pollet
 * @see UnknownHostException
 */
public class CodenvyUnknownHostException extends CodenvyException {
    private static final long serialVersionUID = 6895081544136542396L;

    /**
     * Constructs an instance of {@link CodenvyUnknownHostException} from an {@link UnknownHostException}.
     *
     * @param e
     *         the {@link UnknownHostException} instance.
     * @return the {@link CodenvyUnknownHostException} instance.
     */
    public static CodenvyUnknownHostException from(UnknownHostException e) {
        return new CodenvyUnknownHostException(e.getMessage(), e);
    }

    /**
     * Constructs an instance of {@link CodenvyUnknownHostException}.
     *
     * @param message
     *         the exception message.
     * @param cause
     *         the exception cause.
     */
    public CodenvyUnknownHostException(String message, Throwable cause) {
        super(message, cause);
    }
}
