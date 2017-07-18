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

import com.google.inject.Singleton;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * Exclude LoginFilter request by regexp pattern;
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class RegexpRequestFilter implements RequestFilter {

    private final Pattern filterPattern;

    @Inject
    public RegexpRequestFilter(@Named("auth.sso.client_skip_filter_regexp") String filterPattern) {
        this.filterPattern = Pattern.compile(filterPattern);
    }

    @Override
    public boolean shouldSkip(HttpServletRequest request) {
        return filterPattern.matcher(request.getRequestURI()).matches();
    }
}
