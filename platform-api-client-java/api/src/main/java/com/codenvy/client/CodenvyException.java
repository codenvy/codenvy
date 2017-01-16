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


/**
 * The base Codenvy exception.
 *
 * @author Kevin Pollet
 */
public class CodenvyException extends RuntimeException {
    private static final long serialVersionUID = 197371454259368237L;

    /**
     * Constructs an instance of {@link CodenvyException} from an {@link Exception}.
     *
     * @param e
     *         the {@link Exception} instance.
     * @return the created {@link CodenvyException}.
     */
    public static CodenvyException from(Exception e) {
        return new CodenvyException(e.getMessage(), e.getCause());
    }

    /**
     * Constructs an instance of {@link CodenvyException}.
     *
     * @param message
     *         the exception message.
     * @param cause
     *         the {@link Throwable} cause
     */
    public CodenvyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of {@link CodenvyException}.
     *
     * @param message
     *         the exception message.
     */
    public CodenvyException(String message) {
        super(message);
    }
}
