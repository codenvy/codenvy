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
import com.codenvy.client.auth.TokenBuilder;

/**
 * Token builder.
 *
 * @author Florent Benoit
 */
public class DummyTokenBuilder implements TokenBuilder {

    /**
     * Value of the token.
     */
    private final String value;

    /**
     * Build a new token builder with the specified value
     *
     * @param value
     *         the value of the token
     */
    public DummyTokenBuilder(String value) {
        this.value = value;
    }

    /**
     * @return instance of {@link com.codenvy.client.auth.Token}
     */
    @Override
    public Token build() {
        return new DummyToken(value);
    }
}
