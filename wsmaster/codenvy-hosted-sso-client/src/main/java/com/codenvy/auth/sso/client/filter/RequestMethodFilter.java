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

import javax.servlet.http.HttpServletRequest;

/**
 * Filter request by request method
 *
 * @author Sergii Kabashniuk
 */
public class RequestMethodFilter implements RequestFilter {
    private final String requestMethod;

    public RequestMethodFilter(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    @Override
    public boolean shouldSkip(HttpServletRequest request) {
        return requestMethod.equals(request.getMethod());
    }
}
