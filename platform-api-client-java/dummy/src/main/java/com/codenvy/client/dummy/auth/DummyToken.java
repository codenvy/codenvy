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
package com.codenvy.client.dummy.auth;

import com.codenvy.client.auth.Token;

/**
 * Dummy token class.
 *
 * @author Florent Benoit
 */
public class DummyToken implements Token {
    private final String value;

    /**
     * Constructs an instance of {@linkplain DummyToken}.
     *
     * @param value
     *         the authentication token.
     */
    public DummyToken(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DummyToken other = (DummyToken) obj;

        return this.value.equals(other.value);
    }

    @Override
    public String value() {
        return value;
    }
}
