/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
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
