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
package com.codenvy.client.core;

/**
 * The request response adaptor contract.
 *
 * @param <T>
 *         the {@linkplain java.lang.reflect.Type Type} of the adapted request response
 * @param <S>
 *         the {@linkplain java.lang.reflect.Type Type} of the request response to adapt.
 * @author Kevin Pollet
 */
public interface Adaptor<T, S> {
    /**
     * Adapts the request response.
     *
     * @param response
     *         the request response to adapt
     * @return the adapted response.
     */
    T adapt(S response);
}