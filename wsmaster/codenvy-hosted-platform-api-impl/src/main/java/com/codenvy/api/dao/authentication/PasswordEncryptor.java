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
package com.codenvy.api.dao.authentication;

/**
 * Encrypts password in implementation specific way
 */
public interface PasswordEncryptor {

    /**
     * Encrypts (digests) the given {@code password}
     *
     * @param password
     *         the plain password to be encrypted
     * @return the encrypted password
     * @throws RuntimeException
     *         when it is not possible to encrypt password
     */
    byte[] encrypt(byte[] password) throws RuntimeException;
}
