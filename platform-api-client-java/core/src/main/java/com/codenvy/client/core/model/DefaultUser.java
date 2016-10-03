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
package com.codenvy.client.core.model;

import com.codenvy.client.model.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class represents the user resource on Codenvy.
 *
 * @author Kevin Pollet
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultUser implements User {
    private final String id;
    private final String email;

    /**
     * Constructs an instance of {@linkplain DefaultUser}.
     *
     * @param id
     *         the user id.
     * @param email
     *         the user email.
     * @throws NullPointerException
     *         if id, password or email parameter is {@code null}.
     */
    @JsonCreator
    public DefaultUser(@JsonProperty("id") String id, @JsonProperty("email") String email) {
        checkNotNull(id);
        checkNotNull(email);

        this.id = id;
        this.email = email;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String email() {
        return email;
    }

}
