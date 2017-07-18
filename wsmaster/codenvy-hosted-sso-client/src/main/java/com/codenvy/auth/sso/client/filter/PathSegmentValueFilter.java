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
 * Filter request by value of path segment.
 *
 * @author Sergii Kabashniuk
 */
public class PathSegmentValueFilter implements RequestFilter {

    private       int    segmentPosition;
    private final String segmentValue;

    public PathSegmentValueFilter(int segmentPosition, String segmentValue) {
        this.segmentPosition = segmentPosition;
        this.segmentValue = segmentValue;
    }


    @Override
    public boolean shouldSkip(HttpServletRequest request) {
        List<PathSegment> pathSegments = UriComponent.parsePathSegments(request.getRequestURI(), false);
        return pathSegments.size() >= segmentPosition && pathSegments.get(segmentPosition - 1) != null &&
               pathSegments.get(segmentPosition - 1).getPath().equals(segmentValue);
    }
}
