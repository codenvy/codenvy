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
