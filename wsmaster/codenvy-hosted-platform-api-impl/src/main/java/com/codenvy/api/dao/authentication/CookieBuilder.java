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
package com.codenvy.api.dao.authentication;

import javax.ws.rs.core.Response;

/**
 * Utility class to helps build response after authentication.
 * <p/>
 * It allow to to set or remove such cookies:
 * <p/>
 */
public interface CookieBuilder {

    public void clearCookies(Response.ResponseBuilder builder, String token, boolean secure);

    public void setCookies(Response.ResponseBuilder builder, String token, boolean secure);
}
