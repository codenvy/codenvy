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

import org.everrest.core.impl.uri.UriComponent;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.PathSegment;
import java.util.List;

/**
 * Filter request by number of path segments.
 *
 * @author Sergii Kabashniuk
 */
public class PathSegmentNumberFilter implements RequestFilter {

    private final int segmentNumber;

    public PathSegmentNumberFilter(int segmentNumber) {
        this.segmentNumber = segmentNumber;
    }


    @Override
    public boolean shouldSkip(HttpServletRequest request) {
        List<PathSegment> pathSegments = UriComponent.parsePathSegments(request.getRequestURI(), false);
        int notEmptyPathSergments = 0;
        for (PathSegment pathSegment : pathSegments) {
            if (pathSegment.getPath() != null && !pathSegment.getPath().isEmpty()) {
                notEmptyPathSergments++;
            }
        }
        return notEmptyPathSergments == segmentNumber;
    }
}
