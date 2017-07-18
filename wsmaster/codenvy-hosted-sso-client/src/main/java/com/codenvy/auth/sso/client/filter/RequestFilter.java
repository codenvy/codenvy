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
package com.codenvy.auth.sso.client.filter;

import com.google.inject.ImplementedBy;

import javax.servlet.http.HttpServletRequest;

/**
 * Used by LoginFilter to check if request should be skipped.
 * For complex case then configured LoginFilter request should be skipped.
 *
 * @author Sergii Kabashniuk
 */
@ImplementedBy(SkipNothingFilter.class)
public interface RequestFilter {
    /**
     * @param request
     *         - request in LoginFilter
     * @return - true if request should be skipped.
     */
    boolean shouldSkip(HttpServletRequest request);
}
